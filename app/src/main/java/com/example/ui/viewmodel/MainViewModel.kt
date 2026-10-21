package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.device.ActionExecutionResult
import com.example.data.device.DeviceActionManager
import com.example.data.local.*
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    data object Splash : Screen()
    data object Home : Screen()
    data object Chat : Screen()
    data object History : Screen()
    data object SettingsMain : Screen()
    data object SettingsVoiceLanguage : Screen()
    data object SettingsConnections : Screen()
    data object SettingsLookFeel : Screen()
    data object SettingsQuickActions : Screen()
    data object SettingsPhoneControl : Screen()
    data object SettingsChatsMemory : Screen()
    data object SettingsPrivacyData : Screen()
    data object SettingsPrivacyAbout : Screen()
    data object SettingsProfile : Screen()
    data class WebsitePreview(val title: String, val htmlCode: String) : Screen()
    data class SketchPreview(val prompt: String, val imageResId: Int = 0) : Screen()
}

data class UiNotice(
    val message: String,
    val isError: Boolean = false,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"

    private val db = AppDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    val preferencesRepository = AppPreferencesRepository(application)
    val preferences = preferencesRepository.preferences

    val deviceActionManager = DeviceActionManager(application)
    val aiRepository = AiRepository()
    val voiceEngine = VoiceSynthesisEngine()
    val audioRecorder = AudioRecorderManager(application)

    // Navigation state with backstack
    private val screenStack = ArrayDeque<Screen>().apply { add(Screen.Splash) }
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Orb & Voice State
    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _liveVoiceTranscript = MutableStateFlow("")
    val liveVoiceTranscript: StateFlow<String> = _liveVoiceTranscript.asStateFlow()

    // Active Chat State
    private val _activeConversationId = MutableStateFlow<String>(UUID.randomUUID().toString())
    val activeConversationId: StateFlow<String> = _activeConversationId.asStateFlow()

    val currentMessages: StateFlow<List<ChatMessageEntity>> = _activeConversationId
        .flatMapLatest { id -> chatDao.getMessagesForConversation(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversationsList: StateFlow<List<ConversationEntity>> = chatDao.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending Confirmation Action (e.g. Call or Message confirmation)
    private val _pendingAction = MutableStateFlow<DeviceAction?>(null)
    val pendingAction: StateFlow<DeviceAction?> = _pendingAction.asStateFlow()

    // Notices & Status
    private val _currentNotice = MutableStateFlow<UiNotice?>(null)
    val currentNotice: StateFlow<UiNotice?> = _currentNotice.asStateFlow()

    // Connections test state
    private val _geminiStatus = MutableStateFlow("Unchecked")
    val geminiStatus: StateFlow<String> = _geminiStatus.asStateFlow()

    private val _groqStatus = MutableStateFlow("Unchecked")
    val groqStatus: StateFlow<String> = _groqStatus.asStateFlow()

    init {
        // Bind voice engine callbacks
        voiceEngine.onSpeechStarted = {
            _orbState.value = OrbState.SPEAKING
        }
        voiceEngine.onSpeechCompleted = {
            if (_orbState.value == OrbState.SPEAKING) {
                _orbState.value = OrbState.IDLE
            }
            _audioAmplitude.value = 0f
        }
        voiceEngine.onAudioAmplitudeUpdate = { amp ->
            _audioAmplitude.value = amp
        }

        // Bind audio recorder callbacks
        audioRecorder.onAmplitudeUpdate = { amp ->
            if (_orbState.value == OrbState.LISTENING) {
                _audioAmplitude.value = amp
            }
        }

        // Initial validation check
        viewModelScope.launch {
            checkInitialConnections()
        }
    }

    private suspend fun checkInitialConnections() {
        val prefs = preferences.value
        if (prefs.geminiApiKey.isNotBlank()) {
            val (ok, msg) = aiRepository.validateGeminiKey(prefs.geminiApiKey, prefs.geminiModel)
            _geminiStatus.value = if (ok) "Ready" else "Error: $msg"
        } else {
            _geminiStatus.value = "Not configured"
        }

        if (prefs.groqApiKeys.isNotBlank()) {
            val (count, msg) = aiRepository.validateGroqKeys(prefs.groqApiKeys, prefs.groqModel)
            _groqStatus.value = if (count > 0) "$count key(s) Ready" else "Error: $msg"
        } else {
            _groqStatus.value = "Not configured"
        }
    }

    // Navigation functions
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeLast()
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    fun dismissNotice() {
        _currentNotice.value = null
    }

    fun showNotice(message: String, isError: Boolean = false, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _currentNotice.value = UiNotice(message, isError, actionLabel, onAction)
    }

    // Voice Session Toggle (from Orb click)
    fun toggleVoiceSession() {
        when (_orbState.value) {
            OrbState.SPEAKING -> {
                // Interruption: stop talking
                voiceEngine.stop()
                _orbState.value = OrbState.IDLE
            }
            OrbState.LISTENING -> {
                // Stop listening and process
                stopListeningAndProcess()
            }
            OrbState.THINKING -> {
                // Cancel
                _orbState.value = OrbState.IDLE
            }
            OrbState.IDLE, OrbState.ERROR -> {
                startVoiceListening()
            }
        }
    }

    fun startVoiceListening() {
        voiceEngine.stop()
        audioRecorder.startListening(viewModelScope)
        _orbState.value = OrbState.LISTENING
        _liveVoiceTranscript.value = "Listening... Speak or tap a command"
    }

    fun updateLiveTranscript(text: String) {
        _liveVoiceTranscript.value = text
    }

    fun stopListeningAndProcess(spokenPhrase: String? = null) {
        audioRecorder.stopListening()
        val textToProcess = spokenPhrase ?: _liveVoiceTranscript.value.replace("Listening... Speak now", "").trim()

        if (textToProcess.isNotBlank() && textToProcess != "Listening... Speak now") {
            _orbState.value = OrbState.THINKING
            sendChatMessage(textToProcess, isVoiceTurn = true)
        } else {
            _orbState.value = OrbState.IDLE
        }
    }

    // Chat and Messaging
    fun startNewConversation() {
        _activeConversationId.value = UUID.randomUUID().toString()
    }

    fun selectConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        navigateTo(Screen.Chat)
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.deleteConversation(id)
            chatDao.deleteMessagesForConversation(id)
            if (_activeConversationId.value == id) {
                _activeConversationId.value = UUID.randomUUID().toString()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.clearAllConversations()
            chatDao.clearAllMessages()
            _activeConversationId.value = UUID.randomUUID().toString()
            showNotice("Conversation history cleared.")
        }
    }

    fun sendChatMessage(
        text: String,
        isVoiceTurn: Boolean = false,
        imageUri: Uri? = null,
        context: Context? = null
    ) {
        if (text.isBlank() && imageUri == null) return

        val conversationId = _activeConversationId.value
        val prefs = preferences.value

        viewModelScope.launch(Dispatchers.IO) {
            // Read image bytes if attached
            var imageBytes: ByteArray? = null
            var imageMime = "image/jpeg"
            if (imageUri != null && context != null) {
                try {
                    val cr = context.contentResolver
                    imageMime = cr.getType(imageUri) ?: "image/jpeg"
                    cr.openInputStream(imageUri)?.use { input ->
                        val buffer = ByteArrayOutputStream()
                        val data = ByteArray(4096)
                        var nRead: Int
                        while (input.read(data, 0, data.size).also { nRead = it } != -1) {
                            buffer.write(data, 0, nRead)
                        }
                        imageBytes = buffer.toByteArray()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading image: ${e.message}")
                }
            }

            // Ensure conversation exists in DB
            val existing = chatDao.getConversationById(conversationId)
            if (existing == null) {
                val title = if (text.isNotBlank()) text.take(32) else "Image query"
                chatDao.insertConversation(
                    ConversationEntity(
                        id = conversationId,
                        title = title,
                        lastMessageSnippet = text.take(60)
                    )
                )
            } else {
                chatDao.updateConversation(
                    existing.copy(
                        updatedAt = System.currentTimeMillis(),
                        lastMessageSnippet = text.take(60)
                    )
                )
            }

            // Save user message
            val userMsg = ChatMessageEntity(
                conversationId = conversationId,
                role = "user",
                content = text,
                hasAttachment = imageUri != null,
                attachmentUri = imageUri?.toString(),
                attachmentType = if (imageUri != null) "image" else null
            )
            chatDao.insertMessage(userMsg)

            _orbState.value = OrbState.THINKING

            // Fetch prior messages for context
            val history = chatDao.getMessagesList(conversationId)
                .map { Pair(it.role, it.content) }

            // Call AI provider
            val response = aiRepository.sendMessage(
                userMessage = text,
                preferences = prefs,
                chatHistory = history,
                imageBytes = imageBytes,
                imageMime = imageMime
            )

            // Save assistant reply
            val assistantMsg = ChatMessageEntity(
                conversationId = conversationId,
                role = "assistant",
                content = response.replyText,
                modelUsed = response.modelUsed,
                isError = !response.isSuccess
            )
            chatDao.insertMessage(assistantMsg)

            // Handle detected device action if any
            response.detectedAction?.let { action ->
                handleParsedAction(action)
            }

            // Spoken voice reply if isVoiceTurn or voice response enabled
            if (response.isSuccess) {
                if (isVoiceTurn) {
                    voiceEngine.speak(
                        text = response.replyText,
                        voice = prefs.selectedVoice,
                        speed = prefs.speechSpeed,
                        volume = prefs.speechVolume
                    )
                } else {
                    _orbState.value = OrbState.IDLE
                }
            } else {
                _orbState.value = OrbState.ERROR
                showNotice(response.errorMessage ?: "AI Provider error", isError = true)
            }
        }
    }

    private fun handleParsedAction(action: ParsedAction) {
        viewModelScope.launch(Dispatchers.Main) {
            when (action) {
                is ParsedAction.OpenApp -> {
                    val result = deviceActionManager.openApp(action.appName)
                    when (result) {
                        is ActionExecutionResult.Success -> showNotice(result.message)
                        is ActionExecutionResult.Error -> showNotice(result.error, isError = true)
                        else -> {}
                    }
                }
                is ParsedAction.Call -> {
                    // Search contact first to get phone number
                    val contacts = deviceActionManager.searchContacts(action.targetName)
                    if (contacts.isNotEmpty()) {
                        val contact = contacts.first()
                        _pendingAction.value = DeviceAction.CallPhone(contact.name, contact.phoneNumber)
                    } else {
                        // Dial number directly if numerical, else prompt confirmation
                        val clean = action.targetName.replace(Regex("[^0-9+]"), "")
                        if (clean.length >= 3) {
                            _pendingAction.value = DeviceAction.CallPhone(clean, clean)
                        } else {
                            showNotice("Contact \"${action.targetName}\" not found in phonebook.", isError = true)
                        }
                    }
                }
                is ParsedAction.WhatsApp -> {
                    val contacts = deviceActionManager.searchContacts(action.targetName)
                    val phone = contacts.firstOrNull()?.phoneNumber
                    _pendingAction.value = DeviceAction.SendMessage(
                        contactName = action.targetName,
                        phoneNumber = phone,
                        message = action.message,
                        isWhatsApp = true
                    )
                }
                is ParsedAction.Sms -> {
                    val contacts = deviceActionManager.searchContacts(action.targetName)
                    val phone = contacts.firstOrNull()?.phoneNumber
                    _pendingAction.value = DeviceAction.SendMessage(
                        contactName = action.targetName,
                        phoneNumber = phone,
                        message = action.message,
                        isWhatsApp = false
                    )
                }
                is ParsedAction.OpenSettings -> {
                    deviceActionManager.openSettings()
                }
                is ParsedAction.BuildWebsite -> {
                    navigateTo(Screen.WebsitePreview(action.title, action.htmlCode))
                }
                is ParsedAction.SketchPhoto -> {
                    navigateTo(Screen.SketchPreview(action.prompt))
                }
                is ParsedAction.OpenWeb -> {
                    deviceActionManager.openWebUrl(action.url, action.title)
                }
            }
        }
    }

    fun confirmPendingAction() {
        val action = _pendingAction.value ?: return
        _pendingAction.value = null
        when (action) {
            is DeviceAction.CallPhone -> {
                deviceActionManager.dialPhoneNumber(action.phoneNumber, action.contactName)
            }
            is DeviceAction.SendMessage -> {
                if (action.isWhatsApp) {
                    deviceActionManager.prepareWhatsApp(action.phoneNumber, action.message, action.contactName)
                } else {
                    deviceActionManager.prepareSms(action.phoneNumber ?: "", action.message, action.contactName)
                }
            }
            is DeviceAction.OpenApp -> {
                viewModelScope.launch {
                    deviceActionManager.openApp(action.appName)
                }
            }
            else -> {}
        }
    }

    fun dismissPendingAction() {
        _pendingAction.value = null
    }

    // Connections Validation Actions
    fun testGeminiKey(key: String, model: String) {
        viewModelScope.launch {
            _geminiStatus.value = "Validating..."
            val (ok, msg) = aiRepository.validateGeminiKey(key, model)
            _geminiStatus.value = if (ok) "Ready" else "Error: $msg"
        }
    }

    fun testGroqKeys(keys: String, model: String) {
        viewModelScope.launch {
            _groqStatus.value = "Validating..."
            val (count, msg) = aiRepository.validateGroqKeys(keys, model)
            _groqStatus.value = if (count > 0) "$count key(s) Ready" else "Error: $msg"
        }
    }

    // Voice Preview
    fun previewVoice(voice: VoiceOption) {
        val prefs = preferences.value
        voiceEngine.previewVoice(voice, prefs.speechSpeed, prefs.speechVolume)
    }

    fun stopVoicePreview() {
        voiceEngine.stop()
    }
}

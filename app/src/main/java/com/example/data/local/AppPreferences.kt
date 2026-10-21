package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserPreferences(
    val assistantName: String = "LIA",
    val userDisplayName: String = "Alex",
    val personality: Personality = Personality.WARM_FRIENDLY,
    val customInstructions: String = "",
    val language: LanguageOption = LanguageOption.AUTO,
    val voiceGender: VoiceGender = VoiceGender.FEMALE,
    val selectedVoice: VoiceOption = VoiceOption.ARIA,
    val speechSpeed: Float = 1.0f,
    val speechVolume: Float = 0.9f,
    val listeningMode: ListeningMode = ListeningMode.TAP_TO_TALK,
    val chatProvider: ProviderType = ProviderType.GEMINI,
    val voiceProvider: ProviderType = ProviderType.GEMINI,
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-2.5-flash",
    val groqApiKeys: String = "", // Comma-separated or single key
    val groqModel: String = "llama-3.3-70b-versatile",
    val themeMode: ThemeMode = ThemeMode.DARK_PURPLE,
    val orbStyle: OrbStyle = OrbStyle.CLASSIC,
    val reduceMotion: Boolean = false,
    val ambientEdgeGlow: Boolean = true,
    val memoryEnabled: Boolean = true,
    val savedMemorySummary: String = "User prefers friendly responses, tech/coding advice, and speaks English & Hindi."
)

class AppPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_ai_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        val savedGeminiKey = prefs.getString("gemini_api_key", "") ?: ""
        val effectiveGeminiKey = if (savedGeminiKey.isNotBlank()) {
            savedGeminiKey
        } else if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            buildConfigKey
        } else {
            ""
        }

        val savedGroqKey = prefs.getString("groq_api_keys", "") ?: ""
        val effectiveGroqKey = if (savedGroqKey.isNotBlank()) {
            savedGroqKey
        } else {
            try {
                // Check if injected via BuildConfig
                val field = BuildConfig::class.java.getField("GROQ_API_KEY")
                val v = field.get(null) as? String ?: ""
                if (v.isNotBlank() && v != "MY_GROQ_API_KEY") v else ""
            } catch (_: Exception) {
                ""
            }
        }

        return UserPreferences(
            assistantName = prefs.getString("assistant_name", "LIA") ?: "LIA",
            userDisplayName = prefs.getString("user_display_name", "Alex") ?: "Alex",
            personality = Personality.entries.find { it.name == prefs.getString("personality", null) } ?: Personality.WARM_FRIENDLY,
            customInstructions = prefs.getString("custom_instructions", "") ?: "",
            language = LanguageOption.entries.find { it.name == prefs.getString("language", null) } ?: LanguageOption.AUTO,
            voiceGender = VoiceGender.entries.find { it.name == prefs.getString("voice_gender", null) } ?: VoiceGender.FEMALE,
            selectedVoice = VoiceOption.entries.find { it.id == prefs.getString("selected_voice", null) } ?: VoiceOption.ARIA,
            speechSpeed = prefs.getFloat("speech_speed", 1.0f),
            speechVolume = prefs.getFloat("speech_volume", 0.9f),
            listeningMode = ListeningMode.entries.find { it.name == prefs.getString("listening_mode", null) } ?: ListeningMode.TAP_TO_TALK,
            chatProvider = ProviderType.entries.find { it.name == prefs.getString("chat_provider", null) } ?: ProviderType.GEMINI,
            voiceProvider = ProviderType.entries.find { it.name == prefs.getString("voice_provider", null) } ?: ProviderType.GEMINI,
            geminiApiKey = effectiveGeminiKey,
            geminiModel = prefs.getString("gemini_model", "gemini-2.5-flash") ?: "gemini-2.5-flash",
            groqApiKeys = effectiveGroqKey,
            groqModel = prefs.getString("groq_model", "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile",
            themeMode = ThemeMode.entries.find { it.name == prefs.getString("theme_mode", null) } ?: ThemeMode.DARK_PURPLE,
            orbStyle = OrbStyle.entries.find { it.name == prefs.getString("orb_style", null) } ?: OrbStyle.CLASSIC,
            reduceMotion = prefs.getBoolean("reduce_motion", false),
            ambientEdgeGlow = prefs.getBoolean("ambient_edge_glow", true),
            memoryEnabled = prefs.getBoolean("memory_enabled", true),
            savedMemorySummary = prefs.getString("saved_memory_summary", "User prefers friendly responses, tech/coding advice, and speaks English & Hindi.") ?: ""
        )
    }

    fun updatePreferences(update: (UserPreferences) -> UserPreferences) {
        val current = _preferences.value
        val newPrefs = update(current)
        _preferences.value = newPrefs

        prefs.edit().apply {
            putString("assistant_name", newPrefs.assistantName)
            putString("user_display_name", newPrefs.userDisplayName)
            putString("personality", newPrefs.personality.name)
            putString("custom_instructions", newPrefs.customInstructions)
            putString("language", newPrefs.language.name)
            putString("voice_gender", newPrefs.voiceGender.name)
            putString("selected_voice", newPrefs.selectedVoice.id)
            putFloat("speech_speed", newPrefs.speechSpeed)
            putFloat("speech_volume", newPrefs.speechVolume)
            putString("listening_mode", newPrefs.listeningMode.name)
            putString("chat_provider", newPrefs.chatProvider.name)
            putString("voice_provider", newPrefs.voiceProvider.name)
            putString("gemini_api_key", newPrefs.geminiApiKey)
            putString("gemini_model", newPrefs.geminiModel)
            putString("groq_api_keys", newPrefs.groqApiKeys)
            putString("groq_model", newPrefs.groqModel)
            putString("theme_mode", newPrefs.themeMode.name)
            putString("orb_style", newPrefs.orbStyle.name)
            putBoolean("reduce_motion", newPrefs.reduceMotion)
            putBoolean("ambient_edge_glow", newPrefs.ambientEdgeGlow)
            putBoolean("memory_enabled", newPrefs.memoryEnabled)
            putString("saved_memory_summary", newPrefs.savedMemorySummary)
            apply()
        }
    }

    fun clearApiKeys() {
        updatePreferences {
            it.copy(geminiApiKey = "", groqApiKeys = "")
        }
    }

    fun clearMemory() {
        updatePreferences {
            it.copy(savedMemorySummary = "")
        }
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _preferences.value = loadPreferences()
    }
}

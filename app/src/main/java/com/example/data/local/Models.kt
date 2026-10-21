package com.example.data.local

import androidx.compose.ui.graphics.Color

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class Personality(val displayName: String, val description: String) {
    WARM_FRIENDLY("Warm and Friendly", "Empathetic, encouraging, and welcoming companion"),
    PROFESSIONAL("Professional", "Clear, organized, and business-focused assistant"),
    DEVELOPER("Developer / Technical", "Precise, code-savvy, and deeply technical"),
    WITTY("Witty & Playful", "Humorous, clever, and entertaining conversationalist"),
    CONCISE("Concise & Direct", "Brief, to the point, minimal small talk"),
    DETAILED("Detailed & Thorough", "Comprehensive explanations and deep analysis"),
    CUSTOM("Custom Instructions", "User-defined behavioral persona")
}

enum class LanguageOption(val displayName: String, val code: String) {
    AUTO("Auto Detect", "auto"),
    ENGLISH("English", "en"),
    HINDI("Hindi (हिन्दी)", "hi"),
    HINGLISH("Hinglish", "hi-en"),
    URDU("Urdu (اردو)", "ur")
}

enum class VoiceGender {
    FEMALE,
    MALE
}

enum class VoiceOption(
    val id: String,
    val voiceName: String,
    val gender: VoiceGender,
    val description: String,
    val baseFrequency: Float, // Pitch base for natural acoustic synthesis
    val style: String
) {
    ARIA("aria", "Aria", VoiceGender.FEMALE, "Natural, warm female voice with rich prosody", 220f, "Warm & Melodic"),
    SERAPHINA("seraphina", "Seraphina", VoiceGender.FEMALE, "Soft conversational female voice with gentle timbre", 240f, "Soft & Intimate"),
    ELENA("elena", "Elena", VoiceGender.FEMALE, "Clear professional female voice for business & knowledge", 200f, "Clear & Articulate"),
    LIAM("liam", "Liam", VoiceGender.MALE, "Natural, warm male voice with balanced tone", 140f, "Warm & Engaging"),
    MARCUS("marcus", "Marcus", VoiceGender.MALE, "Deep conversational male voice with resonant bass", 110f, "Deep & Resonant"),
    DAVID("david", "David", VoiceGender.MALE, "Clear professional male voice with authoritative clarity", 130f, "Crisp & Professional")
}

enum class ThemeMode(val displayName: String) {
    DARK_PURPLE("Dark Purple"),
    BLACK_GOLD("Black and Gold"),
    MIDNIGHT_BLUE("Midnight Blue")
}

enum class OrbStyle(val displayName: String) {
    CLASSIC("Classic Energy"),
    GALAXY("Cosmic Galaxy"),
    AURORA("Aurora Borealis"),
    MINIMAL("Minimal Halo")
}

enum class ListeningMode(val displayName: String) {
    TAP_TO_TALK("Tap to Talk"),
    HOLD_TO_TALK("Hold to Talk"),
    CONTINUOUS("Continuous Conversation")
}

enum class ProviderType(val displayName: String) {
    GEMINI("Google Gemini"),
    GROQ("Groq Cloud")
}

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)

data class InstalledAppItem(
    val label: String,
    val packageName: String
)

sealed class DeviceAction {
    data class OpenApp(val appName: String, val packageName: String?) : DeviceAction()
    data class CallPhone(val contactName: String, val phoneNumber: String) : DeviceAction()
    data class SendMessage(val contactName: String, val phoneNumber: String?, val message: String, val isWhatsApp: Boolean) : DeviceAction()
    data class OpenSettings(val settingName: String = "") : DeviceAction()
    data class OpenWeb(val url: String, val title: String) : DeviceAction()
    data class BuildWebsite(val title: String, val htmlCode: String) : DeviceAction()
    data class SketchPhoto(val prompt: String) : DeviceAction()
}

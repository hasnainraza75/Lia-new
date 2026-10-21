package com.example.data.remote

import android.util.Base64
import android.util.Log
import com.example.data.local.LanguageOption
import com.example.data.local.Personality
import com.example.data.local.ProviderType
import com.example.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class AiResponse(
    val replyText: String,
    val detectedAction: ParsedAction? = null,
    val modelUsed: String,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

sealed class ParsedAction {
    data class OpenApp(val appName: String) : ParsedAction()
    data class Call(val targetName: String) : ParsedAction()
    data class WhatsApp(val targetName: String, val message: String) : ParsedAction()
    data class Sms(val targetName: String, val message: String) : ParsedAction()
    data class OpenSettings(val setting: String = "") : ParsedAction()
    data class BuildWebsite(val title: String, val htmlCode: String) : ParsedAction()
    data class SketchPhoto(val prompt: String) : ParsedAction()
    data class OpenWeb(val url: String, val title: String) : ParsedAction()
}

class AiRepository {
    private val TAG = "AiRepository"
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    /**
     * Validates a Gemini API Key with a small test request.
     */
    suspend fun validateGeminiKey(apiKey: String, model: String = "gemini-2.5-flash"): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext Pair(false, "Gemini API key is empty.")
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val bodyJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", "ping"))
                            })
                        })
                    })
                }
                val request = Request.Builder()
                    .url(url)
                    .post(bodyJson.toString().toRequestBody(JSON_MEDIA))
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        Pair(true, "Gemini connection ready!")
                    } else {
                        val errorMsg = try {
                            JSONObject(body).getJSONObject("error").getString("message")
                        } catch (_: Exception) {
                            "HTTP ${response.code}: $body"
                        }
                        Pair(false, errorMsg)
                    }
                }
            } catch (e: Exception) {
                Pair(false, e.localizedMessage ?: "Network connection error")
            }
        }

    /**
     * Validates Groq API Key(s) and returns (validCount, message).
     */
    suspend fun validateGroqKeys(keysStr: String, model: String = "llama-3.3-70b-versatile"): Pair<Int, String> =
        withContext(Dispatchers.IO) {
            val keyList = keysStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (keyList.isEmpty()) return@withContext Pair(0, "No Groq API keys configured.")

            var validCount = 0
            var lastError = ""

            for (key in keyList) {
                try {
                    val url = "https://api.groq.com/openai/v1/chat/completions"
                    val bodyJson = JSONObject().apply {
                        put("model", model)
                        put("messages", JSONArray().apply {
                            put(JSONObject().put("role", "user").put("content", "ping"))
                        })
                        put("max_tokens", 5)
                    }
                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer $key")
                        .post(bodyJson.toString().toRequestBody(JSON_MEDIA))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            validCount++
                        } else {
                            val b = response.body?.string() ?: ""
                            lastError = "Key ${key.take(4)}... HTTP ${response.code}: $b"
                        }
                    }
                } catch (e: Exception) {
                    lastError = e.localizedMessage ?: "Connection error"
                }
            }

            if (validCount > 0) {
                Pair(validCount, "$validCount of ${keyList.size} Groq key(s) verified.")
            } else {
                Pair(0, lastError.ifBlank { "Could not verify Groq keys." })
            }
        }

    /**
     * Sends message to the user-selected AI provider (Gemini or Groq).
     */
    suspend fun sendMessage(
        userMessage: String,
        preferences: UserPreferences,
        chatHistory: List<Pair<String, String>> = emptyList(), // role to content
        imageBytes: ByteArray? = null,
        imageMime: String = "image/jpeg"
    ): AiResponse = withContext(Dispatchers.IO) {
        val provider = preferences.chatProvider

        if (provider == ProviderType.GEMINI) {
            sendGeminiMessage(userMessage, preferences, chatHistory, imageBytes, imageMime)
        } else {
            // Groq does not support binary image upload directly on Llama text models, fallback to Gemini if image is present
            if (imageBytes != null && preferences.geminiApiKey.isNotBlank()) {
                sendGeminiMessage(userMessage, preferences, chatHistory, imageBytes, imageMime)
            } else {
                sendGroqMessage(userMessage, preferences, chatHistory)
            }
        }
    }

    private fun buildSystemPrompt(preferences: UserPreferences): String {
        val langInstruction = when (preferences.language) {
            LanguageOption.AUTO -> "Detect the user's language and respond naturally in the exact same language (English, Hindi, Hinglish, Urdu). Code-switch smoothly if they speak Hinglish."
            LanguageOption.ENGLISH -> "Respond primarily in fluent, clear English."
            LanguageOption.HINDI -> "Respond in natural, polite Hindi (Devanagari script or conversational Hindi)."
            LanguageOption.HINGLISH -> "Respond in natural conversational Hinglish (Hindi written in Roman script)."
            LanguageOption.URDU -> "Respond in polite Urdu."
        }

        val personaInstruction = when (preferences.personality) {
            Personality.WARM_FRIENDLY -> "You are warm, empathetic, caring, friendly, and always encouraging."
            Personality.PROFESSIONAL -> "You are crisp, professional, structured, and business-focused."
            Personality.DEVELOPER -> "You are a technical software engineer and architecture expert with clean code examples."
            Personality.WITTY -> "You are clever, witty, humorous, and engaging."
            Personality.CONCISE -> "You are concise and direct, providing answers in 1-3 sentences without filler."
            Personality.DETAILED -> "You provide comprehensive, in-depth breakdowns and explanations."
            Personality.CUSTOM -> preferences.customInstructions.ifBlank { "Be helpful, accurate and polite." }
        }

        val memoryContext = if (preferences.memoryEnabled && preferences.savedMemorySummary.isNotBlank()) {
            "\nLong-Term User Memory:\n${preferences.savedMemorySummary}\n"
        } else ""

        return """
            You are ${preferences.assistantName}, the user's personal AI companion and phone assistant.
            $personaInstruction
            $langInstruction
            $memoryContext
            
            IMPORTANT PHONE ACTIONS:
            You can trigger real device actions by appending an action tag at the very end of your response on a new line:
            - If user asks to open an app (e.g. "YouTube kholo", "open WhatsApp", "open Chrome", "Camera kholo"):
              [[ACTION:OPEN_APP|AppName]]
            - If user asks to call someone (e.g. "Mummy ko call karo", "Call Rahul", "Call 9876543210"):
              [[ACTION:CALL|ContactNameOrNumber]]
            - If user asks to WhatsApp someone (e.g. "Papa ko WhatsApp karo hello", "WhatsApp Priya: See you tomorrow"):
              [[ACTION:WHATSAPP|ContactNameOrNumber|MessageContent]]
            - If user asks to SMS/message someone (e.g. "SMS John: On my way"):
              [[ACTION:SMS|ContactNameOrNumber|MessageContent]]
            - If user asks to open settings (e.g. "Settings kholo", "Open phone settings"):
              [[ACTION:SETTINGS]]
            - If user asks to build/generate a website (e.g. "Build a website for bakery"):
              [[ACTION:BUILD_WEBSITE|Website Title|<!DOCTYPE html><html>...HTML/CSS/JS code...</html>]]
            - If user asks to sketch/generate an image or photo (e.g. "Sketch a photo of futuristic flying car"):
              [[ACTION:SKETCH_PHOTO|detailed visual description]]
              
            Keep conversational replies natural, concise and friendly.
        """.trimIndent()
    }

    private fun sendGeminiMessage(
        userMessage: String,
        preferences: UserPreferences,
        chatHistory: List<Pair<String, String>>,
        imageBytes: ByteArray?,
        imageMime: String
    ): AiResponse {
        val apiKey = preferences.geminiApiKey
        if (apiKey.isBlank()) {
            return AiResponse(
                replyText = "Gemini API key is not configured. Please add your key in Settings -> Connections.",
                isSuccess = false,
                errorMessage = "API Key Missing",
                modelUsed = preferences.geminiModel
            )
        }

        val model = preferences.geminiModel
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject()
            // System instruction
            rootJson.put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", buildSystemPrompt(preferences)))
                })
            })

            val contentsArray = JSONArray()

            // Prior history (up to last 10 turns)
            val recentHistory = chatHistory.takeLast(10)
            for ((role, content) in recentHistory) {
                val mappedRole = if (role == "user") "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", mappedRole)
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", content))
                    })
                })
            }

            // Current user turn
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))

            if (imageBytes != null) {
                val base64Data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                currentParts.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", imageMime)
                        put("data", base64Data)
                    })
                })
            }

            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", currentParts)
            })

            rootJson.put("contents", contentsArray)

            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errMsg = try {
                        JSONObject(bodyStr).getJSONObject("error").getString("message")
                    } catch (_: Exception) {
                        "Error HTTP ${response.code}: $bodyStr"
                    }
                    return AiResponse(
                        replyText = "Error from Gemini: $errMsg",
                        isSuccess = false,
                        errorMessage = errMsg,
                        modelUsed = model
                    )
                }

                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val rawText = parts.getJSONObject(0).optString("text", "")
                        val (cleanText, action) = extractActionFromText(rawText)
                        return AiResponse(
                            replyText = cleanText,
                            detectedAction = action,
                            modelUsed = model,
                            isSuccess = true
                        )
                    }
                }
                return AiResponse("No response received from Gemini.", isSuccess = false, modelUsed = model)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed", e)
            return AiResponse(
                replyText = "Network error connecting to Gemini: ${e.localizedMessage}",
                isSuccess = false,
                errorMessage = e.message,
                modelUsed = model
            )
        }
    }

    private fun sendGroqMessage(
        userMessage: String,
        preferences: UserPreferences,
        chatHistory: List<Pair<String, String>>
    ): AiResponse {
        val keysList = preferences.groqApiKeys.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (keysList.isEmpty()) {
            return AiResponse(
                replyText = "Groq API key is not configured. Please add your key in Settings -> Connections.",
                isSuccess = false,
                errorMessage = "API Key Missing",
                modelUsed = preferences.groqModel
            )
        }

        val model = preferences.groqModel
        val url = "https://api.groq.com/openai/v1/chat/completions"

        // Rotate through keys if rate-limited
        var lastError = ""
        for (apiKey in keysList) {
            try {
                val rootJson = JSONObject().apply {
                    put("model", model)
                    put("temperature", 0.7)
                    put("max_tokens", 1024)

                    val messages = JSONArray()
                    messages.put(JSONObject().apply {
                        put("role", "system")
                        put("content", buildSystemPrompt(preferences))
                    })

                    val recent = chatHistory.takeLast(10)
                    for ((role, content) in recent) {
                        messages.put(JSONObject().apply {
                            put("role", if (role == "user") "user" else "assistant")
                            put("content", content)
                        })
                    }

                    messages.put(JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    })

                    put("messages", messages)
                }

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $apiKey")
                    .post(rootJson.toString().toRequestBody(JSON_MEDIA))
                    .build()

                client.newCall(request).execute().use { response ->
                    val bodyStr = response.body?.string() ?: ""
                    if (response.code == 429) {
                        // Rate limited -> try next key
                        lastError = "Rate limited on key (${apiKey.take(4)}...)"
                        return@use
                    }
                    if (!response.isSuccessful) {
                        lastError = "HTTP ${response.code}: $bodyStr"
                        return@use
                    }

                    val json = JSONObject(bodyStr)
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val choice = choices.getJSONObject(0)
                        val messageObj = choice.optJSONObject("message")
                        val rawText = messageObj?.optString("content", "") ?: ""
                        val (cleanText, action) = extractActionFromText(rawText)
                        return AiResponse(
                            replyText = cleanText,
                            detectedAction = action,
                            modelUsed = model,
                            isSuccess = true
                        )
                    }
                }
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: "Connection error"
            }
        }

        return AiResponse(
            replyText = "Groq request failed: $lastError",
            isSuccess = false,
            errorMessage = lastError,
            modelUsed = model
        )
    }

    private fun extractActionFromText(raw: String): Pair<String, ParsedAction?> {
        val actionRegex = Regex("\\[\\[ACTION:([^\\]]+)\\]\\]")
        val match = actionRegex.find(raw) ?: return Pair(raw.trim(), null)

        val fullMatch = match.value
        val actionBody = match.groupValues[1]
        val cleanText = raw.replace(fullMatch, "").trim()

        val tokens = actionBody.split("|")
        val actionType = tokens.getOrNull(0) ?: ""

        val parsedAction = when (actionType.uppercase()) {
            "OPEN_APP" -> {
                val app = tokens.getOrNull(1)?.trim() ?: ""
                if (app.isNotBlank()) ParsedAction.OpenApp(app) else null
            }
            "CALL" -> {
                val target = tokens.getOrNull(1)?.trim() ?: ""
                if (target.isNotBlank()) ParsedAction.Call(target) else null
            }
            "WHATSAPP" -> {
                val target = tokens.getOrNull(1)?.trim() ?: ""
                val msg = tokens.getOrNull(2)?.trim() ?: ""
                ParsedAction.WhatsApp(target, msg)
            }
            "SMS" -> {
                val target = tokens.getOrNull(1)?.trim() ?: ""
                val msg = tokens.getOrNull(2)?.trim() ?: ""
                ParsedAction.Sms(target, msg)
            }
            "SETTINGS" -> ParsedAction.OpenSettings()
            "BUILD_WEBSITE" -> {
                val title = tokens.getOrNull(1)?.trim() ?: "Generated Website"
                val code = tokens.getOrNull(2)?.trim() ?: ""
                ParsedAction.BuildWebsite(title, code)
            }
            "SKETCH_PHOTO" -> {
                val prompt = tokens.getOrNull(1)?.trim() ?: ""
                ParsedAction.SketchPhoto(prompt)
            }
            "OPEN_WEB" -> {
                val url = tokens.getOrNull(1)?.trim() ?: ""
                val title = tokens.getOrNull(2)?.trim() ?: "Link"
                ParsedAction.OpenWeb(url, title)
            }
            else -> null
        }

        return Pair(cleanText, parsedAction)
    }
}

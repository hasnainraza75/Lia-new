package com.example.data.device

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import com.example.data.local.ContactItem
import com.example.data.local.DeviceAction
import com.example.data.local.InstalledAppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

sealed class ActionExecutionResult {
    data class Success(val message: String, val action: DeviceAction? = null) : ActionExecutionResult()
    data class RequiresConfirmation(val message: String, val action: DeviceAction) : ActionExecutionResult()
    data class Error(val error: String) : ActionExecutionResult()
}

class DeviceActionManager(private val context: Context) {
    private val TAG = "DeviceActionManager"

    /**
     * Finds installed launchable applications matching a query or common name.
     */
    suspend fun getInstalledApps(): List<InstalledAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        resolveInfos.map {
            InstalledAppItem(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName
            )
        }.distinctBy { it.packageName }.sortedBy { it.label }
    }

    /**
     * Resolves app package name from user phrase (e.g. "YouTube", "WhatsApp", "Chrome", "Camera").
     */
    suspend fun resolveApp(appName: String): InstalledAppItem? = withContext(Dispatchers.IO) {
        val query = appName.trim().lowercase()
        val apps = getInstalledApps()

        // Exact or prefix match
        apps.firstOrNull { it.label.lowercase() == query }
            ?: apps.firstOrNull { it.label.lowercase().contains(query) }
            ?: when {
                query.contains("youtube") -> apps.firstOrNull { it.packageName.contains("youtube") }
                    ?: InstalledAppItem("YouTube", "com.google.android.youtube")
                query.contains("whatsapp") -> apps.firstOrNull { it.packageName.contains("whatsapp") }
                    ?: InstalledAppItem("WhatsApp", "com.whatsapp")
                query.contains("chrome") || query.contains("browser") -> apps.firstOrNull { it.packageName.contains("chrome") }
                query.contains("camera") -> apps.firstOrNull { it.label.lowercase().contains("camera") || it.packageName.contains("camera") }
                query.contains("settings") -> InstalledAppItem("Settings", "com.android.settings")
                query.contains("map") -> apps.firstOrNull { it.packageName.contains("maps") }
                query.contains("calc") -> apps.firstOrNull { it.label.lowercase().contains("calc") }
                query.contains("clock") || query.contains("alarm") -> apps.firstOrNull { it.label.lowercase().contains("clock") }
                else -> null
            }
    }

    /**
     * Launches an application by package name or name.
     */
    suspend fun openApp(appName: String): ActionExecutionResult = withContext(Dispatchers.Main) {
        val resolved = resolveApp(appName)
        if (resolved == null) {
            return@withContext ActionExecutionResult.Error(
                "Could not find an installed app matching \"$appName\". Please make sure it is installed."
            )
        }

        if (resolved.packageName == "com.android.settings") {
            return@withContext openSettings()
        }

        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(resolved.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
                ActionExecutionResult.Success(
                    "Opening ${resolved.label}...",
                    DeviceAction.OpenApp(resolved.label, resolved.packageName)
                )
            } catch (e: Exception) {
                ActionExecutionResult.Error("Failed to launch ${resolved.label}: ${e.message}")
            }
        } else {
            ActionExecutionResult.Error(
                "${resolved.label} is installed, but cannot be launched directly as a standalone app."
            )
        }
    }

    /**
     * Searches device contacts by name.
     */
    suspend fun searchContacts(nameQuery: String): List<ContactItem> = withContext(Dispatchers.IO) {
        val list = ArrayList<ContactItem>()
        if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$nameQuery%")

        try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (it.moveToNext()) {
                    val id = if (idCol >= 0) it.getString(idCol) else ""
                    val name = if (nameCol >= 0) it.getString(nameCol) else ""
                    val num = if (numCol >= 0) it.getString(numCol) else ""
                    val photo = if (photoCol >= 0) it.getString(photoCol) else null
                    list.add(ContactItem(id, name, num, photo))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts: ${e.message}")
        }
        list.distinctBy { "${it.name}-${it.phoneNumber}" }
    }

    /**
     * Dials a phone number using Android's native dialer.
     */
    fun dialPhoneNumber(phoneNumber: String, contactName: String = ""): ActionExecutionResult {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (cleanNumber.isBlank()) {
            return ActionExecutionResult.Error("Invalid phone number provided.")
        }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Success(
                "Calling ${if (contactName.isNotBlank()) contactName else cleanNumber}...",
                DeviceAction.CallPhone(contactName.ifBlank { cleanNumber }, cleanNumber)
            )
        } catch (e: Exception) {
            ActionExecutionResult.Error("Unable to open dialer: ${e.message}")
        }
    }

    /**
     * Prepares an SMS draft. Requires manual user Send tap.
     */
    fun prepareSms(phoneNumber: String, messageText: String, contactName: String = ""): ActionExecutionResult {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${if (cleanNumber.isNotBlank()) cleanNumber else ""}")
            putExtra("sms_body", messageText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Success(
                "SMS draft prepared for ${if (contactName.isNotBlank()) contactName else cleanNumber}. Please tap Send.",
                DeviceAction.SendMessage(contactName.ifBlank { cleanNumber }, cleanNumber, messageText, false)
            )
        } catch (e: Exception) {
            ActionExecutionResult.Error("Unable to open SMS application: ${e.message}")
        }
    }

    /**
     * Prepares a WhatsApp message. Requires manual user Send tap.
     */
    fun prepareWhatsApp(phoneNumber: String?, messageText: String, contactName: String = ""): ActionExecutionResult {
        val cleanNumber = phoneNumber?.replace(Regex("[^0-9+]"), "") ?: ""
        return try {
            val url = if (cleanNumber.isNotBlank()) {
                val numWithoutPlus = if (cleanNumber.startsWith("+")) cleanNumber.substring(1) else cleanNumber
                "https://api.whatsapp.com/send?phone=$numWithoutPlus&text=${URLEncoder.encode(messageText, "UTF-8")}"
            } else {
                "https://api.whatsapp.com/send?text=${URLEncoder.encode(messageText, "UTF-8")}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionExecutionResult.Success(
                "WhatsApp draft prepared for ${contactName.ifBlank { if (cleanNumber.isNotBlank()) cleanNumber else "contact" }}. Tap send to deliver.",
                DeviceAction.SendMessage(contactName.ifBlank { cleanNumber }, cleanNumber, messageText, true)
            )
        } catch (e: Exception) {
            // Fallback to standard share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(shareIntent)
                ActionExecutionResult.Success(
                    "Opening WhatsApp with draft message...",
                    DeviceAction.SendMessage(contactName, cleanNumber, messageText, true)
                )
            } catch (_: Exception) {
                ActionExecutionResult.Error("WhatsApp does not appear to be installed on this device.")
            }
        }
    }

    /**
     * Opens Android System Settings.
     */
    fun openSettings(): ActionExecutionResult {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Success("Opening Settings...", DeviceAction.OpenSettings())
        } catch (e: Exception) {
            ActionExecutionResult.Error("Failed to open Settings: ${e.message}")
        }
    }

    /**
     * Opens a web link.
     */
    fun openWebUrl(url: String, title: String = "Website"): ActionExecutionResult {
        val properUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(properUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Success("Opening $title...", DeviceAction.OpenWeb(properUrl, title))
        } catch (e: Exception) {
            ActionExecutionResult.Error("Unable to open browser: ${e.message}")
        }
    }

    /**
     * Opens Maps navigation with destination query.
     */
    fun openNavigation(destination: String): ActionExecutionResult {
        val uri = Uri.parse("geo:0,0?q=${URLEncoder.encode(destination, "UTF-8")}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Success("Opening maps navigation for $destination...")
        } catch (e: Exception) {
            ActionExecutionResult.Error("Maps application not found: ${e.message}")
        }
    }
}

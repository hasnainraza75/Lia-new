package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.LocalCustomColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PhoneControlScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    val preferences by viewModel.preferences.collectAsState()

    // Real system permission states
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var contactsGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    var phoneGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
    }
    var notificationsGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // Permission Request Launchers
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
    }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        contactsGranted = granted
    }
    val phoneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        phoneGranted = granted
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsGranted = granted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.bgUpper,
                        colors.bgMain
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Phone Control",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Native Android integrations & permissions status",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Capabilities Overview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Direct System Execution",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${preferences.assistantName} performs genuine Android actions through system intents. You can say \"YouTube kholo\", \"Mummy ko call karo\", or \"Papa ko WhatsApp karo\" for instant execution.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Permissions Checklist
            SettingsSectionHeader(title = "Android Permissions Status")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PermissionStatusCard(
                    title = "Microphone Access",
                    subtitle = "Required for real-time natural voice conversation with ${preferences.assistantName}",
                    icon = Icons.Filled.Mic,
                    isGranted = micGranted,
                    onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )

                PermissionStatusCard(
                    title = "Contacts Directory",
                    subtitle = "Allows resolving names (e.g. \"Call Rahul\") without uploading address book",
                    icon = Icons.Filled.Contacts,
                    isGranted = contactsGranted,
                    onRequest = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
                )

                PermissionStatusCard(
                    title = "Phone Calling",
                    subtitle = "Enables launching Android phone dialer with number pre-filled",
                    icon = Icons.Filled.Phone,
                    isGranted = phoneGranted,
                    onRequest = { phoneLauncher.launch(Manifest.permission.CALL_PHONE) }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionStatusCard(
                        title = "Notifications",
                        subtitle = "Displays background session indicator and reminders",
                        icon = Icons.Filled.Notifications,
                        isGranted = notificationsGranted,
                        onRequest = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Default Digital Assistant Role Guidance
            SettingsSectionHeader(title = "Default Digital Assistant")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Assistant,
                                contentDescription = null,
                                tint = colors.accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Set as Assistant App",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You can set NOVA AI as your device's default digital assistant in Android Settings under Apps -> Default Apps -> Digital Assistant app.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan)
                    ) {
                        Text("Open Default Apps Settings", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // System App Info Button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open App Permissions in System Settings", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PermissionStatusCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    val colors = LocalCustomColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) colors.borderLavender else Color(0xFFEF4444).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) Color(0xFF4ADE80).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isGranted) Color(0xFF4ADE80) else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGranted) "Granted" else "Required",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) Color(0xFF4ADE80) else Color(0xFFEF4444),
                            modifier = Modifier
                                .background(
                                    (if (isGranted) Color(0xFF4ADE80) else Color(0xFFEF4444)).copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, fontSize = 11.sp, color = colors.textMuted)
                }
            }

            if (!isGranted) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Grant", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

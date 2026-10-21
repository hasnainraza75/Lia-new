package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.LocalCustomColors

@Composable
fun WebsitePreviewScreen(
    title: String,
    htmlCode: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Preview, 1: HTML Code

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
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = colors.textPrimary
                    )
                }

                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Website Code", htmlCode))
                    Toast.makeText(context, "HTML code copied to clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy Code", tint = colors.accentGold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Switcher Tabs: Live Preview vs Code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceCard)
                    .border(1.dp, colors.borderLavender, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) colors.accentGold else Color.Transparent)
                        .clickable { selectedTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Interactive Preview",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.Black else colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) colors.accentGold else Color.Transparent)
                        .clickable { selectedTab = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HTML / CSS Source",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.Black else colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content Area
            if (selectedTab == 0) {
                // Live Android WebView Rendering
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                            }
                        },
                        update = { view ->
                            view.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                        }
                    )
                }
            } else {
                // Raw HTML Source Code viewer
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    color = colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = htmlCode,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.accentCyan,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

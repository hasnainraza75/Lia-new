package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush as GBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.LivingAiOrb
import com.example.data.local.OrbState
import com.example.ui.theme.LocalCustomColors

@Composable
fun SketchPreviewScreen(
    initialPrompt: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    var promptInput by remember { mutableStateOf(initialPrompt) }
    var selectedStyle by remember { mutableStateOf("Cyberpunk Neon") }
    var isGenerating by remember { mutableStateOf(false) }

    val styles = listOf("Cyberpunk Neon", "Cosmic Galaxy", "Minimalist Vector", "3D Surreal Oil")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                GBrush.verticalGradient(
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
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Sketch Photo Studio",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Creative AI visual generation",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Art Canvas Preview Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = colors.surfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderLavender)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Stylized AI generative preview canvas
                    AsyncImage(
                        model = "file:///app/src/main/res/drawable/nova_app_icon.jpg",
                        contentDescription = "Generated Art",
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay watermark
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Style: $selectedStyle", fontSize = 10.sp, color = colors.accentGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Input
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("Visual Prompt Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Style Pills
            Text("Artistic Style", fontSize = 12.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { style ->
                    val isSel = selectedStyle == style
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedStyle = style },
                        color = if (isSel) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) colors.accentGold else colors.borderLavender)
                    ) {
                        Text(
                            text = style,
                            fontSize = 11.sp,
                            color = if (isSel) colors.accentGold else colors.textMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Generate & Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Rendering artwork with style: $selectedStyle", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan)
                ) {
                    Icon(imageVector = Icons.Filled.Brush, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Regenerate", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Artwork saved to local gallery", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold)
                ) {
                    Icon(imageVector = Icons.Filled.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Art", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

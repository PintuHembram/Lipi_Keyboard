package com.lipi.keyboard.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipi.keyboard.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedTheme by remember { mutableStateOf(SettingsManager.getTheme(context)) }
    var selectedLayout by remember { mutableStateOf(SettingsManager.getDefaultLayoutName(context)) }
    var keyboardHeightScale by remember { mutableFloatStateOf(SettingsManager.getHeightScale(context)) }
    var hapticFeedbackEnabled by remember { mutableStateOf(SettingsManager.isHapticEnabled(context)) }
    var soundFeedbackEnabled by remember { mutableStateOf(SettingsManager.isSoundEnabled(context)) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Keyboard Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. Keyboard Themes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🎨 Keyboard Theme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val themes = listOf(
                        "Dark AMOLED" to Color(0xFF0B1918),
                        "Emerald Green" to Color(0xFF004D40),
                        "Royal Blue" to Color(0xFF0A192F),
                        "Classic Light" to Color(0xFFF1F3F4)
                    )

                    themes.forEach { (themeName, themeColor) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = themeName
                                    SettingsManager.setTheme(context, themeName)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedTheme == themeName),
                                onClick = {
                                    selectedTheme = themeName
                                    SettingsManager.setTheme(context, themeName)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(themeColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(themeName, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 2. Layouts Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⌨️ Default Language Layout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val layouts = listOf(
                        "Odia Transliteration" to "Phonetic Latin to Odia",
                        "𑢹𑢺 Warang Chiti (Ho)" to "Native Ho alphabet & numerals",
                        "Odia InScript" to "Standard national InScript arrangement",
                        "English (US)" to "Standard QWERTY layout"
                    )

                    layouts.forEach { (name, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLayout = name
                                    SettingsManager.setDefaultLayoutName(context, name)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedLayout == name),
                                onClick = {
                                    selectedLayout = name
                                    SettingsManager.setDefaultLayoutName(context, name)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(desc, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 3. Keyboard Height Scale
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📐 Keyboard Height",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "${keyboardHeightScale.toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Adjust the vertical height of keyboard rows (80% – 120%).",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = keyboardHeightScale,
                        onValueChange = {
                            keyboardHeightScale = it
                            SettingsManager.setHeightScale(context, it)
                        },
                        valueRange = 80f..120f,
                        steps = 7
                    )
                }
            }

            // 4. Sound & Vibrations
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔔 Sound & Vibrations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keypress Haptic Feedback", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Vibrate phone slightly on key press", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = {
                                hapticFeedbackEnabled = it
                                SettingsManager.setHapticEnabled(context, it)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keypress Sound Effect", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Play key click sound on key press", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = soundFeedbackEnabled,
                            onCheckedChange = {
                                soundFeedbackEnabled = it
                                SettingsManager.setSoundEnabled(context, it)
                            }
                        )
                    }
                }
            }

            // 5. Help, Complaints & Suggestions via Email
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📩 Help, Complaints & Suggestions",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Have a question, complaint, feature request, or suggestion? Email us directly and our team will assist you!",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:pintuh157@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Lipi Keyboard - Feedback / Complaint / Suggestion")
                                putExtra(Intent.EXTRA_TEXT, "Hello Lipi Keyboard Team,\n\nI would like to share the following feedback:\n\n")
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Send Email via..."))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Email: pintuh157@gmail.com", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✉️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Email to pintuh157@gmail.com")
                    }
                }
            }

            // Developer & Version Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Lipi Keyboard v1.1 (Build 2)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Developed by Pintu Hembram",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        "HEMBRAM IT SOLUTIONS PVT. LTD. Tech Team",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

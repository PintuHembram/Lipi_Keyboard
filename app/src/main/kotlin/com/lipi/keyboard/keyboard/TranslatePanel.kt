package com.lipi.keyboard.keyboard

import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipi.keyboard.translate.TranslateResult
import com.lipi.keyboard.translate.TranslationFactory
import com.lipi.keyboard.translate.TranslationProvider
import kotlinx.coroutines.launch

@Composable
fun TranslatePanel(
    connectionProvider: () -> InputConnection?,
    activeLayoutId: String,
    onClosePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val provider: TranslationProvider = remember { TranslationFactory.createProvider(context) }

    var prefilledLength by remember { mutableStateOf(0) }
    var sourceText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var unsupportedNote by remember { mutableStateOf<String?>(null) }
    var isDownloadNeeded by remember { mutableStateOf(false) }

    var sourceLang by remember { mutableStateOf("en") }
    var targetLang by remember { mutableStateOf("hi") }

    val supportedLanguages = remember { provider.supportedLanguages() }

    // Prefill source text from cursor or selection
    LaunchedEffect(Unit) {
        val ic = connectionProvider()
        if (ic != null) {
            val selectedText = ic.getSelectedText(0)?.toString()
            if (!selectedText.isNullOrEmpty()) {
                sourceText = selectedText
                prefilledLength = selectedText.length
            } else {
                val beforeCursor = ic.getTextBeforeCursor(500, 0)?.toString() ?: ""
                val trimmed = beforeCursor.split("\n").lastOrNull()?.trim() ?: beforeCursor.trim()
                sourceText = trimmed
                prefilledLength = trimmed.length
            }
        }
    }

    // Check for unsupported active language note
    LaunchedEffect(activeLayoutId) {
        if (activeLayoutId == "warang_chiti") {
            unsupportedNote = "On-device translation is not available for Warang Chiti. Pre-selected English instead."
            sourceLang = "en"
        } else if (activeLayoutId == "odia_translit" || activeLayoutId == "odia_inscript") {
            unsupportedNote = "On-device translation is not available for Odia. Pre-selected English instead."
            sourceLang = "en"
        } else {
            unsupportedNote = null
        }
    }

    // Run translation logic
    fun runTranslate() {
        if (sourceText.isBlank()) {
            translatedText = ""
            return
        }
        scope.launch {
            isLoading = true
            errorMessage = null
            isDownloadNeeded = false

            when (val result = provider.translate(sourceText, sourceLang, targetLang)) {
                is TranslateResult.Success -> {
                    translatedText = result.translatedText
                    isLoading = false
                }
                is TranslateResult.ModelMissing -> {
                    isLoading = false
                    isDownloadNeeded = true
                    errorMessage = "Language pack required (~30 MB). Download over Wi-Fi?"
                }
                is TranslateResult.Unsupported -> {
                    isLoading = false
                    unsupportedNote = result.note
                }
                is TranslateResult.NetworkError -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is TranslateResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Header & Language Dropdowns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🌐 Translate", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Source Dropdown
                var sourceExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { sourceExpanded = true }) {
                        Text(sourceLang.uppercase(), fontSize = 12.sp)
                    }
                    DropdownMenu(expanded = sourceExpanded, onDismissRequest = { sourceExpanded = false }) {
                        supportedLanguages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text("$name ($code)") },
                                onClick = {
                                    sourceLang = code
                                    sourceExpanded = false
                                    runTranslate()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Swap Button
                OutlinedButton(onClick = {
                    val temp = sourceLang
                    sourceLang = targetLang
                    targetLang = temp
                    runTranslate()
                }) {
                    Text("⇄", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Target Dropdown
                var targetExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { targetExpanded = true }) {
                        Text(targetLang.uppercase(), fontSize = 12.sp)
                    }
                    DropdownMenu(expanded = targetExpanded, onDismissRequest = { targetExpanded = false }) {
                        supportedLanguages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text("$name ($code)") },
                                onClick = {
                                    targetLang = code
                                    targetExpanded = false
                                    runTranslate()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Unsupported Language Warning Card
        unsupportedNote?.let { note ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(note, modifier = Modifier.padding(8.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        // Source Text Input
        OutlinedTextField(
            value = sourceText,
            onValueChange = {
                if (it.length <= 2000) {
                    sourceText = it
                    runTranslate()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type or paste text to translate…") },
            maxLines = 3
        )

        // Result & Progress
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Translating…", fontSize = 12.sp)
                }
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            } else {
                Text(
                    text = translatedText.ifEmpty { "Translation result will appear here" },
                    fontSize = 14.sp,
                    fontWeight = if (translatedText.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (translatedText.isNotEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
                )
            }
        }

        // Download Model Button if missing
        if (isDownloadNeeded) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val sDownloaded = provider.downloadModel(sourceLang, wifiOnly = true)
                        val tDownloaded = provider.downloadModel(targetLang, wifiOnly = true)
                        isLoading = false
                        if (sDownloaded && tDownloaded) {
                            runTranslate()
                        } else {
                            errorMessage = "Download failed. Check network."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("⬇️ Download Language Pack (~30 MB)")
            }
        }

        // Action Buttons: Insert, Replace, Copy, ABC
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = {
                    val ic = connectionProvider()
                    if (ic != null && translatedText.isNotEmpty()) {
                        ic.commitText(translatedText, 1)
                    }
                },
                enabled = translatedText.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Insert", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val ic = connectionProvider()
                    if (ic != null && translatedText.isNotEmpty()) {
                        ic.beginBatchEdit()
                        if (prefilledLength > 0) {
                            ic.deleteSurroundingText(prefilledLength, 0)
                        }
                        ic.commitText(translatedText, 1)
                        ic.endBatchEdit()
                    }
                },
                enabled = translatedText.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Replace", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    if (translatedText.isNotEmpty()) {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText("Translated", translatedText))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = translatedText.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Copy", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onClosePanel,
                modifier = Modifier.weight(1f)
            ) {
                Text("ABC", fontSize = 12.sp)
            }
        }
    }
}

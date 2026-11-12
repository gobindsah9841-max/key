package com.example.loksewastudybook.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.loksewastudybook.data.BookIndex
import com.example.loksewastudybook.data.BookRepository
import com.example.loksewastudybook.data.ColorTheme
import com.example.loksewastudybook.data.ReaderFontSize
import com.example.loksewastudybook.ui.components.AppTopBar
import com.example.loksewastudybook.ui.theme.WrongRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    indexData: BookIndex,
    repository: BookRepository,
    onNavigateBack: () -> Unit,
    onThemeChanged: (ColorTheme) -> Unit,
    onOpenAboutPart: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var selectedFontSize by remember { mutableStateOf(repository.fontSize) }
    var selectedColorTheme by remember { mutableStateOf(repository.colorTheme) }
    var hideAnswers by remember { mutableStateOf(repository.hideAnswersInReader) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf(false) }

    val triedQuestionsCount = remember { repository.getTotalQuestionsTried() }
    val savedTestsCount = remember { repository.getTestRecords().size }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showSearch = false,
                showSettings = false
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Text Size
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reader Text Size",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ReaderFontSize.entries.forEach { fs ->
                                FilterChip(
                                    selected = selectedFontSize == fs,
                                    onClick = {
                                        selectedFontSize = fs
                                        repository.fontSize = fs
                                    },
                                    label = { Text(fs.displayName) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("font_chip_${fs.id}")
                                )
                            }
                        }
                    }
                }
            }

            // Color Themes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Color Theme",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ColorTheme.entries.forEach { theme ->
                                FilterChip(
                                    selected = selectedColorTheme == theme,
                                    onClick = {
                                        selectedColorTheme = theme
                                        repository.colorTheme = theme
                                        onThemeChanged(theme)
                                    },
                                    label = { Text(theme.displayName) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("theme_chip_${theme.id}")
                                )
                            }
                        }
                    }
                }
            }

            // Answers While Reading
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Answers While Reading",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = hideAnswers,
                                onClick = {
                                    hideAnswers = true
                                    repository.hideAnswersInReader = true
                                },
                                label = { Text("Hide until I tap") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("hide_answers_chip_true")
                            )
                            FilterChip(
                                selected = !hideAnswers,
                                onClick = {
                                    hideAnswers = false
                                    repository.hideAnswersInReader = false
                                },
                                label = { Text("Always show") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hide_answers_chip_false")
                            )
                        }
                    }
                }
            }

            // Progress & Data Reset
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "My Progress on this Device",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$triedQuestionsCount of ${indexData.mcqTotal} questions tried · $savedTestsCount timed test(s) saved on this phone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("erase_scores_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = WrongRed
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Erase scores and reading positions")
                        }

                        if (resetSuccessMessage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All test scores and reading positions have been reset.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WrongRed
                            )
                        }
                    }
                }
            }

            // About Book
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "About Loksewa Study Book",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Version ${indexData.version} · Built on ${indexData.built}.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Parts ${indexData.firstPart}–${indexData.lastPart} of the study book for the provincial Public Service Commission 5th Level (non-technical) Paper I: ${indexData.mcqTotal} MCQs, ${indexData.saqTotal} short-answer questions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The application works fully offline with zero network requests and asks for no device permissions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenAboutPart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("read_about_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Read \"About this book\"")
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Erase All Data?") },
            text = { Text("This erases all your scores, saved mock test results, and reading positions on this phone. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.resetAllUserData()
                        showResetDialog = false
                        resetSuccessMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("Erase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

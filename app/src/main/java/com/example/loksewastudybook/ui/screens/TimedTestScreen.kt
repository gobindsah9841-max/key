package com.example.loksewastudybook.ui.screens

import android.text.Html
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loksewastudybook.data.BookIndex
import com.example.loksewastudybook.data.BookRepository
import com.example.loksewastudybook.data.QuizItem
import com.example.loksewastudybook.data.TestRecord
import com.example.loksewastudybook.ui.components.AppTopBar
import com.example.loksewastudybook.ui.theme.WarningAmber
import com.example.loksewastudybook.ui.theme.WrongRed
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val letters = listOf("a", "b", "c", "d")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimedTestScreen(
    indexData: BookIndex,
    repository: BookRepository,
    onNavigateBack: () -> Unit,
    onFinishTest: (items: List<QuizItem>, picks: List<Int>, durationSec: Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // Test Setup State
    var isTestActive by remember { mutableStateOf(false) }
    var selectedQuestionCount by remember { mutableIntStateOf(25) }
    var isTimed by remember { mutableStateOf(true) }
    val selectedChapters = remember {
        mutableStateListOf<Int>().apply {
            addAll(indexData.chapters)
        }
    }

    // Active Test State
    var testItems by remember { mutableStateOf<List<QuizItem>>(emptyList()) }
    val userPicks = remember { mutableStateListOf<Int>() }
    var currentItemIndex by remember { mutableIntStateOf(0) }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    var totalTimeSeconds by remember { mutableIntStateOf(0) }
    var showBlankWarningDialog by remember { mutableStateOf(false) }
    var showQuitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (isTestActive) {
            showQuitConfirmDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Timer countdown effect
    LaunchedEffect(isTestActive, isTimed, remainingSeconds) {
        if (isTestActive && isTimed && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            if (remainingSeconds <= 0) {
                // Auto submit on time expiry
                onFinishTest(testItems, userPicks.toList(), totalTimeSeconds)
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isTestActive) "Timed Test" else "Mock Test Setup",
                canNavigateBack = true,
                onNavigateBack = {
                    if (isTestActive) showQuitConfirmDialog = true else onNavigateBack()
                },
                showSearch = !isTestActive,
                onSearchClick = onOpenSearch,
                showSettings = !isTestActive,
                onSettingsClick = onOpenSettings
            )
        }
    ) { innerPadding ->
        if (!isTestActive) {
            // Setup Screen
            val pastTests = remember { repository.getTestRecords() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("timed_test_setup_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Official Marking Note
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loksewa Official Marking Scheme",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "A mixed test from the chapters you select. Marking strictly follows the official examination: +2 marks for each correct answer, −0.4 marks deducted for each wrong answer, and nothing for a blank.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Question Count Selector
                item {
                    Text(
                        text = "Number of Questions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(10, 25, 50).forEach { count ->
                            FilterChip(
                                selected = selectedQuestionCount == count,
                                onClick = { selectedQuestionCount = count },
                                label = { Text("$count Questions") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("count_chip_$count")
                            )
                        }
                    }
                }

                // Time Limit Selector
                item {
                    Text(
                        text = "Time Limit",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = isTimed,
                            onClick = { isTimed = true },
                            label = { Text("54s / question (Official)") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("time_chip_timed")
                        )
                        FilterChip(
                            selected = !isTimed,
                            onClick = { isTimed = false },
                            label = { Text("No limit") },
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("time_chip_untimed")
                        )
                    }
                }

                // Chapters Filter
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Include Chapters (${selectedChapters.size}/${indexData.chapters.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = {
                            if (selectedChapters.size == indexData.chapters.size) {
                                selectedChapters.clear()
                                selectedChapters.add(1)
                            } else {
                                selectedChapters.clear()
                                selectedChapters.addAll(indexData.chapters)
                            }
                        }) {
                            Text(if (selectedChapters.size == indexData.chapters.size) "Clear" else "Select All")
                        }
                    }
                }

                val chapterParts = indexData.parts.filter { it.kind == "chapter" }
                items(chapterParts.size) { i ->
                    val p = chapterParts[i]
                    val isChecked = selectedChapters.contains(p.ch)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (isChecked) {
                                    if (selectedChapters.size > 1) selectedChapters.remove(p.ch)
                                } else {
                                    selectedChapters.add(p.ch)
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { check ->
                                if (check) selectedChapters.add(p.ch)
                                else if (selectedChapters.size > 1) selectedChapters.remove(p.ch)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chapter ${p.ch} — ${p.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Start Test Button
                item {
                    Button(
                        onClick = {
                            // Gather questions from selected chapters
                            val pool = mutableListOf<QuizItem>()
                            for (ch in selectedChapters) {
                                val quiz = repository.getChapterQuiz(ch)
                                for (q in quiz.allQuestions) {
                                    pool.add(QuizItem(ch = ch, question = q))
                                }
                            }
                            pool.shuffle()
                            val count = minOf(selectedQuestionCount, pool.size)
                            testItems = pool.take(count)
                            userPicks.clear()
                            repeat(count) { userPicks.add(-1) }
                            currentItemIndex = 0
                            val totalSec = if (isTimed) count * 54 else 0
                            totalTimeSeconds = totalSec
                            remainingSeconds = totalSec
                            isTestActive = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_test_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start the Test ($selectedQuestionCount Questions)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Previous Tests Section
                if (pastTests.isNotEmpty()) {
                    item {
                        Text(
                            text = "Your Recent Test Results",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    items(pastTests.reversed().take(5).size) { idx ->
                        val t = pastTests.reversed()[idx]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = t.date,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${t.totalQuestions} Questions · ${t.correct} Right · ${t.wrong} Wrong · ${t.blank} Blank",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (t.marks >= (t.maxMarks * 0.4)) MaterialTheme.colorScheme.primaryContainer else WrongRed.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "${t.marks} / ${t.maxMarks}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (t.marks >= (t.maxMarks * 0.4)) MaterialTheme.colorScheme.primary else WrongRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Active Test Mode
            val totalQuestions = testItems.size
            val currentItem = testItems[currentItemIndex]
            val currentPick = userPicks[currentItemIndex]
            val answeredCount = userPicks.count { it >= 0 }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("active_test_screen"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Indicator Row: Count & Timer
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentItemIndex + 1} of $totalQuestions · $answeredCount answered",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isTimed) {
                            val mins = remainingSeconds / 60
                            val secs = remainingSeconds % 60
                            val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)
                            val isLowTime = remainingSeconds <= 60

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isLowTime) WrongRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Timer",
                                        tint = if (isLowTime) WrongRed else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (isLowTime) WrongRed else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Question Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_question_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Chapter ${currentItem.ch} · ${currentItem.question.id}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (currentItem.question.label.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = currentItem.question.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val cleanStem = remember(currentItem.question.stem) {
                                Html.fromHtml(currentItem.question.stem, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                            }
                            Text(
                                text = cleanStem,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Options
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                currentItem.question.options.forEachIndexed { optIndex, optionText ->
                                    val cleanOptText = remember(optionText) {
                                        Html.fromHtml(optionText, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                    }
                                    val isSelected = currentPick == optIndex
                                    val letter = letters.getOrElse(optIndex) { "$optIndex" }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                userPicks[currentItemIndex] = optIndex
                                            }
                                            .padding(14.dp)
                                            .testTag("test_opt_$optIndex"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = letter.uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = cleanOptText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Prev / Leave Blank / Next Navigation Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (currentItemIndex > 0) currentItemIndex-- },
                            enabled = currentItemIndex > 0,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }

                        TextButton(
                            onClick = { userPicks[currentItemIndex] = -1 },
                            enabled = currentPick >= 0
                        ) {
                            Text("Leave blank")
                        }

                        Button(
                            onClick = { if (currentItemIndex + 1 < totalQuestions) currentItemIndex++ },
                            enabled = currentItemIndex + 1 < totalQuestions,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Question Palette Grid (1..N)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Question Palette",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (k in 0 until totalQuestions) {
                                    val isAnswered = userPicks[k] >= 0
                                    val isCurrent = k == currentItemIndex

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when {
                                                    isCurrent -> MaterialTheme.colorScheme.primary
                                                    isAnswered -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { currentItemIndex = k },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${k + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent || isAnswered) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isCurrent -> MaterialTheme.colorScheme.onPrimary
                                                isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = {
                            val blankCount = userPicks.count { it < 0 }
                            if (blankCount > 0) {
                                showBlankWarningDialog = true
                            } else {
                                onFinishTest(testItems, userPicks.toList(), totalTimeSeconds)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("finish_test_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "Finish Test & See Result",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Confirmation when blanks exist
    if (showBlankWarningDialog) {
        val blankCount = userPicks.count { it < 0 }
        AlertDialog(
            onDismissRequest = { showBlankWarningDialog = false },
            title = { Text("$blankCount Question(s) Left Blank") },
            text = {
                Text("In Loksewa exams, a blank answer costs 0 marks, whereas each wrong answer costs −0.4 marks. Are you sure you want to finish now?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlankWarningDialog = false
                        onFinishTest(testItems, userPicks.toList(), totalTimeSeconds)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Finish Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlankWarningDialog = false }) {
                    Text("Review Questions")
                }
            }
        )
    }

    // Quit test dialog
    if (showQuitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showQuitConfirmDialog = false },
            title = { Text("Quit Test?") },
            text = { Text("Are you sure you want to exit the current test? Your progress will not be saved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showQuitConfirmDialog = false
                        isTestActive = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text("Quit Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitConfirmDialog = false }) {
                    Text("Continue Test")
                }
            }
        )
    }
}

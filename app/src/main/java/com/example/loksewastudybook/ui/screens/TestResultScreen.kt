package com.example.loksewastudybook.ui.screens

import android.text.Html
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loksewastudybook.data.QuizItem
import com.example.loksewastudybook.data.QuizQuestion
import com.example.loksewastudybook.ui.components.AppTopBar
import com.example.loksewastudybook.ui.theme.CorrectGreen
import com.example.loksewastudybook.ui.theme.CorrectGreenBg
import com.example.loksewastudybook.ui.theme.WarningAmber
import com.example.loksewastudybook.ui.theme.WrongRed
import com.example.loksewastudybook.ui.theme.WrongRedBg

private val letters = listOf("a", "b", "c", "d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    items: List<QuizItem>,
    picks: List<Int>,
    isTimedExamMode: Boolean = true,
    onNavigateBack: () -> Unit,
    onRetryAll: () -> Unit,
    onRetryMissed: (missedQuestions: List<QuizQuestion>) -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    val total = items.size
    val correct = items.indices.count { picks.getOrNull(it) == items[it].question.answerIndex }
    val wrong = items.indices.count { val p = picks.getOrNull(it) ?: -1; p >= 0 && p != items[it].question.answerIndex }
    val blank = items.indices.count { (picks.getOrNull(it) ?: -1) < 0 }

    val rawMarks = (2.0 * correct) - (0.4 * wrong)
    val marks = Math.round(rawMarks * 10.0) / 10.0
    val maxMarks = 2 * total
    val percentage = if (maxMarks > 0) Math.max(0, Math.round((marks / maxMarks) * 100).toInt()) else 0

    val missedItems = items.filterIndexed { index, item ->
        val p = picks.getOrNull(index) ?: -1
        p != item.question.answerIndex
    }.map { it.question }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Test Results",
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
                .testTag("test_results_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("score_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (marks >= maxMarks * 0.4) MaterialTheme.colorScheme.primaryContainer else WrongRedBg.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTimedExamMode) "$marks / $maxMarks" else "$correct of $total",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (marks >= maxMarks * 0.4) MaterialTheme.colorScheme.primary else WrongRed
                        )
                        Text(
                            text = if (isTimedExamMode) "$percentage percent" else "right at first try",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (isTimedExamMode) {
                            Text(
                                text = "$correct right × 2 = ${correct * 2}  ·  $wrong wrong × 0.4 = −${Math.round(wrong * 0.4 * 10.0) / 10.0}  ·  $blank blank",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Marked as in official exam: 2 marks for correct, −0.4 deducted for wrong, 0 for blank.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "$correct Correct · $wrong Incorrect",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRetryAll,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("retry_all_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry All")
                    }

                    OutlinedButton(
                        onClick = { onRetryMissed(missedItems) },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("retry_missed_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = missedItems.isNotEmpty()
                    ) {
                        Text("Only Missed (${missedItems.size})")
                    }
                }
            }

            // Review Section Header
            item {
                Text(
                    text = "Question Review (${items.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Questions Review List
            itemsIndexed(items) { index, item ->
                val q = item.question
                val pick = picks.getOrNull(index) ?: -1
                val isCorrect = pick == q.answerIndex
                val isBlank = pick < 0

                val statusColor = when {
                    isCorrect -> CorrectGreen
                    isBlank -> WarningAmber
                    else -> WrongRed
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_card_$index"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${index + 1} · Chapter ${item.ch} · ${q.id}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = when {
                                        isCorrect -> "Correct (+2.0)"
                                        isBlank -> "Blank (0.0)"
                                        else -> "Wrong (−0.4)"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val cleanStem = remember(q.stem) {
                            Html.fromHtml(q.stem, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                        }
                        Text(
                            text = cleanStem,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Options review
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            q.options.forEachIndexed { optIndex, optionText ->
                                val cleanOpt = remember(optionText) {
                                    Html.fromHtml(optionText, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                }
                                val letter = letters.getOrElse(optIndex) { "$optIndex" }
                                val isSelected = pick == optIndex
                                val isAnswer = optIndex == q.answerIndex

                                val optBg = when {
                                    isAnswer -> CorrectGreenBg
                                    isSelected -> WrongRedBg
                                    else -> Color.Transparent
                                }

                                val optBorder = when {
                                    isAnswer -> CorrectGreen
                                    isSelected -> WrongRed
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(optBg)
                                        .border(1.dp, optBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            isAnswer -> CorrectGreen
                                            isSelected -> WrongRed
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isAnswer) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            } else if (isSelected) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            } else {
                                                Text(text = letter.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = cleanOpt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Explanation card
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Answer: (${letters.getOrElse(q.answerIndex) { "" }.uppercase()})" +
                                            if (isBlank) " (You left this blank)" else "",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val cleanExp = remember(q.explanation) {
                                    Html.fromHtml(q.explanation, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                }
                                Text(
                                    text = cleanExp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

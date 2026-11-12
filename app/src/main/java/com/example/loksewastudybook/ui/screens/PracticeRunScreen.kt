package com.example.loksewastudybook.ui.screens

import android.text.Html
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.loksewastudybook.data.BookRepository
import com.example.loksewastudybook.data.QuizQuestion
import com.example.loksewastudybook.ui.components.AppTopBar
import com.example.loksewastudybook.ui.theme.CorrectGreen
import com.example.loksewastudybook.ui.theme.CorrectGreenBg
import com.example.loksewastudybook.ui.theme.WrongRed
import com.example.loksewastudybook.ui.theme.WrongRedBg

private val letters = listOf("a", "b", "c", "d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeRunScreen(
    ch: Int,
    title: String,
    questions: List<QuizQuestion>,
    repository: BookRepository,
    onNavigateBack: () -> Unit,
    onFinishPractice: (total: Int, correct: Int, wrongQuestions: List<QuizQuestion>) -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember(currentIndex) { mutableIntStateOf(-1) }
    var correctCount by remember { mutableIntStateOf(0) }
    val wrongQuestionsList = remember { mutableListOf<QuizQuestion>() }

    if (questions.isEmpty()) {
        onNavigateBack()
        return
    }

    val currentQuestion = questions[currentIndex]

    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
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
                .testTag("practice_run_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${questions.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CorrectGreenBg
                    ) {
                        Text(
                            text = "$correctCount right so far",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CorrectGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Question Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("practice_question_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Tag / Meta row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chapter $ch · ${currentQuestion.id}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentQuestion.label.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = currentQuestion.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stem
                        val cleanStem = remember(currentQuestion.stem) {
                            Html.fromHtml(currentQuestion.stem, Html.FROM_HTML_MODE_LEGACY).toString().trim()
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

                        // 4 Option Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            currentQuestion.options.forEachIndexed { optIndex, optionText ->
                                val cleanOptText = remember(optionText) {
                                    Html.fromHtml(optionText, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                }
                                val letter = letters.getOrElse(optIndex) { "$optIndex" }
                                val isSelected = selectedOption == optIndex
                                val isCorrect = optIndex == currentQuestion.answerIndex
                                val isRevealed = selectedOption != -1

                                val (bgColor, borderColor, textColor, badgeBg, badgeColor) = when {
                                    isRevealed && isCorrect -> {
                                        listOf(
                                            CorrectGreenBg,
                                            CorrectGreen,
                                            Color(0xFF14532D),
                                            CorrectGreen,
                                            Color.White
                                        )
                                    }
                                    isRevealed && isSelected && !isCorrect -> {
                                        listOf(
                                            WrongRedBg,
                                            WrongRed,
                                            Color(0xFF7F1D1D),
                                            WrongRed,
                                            Color.White
                                        )
                                    }
                                    else -> {
                                        listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            MaterialTheme.colorScheme.onSurface,
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .border(
                                            width = if (isRevealed && (isCorrect || isSelected)) 1.5.dp else 1.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = !isRevealed) {
                                            selectedOption = optIndex
                                            val ok = optIndex == currentQuestion.answerIndex
                                            if (ok) {
                                                correctCount++
                                            } else {
                                                wrongQuestionsList.add(currentQuestion)
                                            }
                                            repository.recordQuestionAttempt(ch, currentQuestion.id, ok)
                                        }
                                        .padding(14.dp)
                                        .testTag("practice_opt_$optIndex"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = badgeBg,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isRevealed && isCorrect) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Correct",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else if (isRevealed && isSelected && !isCorrect) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Wrong",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = letter.uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = badgeColor
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = cleanOptText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Explanation banner when revealed
                        if (selectedOption != -1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val isCorrectAnswer = selectedOption == currentQuestion.answerIndex
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCorrectAnswer) CorrectGreenBg.copy(alpha = 0.5f) else WrongRedBg.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Answer: (${letters.getOrElse(currentQuestion.answerIndex) { "" }.uppercase()})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCorrectAnswer) CorrectGreen else WrongRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val cleanExplanation = remember(currentQuestion.explanation) {
                                        Html.fromHtml(currentQuestion.explanation, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                    }
                                    Text(
                                        text = cleanExplanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Next / Finish Button
            if (selectedOption != -1) {
                item {
                    val isLast = currentIndex + 1 >= questions.size
                    Button(
                        onClick = {
                            if (isLast) {
                                onFinishPractice(questions.size, correctCount, wrongQuestionsList)
                            } else {
                                currentIndex++
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("practice_next_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isLast) "See Results" else "Next Question",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }

            // Disclaimer Note
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Practice question written for this book. It is not a past examination question.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

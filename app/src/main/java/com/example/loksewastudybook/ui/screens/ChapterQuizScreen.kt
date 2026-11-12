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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.loksewastudybook.data.BookIndex
import com.example.loksewastudybook.data.BookRepository
import com.example.loksewastudybook.data.QuizQuestion
import com.example.loksewastudybook.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterQuizScreen(
    ch: Int,
    indexData: BookIndex,
    repository: BookRepository,
    onNavigateBack: () -> Unit,
    onStartPractice: (title: String, questions: List<QuizQuestion>) -> Unit,
    onReadChapter: (partIndex: Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    val chapterPart = indexData.parts.find { it.kind == "chapter" && it.ch == ch }
    val quizData = remember(ch) { repository.getChapterQuiz(ch) }
    val allQuestions = quizData.allQuestions
    val wrongQuestions = remember(ch) { repository.getQuestionsWrong(ch, allQuestions) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Chapter $ch — Practice",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showSearch = true,
                onSearchClick = onOpenSearch,
                showSettings = true,
                onSettingsClick = onOpenSettings
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("chapter_quiz_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chapter Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Chapter $ch",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = quizData.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (chapterPart != null && chapterPart.nep.isNotBlank()) {
                            Text(
                                text = chapterPart.nep,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Quick Run Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val shuffled = allQuestions.shuffled()
                            onStartPractice("Chapter $ch — All Questions", shuffled)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_all_questions_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("All ${allQuestions.size} Questions, Mixed")
                    }

                    OutlinedButton(
                        onClick = {
                            if (wrongQuestions.isNotEmpty()) {
                                onStartPractice("Chapter $ch — Questions I Got Wrong", wrongQuestions)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_wrong_questions_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = wrongQuestions.isNotEmpty()
                    ) {
                        Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Questions I Got Wrong (${wrongQuestions.size})")
                    }

                    if (chapterPart != null) {
                        OutlinedButton(
                            onClick = { onReadChapter(chapterPart.index) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chapter_read_link_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Book, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Read this chapter")
                        }
                    }
                }
            }

            // Section: By Topic
            item {
                Text(
                    text = "By Topic",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            items(quizData.topics) { topic ->
                val triedCount = topic.questions.count { repository.isQuestionAttempted(ch, it.id) }
                val rightCount = topic.questions.count { repository.isQuestionLastCorrect(ch, it.id) == true }

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val title = if (topic.id == "T") "Chapter $ch — Top 25" else "Topic ${topic.id}"
                            onStartPractice(title, topic.questions)
                        }
                        .testTag("topic_item_${topic.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = (if (topic.id == "T") "" else "${topic.id} ") + topic.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val statsText = "${topic.questions.size} questions" +
                                    if (triedCount > 0) " · last time $rightCount of $triedCount right" else ""
                            Text(
                                text = statsText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

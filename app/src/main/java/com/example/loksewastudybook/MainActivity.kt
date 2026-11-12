package com.example.loksewastudybook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.loksewastudybook.data.BookIndex
import com.example.loksewastudybook.data.BookRepository
import com.example.loksewastudybook.data.ColorTheme
import com.example.loksewastudybook.data.QuizItem
import com.example.loksewastudybook.data.QuizQuestion
import com.example.loksewastudybook.data.TestRecord
import com.example.loksewastudybook.ui.screens.ChapterQuizScreen
import com.example.loksewastudybook.ui.screens.HomeScreen
import com.example.loksewastudybook.ui.screens.PracticeRunScreen
import com.example.loksewastudybook.ui.screens.QuizHomeScreen
import com.example.loksewastudybook.ui.screens.ReaderScreen
import com.example.loksewastudybook.ui.screens.SearchScreen
import com.example.loksewastudybook.ui.screens.SettingsScreen
import com.example.loksewastudybook.ui.screens.TestResultScreen
import com.example.loksewastudybook.ui.screens.TimedTestScreen
import com.example.loksewastudybook.ui.theme.LoksewaStudyBookTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class Screen {
    data object Home : Screen()
    data class Reader(val partIndex: Int, val headingId: String? = null) : Screen()
    data object QuizHome : Screen()
    data class ChapterQuiz(val ch: Int) : Screen()
    data class PracticeRun(val ch: Int, val title: String, val questions: List<QuizQuestion>) : Screen()
    data object TimedTest : Screen()
    data class TestResult(
        val items: List<QuizItem>,
        val picks: List<Int>,
        val isTimedExamMode: Boolean
    ) : Screen()
    data object Search : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: BookRepository
    private lateinit var bookIndex: BookIndex

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = BookRepository(applicationContext)
        bookIndex = repository.getIndex()

        setContent {
            var currentTheme by remember { mutableStateOf(repository.colorTheme) }

            LoksewaStudyBookTheme(selectedTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoksewaApp(
                        indexData = bookIndex,
                        repository = repository,
                        onThemeChanged = { newTheme ->
                            currentTheme = newTheme
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoksewaApp(
    indexData: BookIndex,
    repository: BookRepository,
    onThemeChanged: (ColorTheme) -> Unit
) {
    val navStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = navStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        navStack.add(screen)
    }

    fun navigateBack() {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.size - 1)
        }
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            is Screen.Home -> {
                HomeScreen(
                    indexData = indexData,
                    repository = repository,
                    onOpenPart = { partIndex ->
                        navigateTo(Screen.Reader(partIndex))
                    },
                    onOpenQuizHome = {
                        navigateTo(Screen.QuizHome)
                    },
                    onOpenChapterQuiz = { ch ->
                        navigateTo(Screen.ChapterQuiz(ch))
                    },
                    onOpenTimedTest = {
                        navigateTo(Screen.TimedTest)
                    },
                    onOpenSearch = {
                        navigateTo(Screen.Search)
                    },
                    onOpenSettings = {
                        navigateTo(Screen.Settings)
                    }
                )
            }

            is Screen.Reader -> {
                ReaderScreen(
                    partIndex = screen.partIndex,
                    initialHeadingId = screen.headingId,
                    indexData = indexData,
                    repository = repository,
                    onNavigateBack = { navigateBack() },
                    onOpenPart = { nextPartIndex ->
                        navStack.removeAt(navStack.size - 1)
                        navStack.add(Screen.Reader(nextPartIndex))
                    },
                    onOpenChapterQuiz = { ch ->
                        navigateTo(Screen.ChapterQuiz(ch))
                    },
                    onOpenSearch = {
                        navigateTo(Screen.Search)
                    },
                    onOpenSettings = {
                        navigateTo(Screen.Settings)
                    }
                )
            }

            is Screen.QuizHome -> {
                QuizHomeScreen(
                    indexData = indexData,
                    repository = repository,
                    onNavigateBack = { navigateBack() },
                    onOpenChapterQuiz = { ch ->
                        navigateTo(Screen.ChapterQuiz(ch))
                    },
                    onOpenTimedTest = {
                        navigateTo(Screen.TimedTest)
                    },
                    onOpenSearch = {
                        navigateTo(Screen.Search)
                    },
                    onOpenSettings = {
                        navigateTo(Screen.Settings)
                    }
                )
            }

            is Screen.ChapterQuiz -> {
                ChapterQuizScreen(
                    ch = screen.ch,
                    indexData = indexData,
                    repository = repository,
                    onNavigateBack = { navigateBack() },
                    onStartPractice = { title, questions ->
                        navigateTo(Screen.PracticeRun(screen.ch, title, questions))
                    },
                    onReadChapter = { partIndex ->
                        navigateTo(Screen.Reader(partIndex))
                    },
                    onOpenSearch = {
                        navigateTo(Screen.Search)
                    },
                    onOpenSettings = {
                        navigateTo(Screen.Settings)
                    }
                )
            }

            is Screen.PracticeRun -> {
                PracticeRunScreen(
                    ch = screen.ch,
                    title = screen.title,
                    questions = screen.questions,
                    repository = repository,
                    onNavigateBack = { navigateBack() },
                    onFinishPractice = { total, correct, wrongQuestions ->
                        val items = screen.questions.map { QuizItem(ch = screen.ch, question = it) }
                        val simulatedPicks = screen.questions.map { q ->
                            val lastCorrect = repository.isQuestionLastCorrect(screen.ch, q.id)
                            if (lastCorrect == true) q.answerIndex else -1
                        }
                        navStack.removeAt(navStack.size - 1)
                        navStack.add(
                            Screen.TestResult(
                                items = items,
                                picks = simulatedPicks,
                                isTimedExamMode = false
                            )
                        )
                    }
                )
            }

            is Screen.TimedTest -> {
                TimedTestScreen(
                    indexData = indexData,
                    repository = repository,
                    onNavigateBack = { navigateBack() },
                    onFinishTest = { items, picks, durationSec ->
                        // Record score and save history
                        var c = 0
                        var w = 0
                        var b = 0
                        for (k in items.indices) {
                            val pick = picks.getOrNull(k) ?: -1
                            if (pick < 0) {
                                b++
                            } else if (pick == items[k].question.answerIndex) {
                                c++
                                repository.recordQuestionAttempt(items[k].ch, items[k].question.id, true)
                            } else {
                                w++
                                repository.recordQuestionAttempt(items[k].ch, items[k].question.id, false)
                            }
                        }

                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
                        val record = TestRecord(
                            date = dateStr,
                            totalQuestions = items.size,
                            correct = c,
                            wrong = w,
                            blank = b
                        )
                        repository.addTestRecord(record)

                        navStack.removeAt(navStack.size - 1)
                        navStack.add(
                            Screen.TestResult(
                                items = items,
                                picks = picks,
                                isTimedExamMode = true
                            )
                        )
                    },
                    onOpenSearch = {
                        navigateTo(Screen.Search)
                    },
                    onOpenSettings = {
                        navigateTo(Screen.Settings)
                    }
                )
            }

            is Screen.TestResult -> {
                TestResultScreen(
                    items = screen.items,
                    picks = screen.picks,
                    isTimedExamMode = screen.isTimedExamMode,
                    onNavigateBack = { navigateBack() },
                    onRetryAll = {
                        navStack.removeAt(navStack.size - 1)
                        navStack.add(
                            Screen.PracticeRun(
                                ch = screen.items.firstOrNull()?.ch ?: 1,
                                title = "Retry Set (${screen.items.size} Questions)",
                                questions = screen.items.map { it.question }
                            )
                        )
                    },
                    onRetryMissed = { missedQuestions ->
                        navStack.removeAt(navStack.size - 1)
                        navStack.add(
                            Screen.PracticeRun(
                                ch = screen.items.firstOrNull()?.ch ?: 1,
                                title = "Missed Questions (${missedQuestions.size})",
                                questions = missedQuestions
                            )
                        )
                    }
                )
            }

            is Screen.Search -> {
                SearchScreen(
                    indexData = indexData,
                    repository = repository,
                    onNavigateBack = { navigateBack() },
                    onOpenSearchResult = { partIndex, headingId ->
                        navigateTo(Screen.Reader(partIndex, headingId))
                    },
                    onOpenSettings = {
                        navigateTo(Screen.Settings)
                    }
                )
            }

            is Screen.Settings -> {
                SettingsScreen(
                    indexData = indexData,
                    repository = repository,
                    onNavigateBack = { navigateBack() },
                    onThemeChanged = onThemeChanged,
                    onOpenAboutPart = {
                        navigateTo(Screen.Reader(0))
                    }
                )
            }
        }
    }
}

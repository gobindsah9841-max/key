package com.example.loksewastudybook.data

data class BookIndex(
    val built: String,
    val version: String,
    val parts: List<PartMeta>,
    val firstPart: Int,
    val lastPart: Int,
    val chapters: List<Int>,
    val mcqTotal: Int,
    val saqTotal: Int
)

data class PartMeta(
    val index: Int,
    val slug: String,
    val num: Int,
    val kind: String, // "about", "front", "chapter"
    val ch: Int,
    val title: String,
    val nep: String,
    val kb: Int,
    val mcq: Int,
    val saq: Int
) {
    val displayLabel: String
        get() = when (kind) {
            "chapter" -> "Chapter $ch"
            "about" -> "About"
            else -> "Part $num"
        }

    val fullTitle: String
        get() = if (kind == "about") title else "$displayLabel — $title"
}

data class TocItem(
    val level: Int,
    val headingId: String,
    val title: String
)

data class PartContent(
    val index: Int,
    val toc: List<TocItem>,
    val html: String
)

data class QuizQuestion(
    val id: String,
    val label: String, // e.g. "Expected – Practice", "Pattern-Based – Reconstructed"
    val stem: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String
)

data class QuizTopic(
    val id: String,
    val title: String,
    val questions: List<QuizQuestion>
)

data class ChapterQuiz(
    val ch: Int,
    val title: String,
    val topics: List<QuizTopic>
) {
    val allQuestions: List<QuizQuestion>
        get() = topics.flatMap { it.questions }
}

data class QuizItem(
    val ch: Int,
    val question: QuizQuestion
)

data class SearchItem(
    val partIndex: Int,
    val headingId: String,
    val title: String,
    val body: String,
    val breadcrumb: String
)

data class TestRecord(
    val date: String,
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val blank: Int
) {
    val marks: Double
        get() = Math.round((2.0 * correct - 0.4 * wrong) * 10.0) / 10.0

    val maxMarks: Int
        get() = 2 * totalQuestions

    val percentage: Int
        get() = if (maxMarks > 0) Math.max(0, Math.round((marks / maxMarks) * 100).toInt()) else 0
}

enum class ColorTheme(val id: String, val displayName: String) {
    LIGHT("light", "Light"),
    SEPIA("sepia", "Sepia"),
    DARK("dark", "Dark");

    companion object {
        fun fromId(id: String): ColorTheme = entries.find { it.id == id } ?: LIGHT
    }
}

enum class ReaderFontSize(val id: Int, val displayName: String, val scale: Float) {
    SMALLEST(0, "Smallest", 0.85f),
    SMALL(1, "Small", 0.93f),
    MEDIUM(2, "Medium", 1.0f),
    LARGE(3, "Large", 1.12f),
    LARGEST(4, "Largest", 1.25f);

    companion object {
        fun fromId(id: Int): ReaderFontSize = entries.find { it.id == id } ?: MEDIUM
    }
}

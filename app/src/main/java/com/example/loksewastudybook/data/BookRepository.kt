package com.example.loksewastudybook.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class BookRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lsbook_prefs", Context.MODE_PRIVATE)

    // In-memory caches
    private var cachedIndex: BookIndex? = null
    private val cachedParts = ConcurrentHashMap<Int, PartContent>()
    private val cachedQuizzes = ConcurrentHashMap<Int, ChapterQuiz>()
    private var cachedSearchItems: List<SearchItem>? = null

    // Helper to extract JSON from LS_REG(...) wrapping in .js files
    private fun extractJson(content: String): String {
        val start = content.indexOfAny(charArrayOf('{', '['))
        val end = content.lastIndexOfAny(charArrayOf('}', ']'))
        return if (start != -1 && end != -1 && end >= start) {
            content.substring(start, end + 1)
        } else {
            content
        }
    }

    private fun readAsset(path: String): String {
        return context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun getIndex(): BookIndex {
        cachedIndex?.let { return it }

        val raw = readAsset("data/index.js")
        val json = JSONObject(extractJson(raw))

        val partsArray = json.getJSONArray("parts")
        val partsList = mutableListOf<PartMeta>()
        for (i in 0 until partsArray.length()) {
            val p = partsArray.getJSONObject(i)
            partsList.add(
                PartMeta(
                    index = p.optInt("i", i),
                    slug = p.optString("slug", ""),
                    num = p.optInt("num", 0),
                    kind = p.optString("kind", "front"),
                    ch = p.optInt("ch", 0),
                    title = p.optString("title", ""),
                    nep = p.optString("nep", ""),
                    kb = p.optInt("kb", 0),
                    mcq = p.optInt("mcq", 0),
                    saq = p.optInt("saq", 0)
                )
            )
        }

        val chaptersArray = json.optJSONArray("chapters") ?: JSONArray()
        val chaptersList = mutableListOf<Int>()
        for (i in 0 until chaptersArray.length()) {
            chaptersList.add(chaptersArray.getInt(i))
        }

        val parsed = BookIndex(
            built = json.optString("built", ""),
            version = json.optString("version", "1.0"),
            parts = partsList,
            firstPart = json.optInt("firstPart", 1),
            lastPart = json.optInt("lastPart", 14),
            chapters = chaptersList,
            mcqTotal = json.optInt("mcq", 710),
            saqTotal = json.optInt("saq", 142)
        )
        cachedIndex = parsed
        return parsed
    }

    fun getPartContent(partIndex: Int): PartContent {
        cachedParts[partIndex]?.let { return it }

        val raw = readAsset("data/part-$partIndex.js")
        val json = JSONObject(extractJson(raw))

        val tocArray = json.optJSONArray("toc") ?: JSONArray()
        val tocList = mutableListOf<TocItem>()
        for (i in 0 until tocArray.length()) {
            val item = tocArray.getJSONArray(i)
            tocList.add(
                TocItem(
                    level = item.optInt(0, 2),
                    headingId = item.optString(1, ""),
                    title = item.optString(2, "")
                )
            )
        }

        val html = json.optString("html", "")
        val partContent = PartContent(
            index = partIndex,
            toc = tocList,
            html = html
        )
        cachedParts[partIndex] = partContent
        return partContent
    }

    fun getChapterQuiz(ch: Int): ChapterQuiz {
        cachedQuizzes[ch]?.let { return it }

        val raw = readAsset("data/quiz-$ch.js")
        val json = JSONObject(extractJson(raw))

        val topicsArray = json.optJSONArray("topics") ?: JSONArray()
        val topicsList = mutableListOf<QuizTopic>()
        for (i in 0 until topicsArray.length()) {
            val tObj = topicsArray.getJSONObject(i)
            val qArray = tObj.optJSONArray("q") ?: JSONArray()
            val questionsList = mutableListOf<QuizQuestion>()
            for (j in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(j)
                val optsArray = qObj.optJSONArray("o") ?: JSONArray()
                val options = mutableListOf<String>()
                for (k in 0 until optsArray.length()) {
                    options.add(optsArray.getString(k))
                }
                questionsList.add(
                    QuizQuestion(
                        id = qObj.optString("id", ""),
                        label = qObj.optString("l", ""),
                        stem = qObj.optString("s", ""),
                        options = options,
                        answerIndex = qObj.optInt("a", 0),
                        explanation = qObj.optString("e", "")
                    )
                )
            }
            topicsList.add(
                QuizTopic(
                    id = tObj.optString("id", ""),
                    title = tObj.optString("title", ""),
                    questions = questionsList
                )
            )
        }

        val quiz = ChapterQuiz(
            ch = json.optInt("ch", ch),
            title = json.optString("title", ""),
            topics = topicsList
        )
        cachedQuizzes[ch] = quiz
        return quiz
    }

    suspend fun getSearchIndex(): List<SearchItem> = withContext(Dispatchers.IO) {
        cachedSearchItems?.let { return@withContext it }

        val raw = readAsset("data/search.js")
        val array = JSONArray(extractJson(raw))
        val list = ArrayList<SearchItem>(array.length())
        for (i in 0 until array.length()) {
            val item = array.getJSONArray(i)
            list.add(
                SearchItem(
                    partIndex = item.optInt(0, 0),
                    headingId = item.optString(1, ""),
                    title = item.optString(2, ""),
                    body = item.optString(3, ""),
                    breadcrumb = item.optString(4, "")
                )
            )
        }
        cachedSearchItems = list
        list
    }

    // User Preferences & State
    var fontSize: ReaderFontSize
        get() = ReaderFontSize.fromId(prefs.getInt("pref_font_size", ReaderFontSize.MEDIUM.id))
        set(value) = prefs.edit().putInt("pref_font_size", value.id).apply()

    var colorTheme: ColorTheme
        get() = ColorTheme.fromId(prefs.getString("pref_color_theme", ColorTheme.LIGHT.id) ?: "light")
        set(value) = prefs.edit().putString("pref_color_theme", value.id).apply()

    var hideAnswersInReader: Boolean
        get() = prefs.getBoolean("pref_hide_answers", true)
        set(value) = prefs.edit().putBoolean("pref_hide_answers", value).apply()

    var lastReadPartIndex: Int
        get() = prefs.getInt("pref_last_read_part", -1)
        set(value) = prefs.edit().putInt("pref_last_read_part", value).apply()

    var lastSearchQuery: String
        get() = prefs.getString("pref_last_search_query", "") ?: ""
        set(value) = prefs.edit().putString("pref_last_search_query", value).apply()

    fun getReadingPosition(partIndex: Int): Int {
        return prefs.getInt("pos_$partIndex", 0)
    }

    fun saveReadingPosition(partIndex: Int, pos: Int) {
        prefs.edit().putInt("pos_$partIndex", pos).apply()
    }

    // Question stats: "ch|qid" -> triedCount, correctCount, lastWasCorrect(1 or 0)
    fun recordQuestionAttempt(ch: Int, qId: String, isCorrect: Boolean) {
        val key = "st_${ch}_$qId"
        val existing = prefs.getString(key, null)
        var tried = 1
        var right = if (isCorrect) 1 else 0
        if (existing != null) {
            val parts = existing.split(",")
            if (parts.size >= 2) {
                tried = (parts[0].toIntOrNull() ?: 0) + 1
                right = (parts[1].toIntOrNull() ?: 0) + (if (isCorrect) 1 else 0)
            }
        }
        val encoded = "$tried,$right,${if (isCorrect) 1 else 0}"
        prefs.edit().putString(key, encoded).apply()
    }

    fun isQuestionAttempted(ch: Int, qId: String): Boolean {
        return prefs.contains("st_${ch}_$qId")
    }

    fun isQuestionLastCorrect(ch: Int, qId: String): Boolean? {
        val raw = prefs.getString("st_${ch}_$qId", null) ?: return null
        val parts = raw.split(",")
        return if (parts.size >= 3) parts[2] == "1" else null
    }

    fun getChapterStats(ch: Int, totalQuestions: Int): Pair<Int, Int> {
        val all = prefs.all
        val prefix = "st_${ch}_"
        var tried = 0
        var right = 0
        for ((k, v) in all) {
            if (k.startsWith(prefix) && v is String) {
                tried++
                val parts = v.split(",")
                if (parts.size >= 3 && parts[2] == "1") {
                    right++
                }
            }
        }
        return Pair(tried, right)
    }

    fun getQuestionsWrong(ch: Int, questions: List<QuizQuestion>): List<QuizQuestion> {
        return questions.filter { q ->
            val correct = isQuestionLastCorrect(ch, q.id)
            correct == false
        }
    }

    fun getTotalQuestionsTried(): Int {
        var count = 0
        for (k in prefs.all.keys) {
            if (k.startsWith("st_")) count++
        }
        return count
    }

    // Test History
    fun getTestRecords(): List<TestRecord> {
        val raw = prefs.getString("pref_test_records", null) ?: return emptyList()
        val list = mutableListOf<TestRecord>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TestRecord(
                        date = obj.optString("d", ""),
                        totalQuestions = obj.optInt("n", 0),
                        correct = obj.optInt("c", 0),
                        wrong = obj.optInt("w", 0),
                        blank = obj.optInt("b", 0)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun addTestRecord(record: TestRecord) {
        val current = getTestRecords().toMutableList()
        current.add(record)
        // Keep last 20 tests
        val trimmed = if (current.size > 20) current.takeLast(20) else current
        val arr = JSONArray()
        for (t in trimmed) {
            val obj = JSONObject()
            obj.put("d", t.date)
            obj.put("n", t.totalQuestions)
            obj.put("c", t.correct)
            obj.put("w", t.wrong)
            obj.put("b", t.blank)
            arr.put(obj)
        }
        prefs.edit().putString("pref_test_records", arr.toString()).apply()
    }

    fun resetAllUserData() {
        prefs.edit().clear().apply()
    }
}

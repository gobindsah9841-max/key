package com.example.loksewastudybook.ui.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.loksewastudybook.data.BookIndex
import com.example.loksewastudybook.data.BookRepository
import com.example.loksewastudybook.data.ColorTheme
import com.example.loksewastudybook.data.PartContent
import com.example.loksewastudybook.ui.components.AppTopBar
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    partIndex: Int,
    initialHeadingId: String? = null,
    indexData: BookIndex,
    repository: BookRepository,
    onNavigateBack: () -> Unit,
    onOpenPart: (Int) -> Unit,
    onOpenChapterQuiz: (Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    val currentPart = indexData.parts.getOrNull(partIndex) ?: indexData.parts.first()
    var partContent by remember(partIndex) {
        mutableStateOf(repository.getPartContent(partIndex))
    }

    // Save as last read
    LaunchedEffect(partIndex) {
        repository.lastReadPartIndex = partIndex
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showTocSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val currentTheme = repository.colorTheme
    val fontSize = repository.fontSize
    val hideAnswers = repository.hideAnswersInReader

    // Construct the full HTML document with embedded CSS and styling
    val fullHtml = remember(partContent, currentTheme, fontSize, hideAnswers) {
        buildReaderHtml(
            title = currentPart.fullTitle,
            bodyHtml = partContent.html,
            theme = currentTheme,
            fontSizeId = fontSize.id,
            hideAnswers = hideAnswers,
            initialHeadingId = initialHeadingId
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = currentPart.fullTitle,
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showToc = partContent.toc.isNotEmpty(),
                onTocClick = { showTocSheet = true },
                showSearch = true,
                onSearchClick = onOpenSearch,
                showSettings = true,
                onSettingsClick = onOpenSettings
            )
        },
        bottomBar = {
            ReaderBottomBar(
                currentPartIndex = partIndex,
                totalParts = indexData.parts.size,
                isChapter = currentPart.kind == "chapter",
                chapterNum = currentPart.ch,
                mcqCount = currentPart.mcq,
                prevPart = indexData.parts.getOrNull(partIndex - 1),
                nextPart = indexData.parts.getOrNull(partIndex + 1),
                onPrevClick = { onOpenPart(partIndex - 1) },
                onNextClick = { onOpenPart(partIndex + 1) },
                onPracticeClick = { onOpenChapterQuiz(currentPart.ch) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("reader_webview_container")
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.defaultTextEncodingName = "utf-8"

                        // Javascript interface to bridge clicks or scroll save
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onScrollChanged(scrollY: Int) {
                                repository.saveReadingPosition(partIndex, scrollY)
                            }
                        }, "AndroidBridge")

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (!initialHeadingId.isNullOrBlank()) {
                                    view?.evaluateJavascript(
                                        "var el = document.getElementById('$initialHeadingId'); if (el) el.scrollIntoView({behavior: 'smooth'});",
                                        null
                                    )
                                } else {
                                    val savedY = repository.getReadingPosition(partIndex)
                                    if (savedY > 0) {
                                        view?.evaluateJavascript("window.scrollTo(0, $savedY);", null)
                                    }
                                }
                            }
                        }
                        webViewRef = this
                        loadDataWithBaseURL("file:///android_asset/", fullHtml, "text/html", "UTF-8", null)
                    }
                },
                update = { wv ->
                    wv.loadDataWithBaseURL("file:///android_asset/", fullHtml, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Table of Contents Bottom Sheet
    if (showTocSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTocSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "In this part",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { showTocSheet = false }
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    webViewRef?.evaluateJavascript("window.scrollTo({top: 0, behavior: 'smooth'});", null)
                                    scope.launch { sheetState.hide() }.invokeOnCompletion { showTocSheet = false }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Top of page",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }

                    items(partContent.toc) { item ->
                        val indent = when (item.level) {
                            2 -> 0.dp
                            3 -> 16.dp
                            4 -> 32.dp
                            else -> 8.dp
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = indent)
                                .clickable {
                                    webViewRef?.evaluateJavascript(
                                        "var el = document.getElementById('${item.headingId}'); if (el) el.scrollIntoView({behavior: 'smooth'});",
                                        null
                                    )
                                    scope.launch { sheetState.hide() }.invokeOnCompletion { showTocSheet = false }
                                },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ReaderBottomBar(
    currentPartIndex: Int,
    totalParts: Int,
    isChapter: Boolean,
    chapterNum: Int,
    mcqCount: Int,
    prevPart: com.example.loksewastudybook.data.PartMeta?,
    nextPart: com.example.loksewastudybook.data.PartMeta?,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onPracticeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (isChapter) {
                Button(
                    onClick = onPracticeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reader_practice_chapter_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Practice the $mcqCount MCQs of this chapter")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (prevPart != null) {
                    OutlinedButton(
                        onClick = onPrevClick,
                        modifier = Modifier.testTag("reader_prev_part_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(prevPart.displayLabel)
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (nextPart != null) {
                    OutlinedButton(
                        onClick = onNextClick,
                        modifier = Modifier.testTag("reader_next_part_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(nextPart.displayLabel)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

private fun buildReaderHtml(
    title: String,
    bodyHtml: String,
    theme: ColorTheme,
    fontSizeId: Int,
    hideAnswers: Boolean,
    initialHeadingId: String?
): String {
    val themeClass = "th-${theme.id}"
    val fsClass = "fs$fontSizeId"
    val hideAnsClass = if (hideAnswers) "hide-ans" else ""

    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
            <title>$title</title>
            <link rel="stylesheet" href="app.css">
            <style>
                body {
                    margin: 0;
                    padding: 16px 14px 40px 14px;
                    -webkit-tap-highlight-color: transparent;
                }
                /* Additional native mobile enhancements */
                .book table {
                    width: 100%;
                    border-collapse: collapse;
                }
                .tw {
                    overflow-x: auto;
                    -webkit-overflow-scrolling: touch;
                    margin: 12px 0;
                }
                .ans {
                    cursor: pointer;
                    user-select: none;
                }
            </style>
        </head>
        <body class="$themeClass $fsClass $hideAnsClass">
            <article class="book" id="book">
                $bodyHtml
            </article>

            <script>
                // Handle tapping to reveal hidden answers in reader mode
                document.addEventListener('click', function(e) {
                    var el = e.target;
                    while (el && el !== document.body) {
                        if (el.classList && el.classList.contains('ans')) {
                            if (document.body.classList.contains('hide-ans')) {
                                el.classList.toggle('shown');
                            }
                            return;
                        }
                        el = el.parentElement;
                    }
                }, false);

                // Report scroll position
                window.addEventListener('scroll', function() {
                    if (window.AndroidBridge && window.AndroidBridge.onScrollChanged) {
                        window.AndroidBridge.onScrollChanged(window.pageYOffset || document.documentElement.scrollTop || 0);
                    }
                }, false);
            </script>
        </body>
        </html>
    """.trimIndent()
}

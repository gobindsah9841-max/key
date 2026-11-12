package com.example.loksewastudybook.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.loksewastudybook.data.BookIndex
import com.example.loksewastudybook.data.BookRepository
import com.example.loksewastudybook.data.SearchItem
import com.example.loksewastudybook.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    indexData: BookIndex,
    repository: BookRepository,
    onNavigateBack: () -> Unit,
    onOpenSearchResult: (partIndex: Int, headingId: String) -> Unit,
    onOpenSettings: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var query by remember { mutableStateOf(repository.lastSearchQuery) }
    var allSearchItems by remember { mutableStateOf<List<SearchItem>?>(null) }
    var isLoadingIndex by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        allSearchItems = repository.getSearchIndex()
        isLoadingIndex = false
    }

    // Filter results
    val searchResults = remember(query, allSearchItems) {
        val q = query.trim().lowercase()
        if (q.length < 2 || allSearchItems == null) {
            emptyList()
        } else {
            val list = allSearchItems!!
            val hits = mutableListOf<Pair<Boolean, SearchItem>>()
            for (item in list) {
                val inTitle = item.title.lowercase().contains(q)
                val inBody = item.body.lowercase().contains(q)
                if (inTitle || inBody) {
                    hits.add(Pair(inTitle, item))
                    if (hits.size >= 100) break
                }
            }
            // Sort title matches first
            hits.sortedByDescending { it.first }.map { it.second }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Search the Book",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showSearch = false,
                showSettings = true,
                onSettingsClick = onOpenSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("search_screen")
        ) {
            // Search Input Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            repository.lastSearchQuery = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input"),
                        placeholder = { Text("Type a word, name or date (e.g. Lipulekh)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = {
                                    query = ""
                                    repository.lastSearchQuery = ""
                                }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Suggestion quick chips
                    val suggestions = listOf("Lipulekh", "Article 76", "2083", "संविधान", "Monsoon", "Ramsar", "Koshi")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(suggestions) { s ->
                            FilterChip(
                                selected = query == s,
                                onClick = {
                                    query = s
                                    repository.lastSearchQuery = s
                                },
                                label = { Text(s) }
                            )
                        }
                    }
                }
            }

            // Results List
            if (isLoadingIndex) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading search index…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (query.trim().length < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Search all 15 parts of the book",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Type at least two letters in English or in Nepali — try searching for Lipulekh, Article 76, 2083, or संविधान.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sections found matching \"$query\".",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "${searchResults.size} section(s) found",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(searchResults) { hit ->
                        val part = indexData.parts.getOrNull(hit.partIndex)
                        val partTitle = part?.fullTitle ?: "Part ${hit.partIndex}"
                        val breadcrumb = if (hit.breadcrumb.isNotBlank()) " › ${hit.breadcrumb}" else ""

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenSearchResult(hit.partIndex, hit.headingId)
                                }
                                .testTag("search_hit_${hit.headingId}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = partTitle + breadcrumb,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = hit.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Render highlighted snippet
                                val snippet = makeSnippet(hit.body, query.trim())
                                Text(
                                    text = snippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun makeSnippet(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    val lower = text.lowercase()
    val qLower = query.lowercase()
    val at = lower.indexOf(qLower)

    if (at < 0) {
        val snippetStr = if (text.length > 130) text.take(130) + "…" else text
        return buildAnnotatedString { append(snippetStr) }
    }

    val from = maxOf(0, at - 50)
    val to = minOf(text.length, at + query.length + 70)

    val pre = (if (from > 0) "…" else "") + text.substring(from, at)
    val matched = text.substring(at, at + query.length)
    val post = text.substring(at + query.length, to) + (if (to < text.length) "…" else "")

    return buildAnnotatedString {
        append(pre)
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), background = Color(0xFFFEF08A))) {
            append(matched)
        }
        append(post)
    }
}

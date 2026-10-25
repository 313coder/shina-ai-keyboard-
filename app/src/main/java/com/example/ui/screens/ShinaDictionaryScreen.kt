package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShinaDictionaryEntryEntity
import com.example.data.ShinaKeyboardRepository
import com.example.util.ShinaHapticManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShinaDictionaryScreen(
    repository: ShinaKeyboardRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDialect by remember { mutableStateOf("") }
    var selectedPartOfSpeech by remember { mutableStateOf("") }
    var userOnlyFilter by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var wordToContributePreFill by remember { mutableStateOf("") }

    val totalCount by repository.totalDictionaryCount.collectAsState(initial = 0)
    val userCount by repository.userContributedDictionaryCount.collectAsState(initial = 0)

    // Collect search results reactively
    val entries by remember(searchQuery, selectedDialect, selectedPartOfSpeech) {
        if (selectedDialect.isEmpty() && selectedPartOfSpeech.isEmpty()) {
            repository.searchDictionary(searchQuery)
        } else {
            repository.searchFilteredDictionary(searchQuery, selectedDialect, selectedPartOfSpeech)
        }
    }.collectAsState(initial = emptyList())

    val filteredEntries = if (userOnlyFilter) {
        entries.filter { it.isUserContributed }
    } else {
        entries
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && filteredEntries.isNotEmpty()) {
            ShinaHapticManager.performDictionaryResultFound(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Shina Offline Dictionary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$totalCount Words Cached • $userCount User-Contributed",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            wordToContributePreFill = ""
                            showAddDialog = true
                        },
                        modifier = Modifier.testTag("dictionary_add_word_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Contribute Word",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    wordToContributePreFill = ""
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.PostAdd, contentDescription = null) },
                text = { Text("Contribute Word") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_contribute_dictionary_word")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- Search Bar ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dictionary_search_input"),
                placeholder = {
                    Text("Search in Shina (سلام), Roman (salam), or Urdu/English meanings...", fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- Filter Chips Row ---
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = selectedDialect.isEmpty() && selectedPartOfSpeech.isEmpty() && !userOnlyFilter,
                        onClick = {
                            selectedDialect = ""
                            selectedPartOfSpeech = ""
                            userOnlyFilter = false
                        },
                        label = { Text("All", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = userOnlyFilter,
                        onClick = { userOnlyFilter = !userOnlyFilter },
                        label = { Text("User Contributed ($userCount)", fontSize = 12.sp) },
                        leadingIcon = {
                            if (userOnlyFilter) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }

                // Parts of speech
                listOf("Noun", "Verb", "Adjective", "Pronoun", "Greeting").forEach { pos ->
                    item {
                        FilterChip(
                            selected = selectedPartOfSpeech == pos,
                            onClick = {
                                selectedPartOfSpeech = if (selectedPartOfSpeech == pos) "" else pos
                            },
                            label = { Text(pos, fontSize = 12.sp) }
                        )
                    }
                }

                // Dialects
                listOf("Gilgit", "Astore", "Diamer", "Chilas", "Kohistan").forEach { dialect ->
                    item {
                        FilterChip(
                            selected = selectedDialect == dialect,
                            onClick = {
                                selectedDialect = if (selectedDialect == dialect) "" else dialect
                            },
                            label = { Text(dialect, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Search Results List ---
            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching word found for \"$searchQuery\"" else "No dictionary words found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Shina is an evolving digital language! Help preserve this word by adding its definition, Roman transliteration, and synonyms to the offline database.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                wordToContributePreFill = searchQuery
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("dictionary_empty_contribute_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Contribute \"${if (searchQuery.isNotBlank()) searchQuery else "New Word"}\"")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        DictionaryWordCard(
                            entry = entry,
                            onSynonymClick = { syn ->
                                ShinaHapticManager.performDictionaryResultFound(context)
                                searchQuery = syn
                            },
                            onCopy = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Shina Word", "${entry.wordShina} (${entry.romanized}): ${entry.definitionUrdu}")
                                cm.setPrimaryClip(clip)
                                ShinaHapticManager.performKeyClick(context)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                scope.launch {
                                    repository.deleteDictionaryEntry(entry.id)
                                    ShinaHapticManager.performKeyClick(context)
                                    Toast.makeText(context, "Word removed from dictionary", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- Contribute / Add Word Dialog ---
    if (showAddDialog) {
        ContributeWordDialog(
            initialWord = wordToContributePreFill,
            onDismiss = { showAddDialog = false },
            onSave = { word, roman, pos, urdu, eng, synonyms, sentence, transl, dialect, contributor ->
                scope.launch {
                    repository.addDictionaryEntry(
                        wordShina = word,
                        romanized = roman,
                        partOfSpeech = pos,
                        definitionUrdu = urdu,
                        definitionEnglish = eng,
                        synonyms = synonyms,
                        exampleSentenceShina = sentence,
                        exampleTranslation = transl,
                        dialect = dialect,
                        contributorName = contributor
                    )
                    ShinaHapticManager.performDataSubmissionSuccess(context)
                    Toast.makeText(context, "Thank you! \"$word\" added to Shina offline dictionary.", Toast.LENGTH_LONG).show()
                    showAddDialog = false
                    searchQuery = word
                }
            }
        )
    }
}

@Composable
fun DictionaryWordCard(
    entry: ShinaDictionaryEntryEntity,
    onSynonymClick: (String) -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dict_card_${entry.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Shina Word + Dialect & POS Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.wordShina,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (entry.romanized.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${entry.romanized}]",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = entry.dialect,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = entry.partOfSpeech,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Urdu Definition
            Text(
                text = entry.definitionUrdu,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // English Definition (if available)
            if (entry.definitionEnglish.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.definitionEnglish,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Synonyms (مترادفات)
            if (entry.synonyms.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Synonyms",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Synonyms (مترادفات):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val synonymList = entry.synonyms.split(",").map { it.trim() }.filter { it.isNotBlank() }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    synonymList.forEach { syn ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSynonymClick(syn) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = syn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowOutward,
                                    contentDescription = "Look up synonym",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Example Sentence (if present)
            if (entry.exampleSentenceShina.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Example in Shina:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "\"${entry.exampleSentenceShina}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (entry.exampleTranslation.isNotBlank()) {
                            Text(
                                text = "→ ${entry.exampleTranslation}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Contributor info & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (entry.isUserContributed) Icons.Default.Person else Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (entry.isUserContributed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (entry.isUserContributed) "Contributed by: ${entry.contributorName}" else "Verified Shina Archive",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy definition",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (entry.isUserContributed) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete word",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

@Composable
fun ContributeWordDialog(
    initialWord: String,
    onDismiss: () -> Unit,
    onSave: (
        word: String,
        roman: String,
        pos: String,
        urdu: String,
        eng: String,
        synonyms: String,
        sentence: String,
        transl: String,
        dialect: String,
        contributor: String
    ) -> Unit
) {
    var word by remember { mutableStateOf(initialWord) }
    var roman by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("Noun (اسم)") }
    var urdu by remember { mutableStateOf("") }
    var eng by remember { mutableStateOf("") }
    var synonyms by remember { mutableStateOf("") }
    var sentence by remember { mutableStateOf("") }
    var transl by remember { mutableStateOf("") }
    var dialect by remember { mutableStateOf("Gilgit") }
    var contributor by remember { mutableStateOf("You (Offline Contributor)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (word.isNotBlank() && urdu.isNotBlank()) {
                        onSave(word, roman, pos, urdu, eng, synonyms, sentence, transl, dialect, contributor)
                    }
                },
                enabled = word.isNotBlank() && urdu.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_submit_word_btn")
            ) {
                Text("Add to Offline Dictionary")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Contribute Shina Word", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Contribute authentic Shina words, definitions, and synonyms to empower the offline dictionary and AI model for the community.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = word,
                        onValueChange = { word = it },
                        label = { Text("Word in Shina Script *") },
                        placeholder = { Text("مثلاً: سلام / مزخ / مست") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_input_shina_word")
                    )
                }

                item {
                    OutlinedTextField(
                        value = roman,
                        onValueChange = { roman = it },
                        label = { Text("Roman Shina Transliteration") },
                        placeholder = { Text("e.g. Salam / Mazakh / Mast") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = urdu,
                        onValueChange = { urdu = it },
                        label = { Text("Urdu Meaning / Definition *") },
                        placeholder = { Text("مثلاً: محبت / پیار / خوشی") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_input_urdu_def")
                    )
                }

                item {
                    OutlinedTextField(
                        value = eng,
                        onValueChange = { eng = it },
                        label = { Text("English Meaning (Optional)") },
                        placeholder = { Text("e.g. Love / Joy / Peace") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = synonyms,
                        onValueChange = { synonyms = it },
                        label = { Text("Synonyms in Shina (مترادفات)") },
                        placeholder = { Text("Comma-separated, e.g. الفت, پیار, چاہت") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_input_synonyms")
                    )
                }

                item {
                    OutlinedTextField(
                        value = sentence,
                        onValueChange = { sentence = it },
                        label = { Text("Example Sentence in Shina") },
                        placeholder = { Text("e.g. مئے دلس مجھ مزخ ہن۔") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = transl,
                        onValueChange = { transl = it },
                        label = { Text("Sentence Translation (Urdu/English)") },
                        placeholder = { Text("e.g. There is love in my heart.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dialect,
                            onValueChange = { dialect = it },
                            label = { Text("Dialect") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = pos,
                            onValueChange = { pos = it },
                            label = { Text("Part of Speech") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = contributor,
                        onValueChange = { contributor = it },
                        label = { Text("Contributor Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

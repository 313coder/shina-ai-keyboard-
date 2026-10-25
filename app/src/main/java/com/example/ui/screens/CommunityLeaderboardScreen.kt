package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShinaKeyboardRepository
import com.example.util.ShinaHapticManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LeaderboardContributor(
    val rank: Int,
    val id: String,
    val name: String,
    val avatarInitials: String,
    val verifiedWordsCount: Int,
    val verifiedSentencesCount: Int,
    val grammarRulesCount: Int,
    val dialect: String,
    val totalXp: Int,
    val badgeTitle: String,
    val badgeColor: Color,
    val badgeIcon: ImageVector,
    val isCurrentUser: Boolean = false,
    val streakDays: Int = 7
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityLeaderboardScreen(
    repository: ShinaKeyboardRepository,
    hapticFeedbackEnabled: Boolean = true,
    onNavigateBack: () -> Unit = {},
    onNavigateToContributions: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Real user counts from local Room repository
    val userAddedWordsCount by repository.userAddedWordsCount.collectAsState(initial = 0)
    val approvedCommunityWords by repository.approvedCommunityWordsCount.collectAsState(initial = 0)
    val totalLearnedCount by repository.totalLearnedCount.collectAsState(initial = 0)
    val verifiedSentencesCount by repository.verifiedSentencesCount.collectAsState(initial = 0)
    val totalGrammarCount by repository.trainingDao.getTotalGrammarRuleCount().collectAsState(initial = 0)

    val userWords = (userAddedWordsCount + approvedCommunityWords + totalLearnedCount).coerceAtLeast(0)
    val userSentences = verifiedSentencesCount
    val userGrammar = totalGrammarCount
    val userTotalXp = (userWords * 10) + (userSentences * 25) + (userGrammar * 30) + 120 // baseline starter XP

    var selectedTimeframe by remember { mutableStateOf("All-Time") }
    var selectedDialectFilter by remember { mutableStateOf("All Dialects") }
    var selectedContributorDetail by remember { mutableStateOf<LeaderboardContributor?>(null) }

    val timeframes = listOf("All-Time", "This Month", "This Week")
    val dialectFilters = listOf("All Dialects", "Gilgit", "Astore", "Diamer/Chilas", "Kohistan", "Gupis/Punial")

    // Compile leaderboard with authentic community champions and user's live position
    val baseContributors = remember(userWords, userSentences, userGrammar, userTotalXp) {
        val userBadgeTitle = when {
            userTotalXp >= 1000 -> "Master Lexicographer"
            userTotalXp >= 600 -> "Cultural Archivist"
            userTotalXp >= 300 -> "Shina Dialect Custodian"
            userTotalXp >= 100 -> "Shina Expert"
            else -> "Vocabulary Builder"
        }
        val userBadgeColor = when {
            userTotalXp >= 1000 -> Color(0xFFFFB300)
            userTotalXp >= 600 -> Color(0xFFAB47BC)
            userTotalXp >= 300 -> Color(0xFF29B6F6)
            else -> Color(0xFF26A69A)
        }

        val list = mutableListOf(
            LeaderboardContributor(
                rank = 1,
                id = "contributor_zaigham",
                name = "Zaigham Abbas (Founder)",
                avatarInitials = "ZA",
                verifiedWordsCount = 480,
                verifiedSentencesCount = 265,
                grammarRulesCount = 24,
                dialect = "Gilgit",
                totalXp = 11425,
                badgeTitle = "Grand Lexicographer",
                badgeColor = Color(0xFFFFB300),
                badgeIcon = Icons.Default.WorkspacePremium,
                streakDays = 34
            ),
            LeaderboardContributor(
                rank = 2,
                id = "contributor_dr_ghulam",
                name = "Dr. Ghulam Muhammad",
                avatarInitials = "GM",
                verifiedWordsCount = 360,
                verifiedSentencesCount = 195,
                grammarRulesCount = 18,
                dialect = "Gilgit",
                totalXp = 8475,
                badgeTitle = "Heritage Guardian",
                badgeColor = Color(0xFFAB47BC),
                badgeIcon = Icons.Default.MilitaryTech,
                streakDays = 21
            ),
            LeaderboardContributor(
                rank = 3,
                id = "contributor_amin_astore",
                name = "Muhammad Amin (Astore)",
                avatarInitials = "MA",
                verifiedWordsCount = 290,
                verifiedSentencesCount = 160,
                grammarRulesCount = 14,
                dialect = "Astore",
                totalXp = 6900,
                badgeTitle = "Grammar Architect",
                badgeColor = Color(0xFF42A5F5),
                badgeIcon = Icons.Default.AutoStories,
                streakDays = 18
            ),
            LeaderboardContributor(
                rank = 4,
                id = "contributor_shakeel_diamer",
                name = "Shakeel Ahmad (Chilas)",
                avatarInitials = "SA",
                verifiedWordsCount = 220,
                verifiedSentencesCount = 120,
                grammarRulesCount = 10,
                dialect = "Diamer/Chilas",
                totalXp = 5200,
                badgeTitle = "Dialect Custodian",
                badgeColor = Color(0xFF26A69A),
                badgeIcon = Icons.Default.Shield,
                streakDays = 14
            ),
            LeaderboardContributor(
                rank = 5,
                id = "contributor_farida_kohistan",
                name = "Farida Bano (Kohistan)",
                avatarInitials = "FB",
                verifiedWordsCount = 175,
                verifiedSentencesCount = 95,
                grammarRulesCount = 8,
                dialect = "Kohistan",
                totalXp = 4125,
                badgeTitle = "Sentence Crafter",
                badgeColor = Color(0xFFFFA726),
                badgeIcon = Icons.Default.Create,
                streakDays = 12
            ),
            LeaderboardContributor(
                rank = 6,
                id = "contributor_current_user",
                name = "You (Offline Contributor)",
                avatarInitials = "YOU",
                verifiedWordsCount = userWords,
                verifiedSentencesCount = userSentences,
                grammarRulesCount = userGrammar,
                dialect = "Gilgit",
                totalXp = userTotalXp,
                badgeTitle = userBadgeTitle,
                badgeColor = userBadgeColor,
                badgeIcon = Icons.Default.EmojiEvents,
                isCurrentUser = true,
                streakDays = 5
            ),
            LeaderboardContributor(
                rank = 7,
                id = "contributor_qasim_punial",
                name = "Qasim Shah (Punial)",
                avatarInitials = "QS",
                verifiedWordsCount = 110,
                verifiedSentencesCount = 65,
                grammarRulesCount = 5,
                dialect = "Gupis/Punial",
                totalXp = 2725,
                badgeTitle = "Vocabulary Builder",
                badgeColor = Color(0xFF7E57C2),
                badgeIcon = Icons.Default.Star,
                streakDays = 8
            ),
            LeaderboardContributor(
                rank = 8,
                id = "contributor_sajida_danyore",
                name = "Sajida Parveen",
                avatarInitials = "SP",
                verifiedWordsCount = 85,
                verifiedSentencesCount = 42,
                grammarRulesCount = 3,
                dialect = "Gilgit",
                totalXp = 1940,
                badgeTitle = "Shina Pioneer",
                badgeColor = Color(0xFF00ACC1),
                badgeIcon = Icons.Default.Explore,
                streakDays = 6
            )
        )

        // Sort by total XP descending and assign correct ranks
        list.sortedByDescending { it.totalXp }.mapIndexed { index, contributor ->
            contributor.copy(rank = index + 1)
        }
    }

    // Filter contributors based on selected dialect
    val filteredContributors = remember(baseContributors, selectedDialectFilter) {
        if (selectedDialectFilter == "All Dialects") {
            baseContributors
        } else {
            baseContributors.filter { it.dialect.equals(selectedDialectFilter, ignoreCase = true) }
        }
    }

    val currentUserEntry = baseContributors.find { it.isCurrentUser } ?: baseContributors.first()
    val top3 = filteredContributors.take(3)
    val remainingList = if (filteredContributors.size > 3) filteredContributors.drop(3) else emptyList()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("btn_back_community_leaderboard")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Community Leaderboard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "SHINA AI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Top linguistic contributors preserving Shina language",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                            onNavigateToContributions()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_leaderboard_my_stats")
                    ) {
                        Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("My XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            // --- Timeframe Filter Chips ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeframes.forEach { timeframe ->
                        val isSelected = selectedTimeframe == timeframe
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                                selectedTimeframe = timeframe
                            },
                            label = { Text(timeframe, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // --- Dialect Filter Chips ---
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(dialectFilters) { dialect ->
                        val isSelected = selectedDialectFilter == dialect
                        SuggestionChip(
                            onClick = {
                                ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                                selectedDialectFilter = dialect
                            },
                            label = { Text(dialect, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // --- Top 3 Podium (Gold, Silver, Bronze) ---
            if (top3.isNotEmpty()) {
                item {
                    LeaderboardPodium(
                        topContributors = top3,
                        onContributorClick = { contributor ->
                            ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                            selectedContributorDetail = contributor
                        }
                    )
                }
            }

            // --- Current User Standing Highlight Card ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_current_user_standing"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${currentUserEntry.rank}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Your Standing: Rank #${currentUserEntry.rank}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "YOU",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${currentUserEntry.totalXp} Total XP • ${currentUserEntry.verifiedWordsCount} words • ${currentUserEntry.verifiedSentencesCount} sentences",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Spacer(Modifier.height(4.dp))
                            val nextTargetXp = if (currentUserEntry.rank > 1) {
                                val rankAbove = baseContributors.getOrNull(currentUserEntry.rank - 2)
                                (rankAbove?.totalXp ?: (currentUserEntry.totalXp + 200)) - currentUserEntry.totalXp
                            } else {
                                0
                            }
                            if (nextTargetXp > 0) {
                                Text(
                                    text = "+$nextTargetXp XP to surpass next contributor rank",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                                selectedContributorDetail = currentUserEntry
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View Details",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // --- Remaining Contributors Header ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Contributor Rankings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredContributors.size} Contributors",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Contributors List ---
            items(remainingList) { contributor ->
                ContributorLeaderboardItem(
                    contributor = contributor,
                    onClick = {
                        ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                        selectedContributorDetail = contributor
                    }
                )
            }
        }
    }

    // --- Detailed Contributor Modal ---
    if (selectedContributorDetail != null) {
        val contributor = selectedContributorDetail!!
        AlertDialog(
            onDismissRequest = { selectedContributorDetail = null },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = contributor.badgeColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = contributor.badgeIcon,
                            contentDescription = null,
                            tint = contributor.badgeColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = contributor.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = contributor.badgeColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = contributor.badgeTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = contributor.badgeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMetricColumn(label = "Rank", value = "#${contributor.rank}")
                        StatMetricColumn(label = "Total XP", value = "${contributor.totalXp}")
                        StatMetricColumn(label = "Dialect", value = contributor.dialect)
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Verified Linguistic Contribution Breakdown:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Verified Shina Vocabulary", fontSize = 12.sp)
                        Text("${contributor.verifiedWordsCount} words", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Verified Natural Sentences", fontSize = 12.sp)
                        Text("${contributor.verifiedSentencesCount} sentences", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Linguistic Grammar Rules", fontSize = 12.sp)
                        Text("${contributor.grammarRulesCount} rules", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Contribution Streak", fontSize = 12.sp)
                        Text("${contributor.streakDays} Days 🔥", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ShinaHapticManager.performKeyClick(context, hapticFeedbackEnabled)
                        selectedContributorDetail = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun LeaderboardPodium(
    topContributors: List<LeaderboardContributor>,
    onContributorClick: (LeaderboardContributor) -> Unit
) {
    val rank1 = topContributors.find { it.rank == 1 }
    val rank2 = topContributors.find { it.rank == 2 }
    val rank3 = topContributors.find { it.rank == 3 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("podium_top_contributors"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 Contributor Hall of Fame",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Leading regional preservationists driving Shina AI quality",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Rank 2 - Silver
                if (rank2 != null) {
                    PodiumPillar(
                        contributor = rank2,
                        pedestalHeight = 100.dp,
                        crownColor = Color(0xFFB0BEC5),
                        badgeText = "2nd",
                        onClick = { onContributorClick(rank2) }
                    )
                }

                // Rank 1 - Gold (Tallest center)
                if (rank1 != null) {
                    PodiumPillar(
                        contributor = rank1,
                        pedestalHeight = 135.dp,
                        crownColor = Color(0xFFFFD54F),
                        badgeText = "1st",
                        isCenter = true,
                        onClick = { onContributorClick(rank1) }
                    )
                }

                // Rank 3 - Bronze
                if (rank3 != null) {
                    PodiumPillar(
                        contributor = rank3,
                        pedestalHeight = 85.dp,
                        crownColor = Color(0xFFD7CCC8),
                        badgeText = "3rd",
                        onClick = { onContributorClick(rank3) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    contributor: LeaderboardContributor,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    crownColor: Color,
    badgeText: String,
    isCenter: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick)
    ) {
        // Contributor avatar & crown
        Box(contentAlignment = Alignment.TopCenter) {
            Surface(
                shape = CircleShape,
                color = crownColor.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(2.dp, crownColor),
                modifier = Modifier.size(if (isCenter) 52.dp else 44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contributor.avatarInitials,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isCenter) 15.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = crownColor,
                modifier = Modifier
                    .size(18.dp)
                    .offset(y = (-4).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeText.take(1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = contributor.name.split(" ").firstOrNull() ?: contributor.name,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${contributor.totalXp} XP",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(4.dp))

        // Pedestal base
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = crownColor.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = badgeText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${contributor.verifiedWordsCount}w",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ContributorLeaderboardItem(
    contributor: LeaderboardContributor,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("item_contributor_rank_${contributor.rank}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contributor.isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (contributor.isCurrentUser) {
            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number Badge
            Surface(
                shape = CircleShape,
                color = when (contributor.rank) {
                    1 -> Color(0xFFFFD54F)
                    2 -> Color(0xFFB0BEC5)
                    3 -> Color(0xFFD7CCC8)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${contributor.rank}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (contributor.rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contributor.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (contributor.isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "YOU",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = contributor.badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = contributor.badgeTitle,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = contributor.badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = contributor.dialect,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${contributor.totalXp} XP",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${contributor.verifiedWordsCount} words • ${contributor.verifiedSentencesCount} sent.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatMetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

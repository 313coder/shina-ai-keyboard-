package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Dynamic Links (defaults initialized with developer's official profiles)
    var websiteUrl by remember { mutableStateOf("https://zaighamtd.netlify.app/") }
    var facebookUrl by remember { mutableStateOf("https://www.facebook.com/share/1D9whwfjD6/") }
    var githubUrl by remember { mutableStateOf("https://github.com/zaigham-abbas") }
    var emailAddress by remember { mutableStateOf("zaighama377@gmail.com") }
    var showEditLinksDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Gilgit Hero Banner ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.gilgit_shina_hero_1785264002858),
                        contentDescription = "Gilgit-Baltistan Karakoram Vista",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.82f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "'SAIPBG' SHINA AI",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "Power by Grazina • Preserving & Digitizing the Shina Language",
                            fontSize = 13.sp,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // --- Developer Profile Card with Portrait Image ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Developer Image
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.custom_developer_photo),
                            contentDescription = "Zaigham Abbas - Lead Developer",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Zaigham Abbas",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Creator & Lead AI Engineer of 'SAIPBG' Shina AI",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gilgit-Baltistan, Pakistan 🇵🇰 • Power by Grazina",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Mission & Cultural Vision",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Shina is an ancient Dardic language spoken across the mighty valleys of Gilgit, Astore, Diamer, Chilas, Punial, Ghizer, and Kohistan. For decades, Shina lacked native digital input keyboards, smart predictive models, and linguistic AI tools.\n\n'SAIPBG' Shina AI (Power by Grazina) was created by Zaigham Abbas to bridge this digital divide, bringing full modern AI assistance, phonetic voice dictation, on-device machine learning, and native keyboard support to millions of Shina speakers globally.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- Complete Feature Breakdown Matrix ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("App Capabilities & Feature Matrix", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureItem(
                        icon = Icons.Default.Keyboard,
                        title = "Native Shina Nasta'liq & Arabic Keyboard",
                        description = "50+ special Shina graphemes (ݜ, ڙ, ڇ, ݨ, ݩ, ݭ), number row, haptics, and custom themes."
                    )
                    FeatureItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Multilingual Gemini AI Writing Suite",
                        description = "Contextual translation, grammar refinement, and content generation between Shina, Urdu, English, Balti, and Burushaski."
                    )
                    FeatureItem(
                        icon = Icons.Default.Mic,
                        title = "Phonetic Voice Typing & Speech Engine",
                        description = "Real-time speech recognition tuned specifically for regional Shina phonetics and accents."
                    )
                    FeatureItem(
                        icon = Icons.Default.Psychology,
                        title = "On-Device AI Self-Training Studio",
                        description = "Local machine learning engine that trains on verified sentences without sending private data to cloud servers."
                    )
                    FeatureItem(
                        icon = Icons.Default.Insights,
                        title = "User Contributions & Model Progress Tracker",
                        description = "Visual impact tracking showing words added, sentences verified, vocabulary coverage, and accuracy gain."
                    )
                    FeatureItem(
                        icon = Icons.Default.LaptopMac,
                        title = "Desktop, Chromebook & DeX Universal Mode",
                        description = "Adaptive Navigation Rail layout on wide screens, multi-window freeform support, and physical QWERTY transliteration."
                    )
                }
            }
        }

        // --- Developer Web Links & Social Media Connections ---
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Official Links & Contact", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        TextButton(
                            onClick = { showEditLinksDialog = true },
                            modifier = Modifier.testTag("about_edit_links_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Links", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Links", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ContactRow(
                        icon = Icons.Default.Public,
                        label = "Official Portfolio & Website",
                        value = websiteUrl,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    )

                    ContactRow(
                        icon = Icons.Default.Share,
                        label = "Facebook Profile",
                        value = facebookUrl,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    )

                    ContactRow(
                        icon = Icons.Default.Email,
                        label = "Official Email Inquiries",
                        value = emailAddress,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$emailAddress")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    )

                    ContactRow(
                        icon = Icons.Default.Code,
                        label = "Developer GitHub Profile",
                        value = githubUrl,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    )
                }
            }
        }

        // --- Technical Specifications Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Technical Architecture", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    TechRow("Platform", "Android, ChromeOS & Desktop (Universal)")
                    TechRow("UI Framework", "Jetpack Compose & Material 3")
                    TechRow("Local Database", "Room SQLite with Migration V4")
                    TechRow("AI Architecture", "Hybrid On-Device N-Gram Neural + Cloud Gemini")
                    TechRow("Target Audience", "Gilgit-Baltistan & Global Shina Community")
                }
            }
        }

        // --- Legal & Version Footer ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { showPrivacyDialog = true }) {
                    Text("Privacy Policy")
                }
                TextButton(onClick = { showTermsDialog = true }) {
                    Text("Terms of Service")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Shina AI Keyboard v1.2.0 Universal • Created with ❤️ by Zaigham Abbas",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // --- EDIT LINKS DIALOG ---
    if (showEditLinksDialog) {
        var tempWeb by remember { mutableStateOf(websiteUrl) }
        var tempFb by remember { mutableStateOf(facebookUrl) }
        var tempGh by remember { mutableStateOf(githubUrl) }
        var tempEmail by remember { mutableStateOf(emailAddress) }

        AlertDialog(
            onDismissRequest = { showEditLinksDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        websiteUrl = tempWeb
                        facebookUrl = tempFb
                        githubUrl = tempGh
                        emailAddress = tempEmail
                        showEditLinksDialog = false
                    }
                ) {
                    Text("Save Links")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditLinksDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Edit Developer Links", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempWeb,
                        onValueChange = { tempWeb = it },
                        label = { Text("Official Website URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempFb,
                        onValueChange = { tempFb = it },
                        label = { Text("Facebook Profile URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempGh,
                        onValueChange = { tempGh = it },
                        label = { Text("GitHub URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = { Button(onClick = { showPrivacyDialog = false }) { Text("Close") } },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Shina AI Keyboard is built with strict privacy-first principles. We do NOT store or log passwords, payment card info, bank details, or private messages. Anonymous typing frequency data is only collected with explicit user opt-in for language digitization.",
                    fontSize = 13.sp
                )
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            confirmButton = { Button(onClick = { showTermsDialog = false }) { Text("Close") } },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "By using Shina AI Keyboard, you agree to respect intellectual property and community guidelines when contributing words or voice datasets for the Shina language preservation project.",
                    fontSize = 13.sp
                )
            }
        )
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

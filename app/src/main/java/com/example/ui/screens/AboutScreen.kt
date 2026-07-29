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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)
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
                            text = "SHINA AI KEYBOARD",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            text = "Preserving & Digitizing the Shina Language",
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Creator & Lead Developer of Shina AI",
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
                            text = "Gilgit-Baltistan, Pakistan 🇵🇰",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Our Mission",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Preserve, digitize, and empower the Shina language using modern Artificial Intelligence, intelligent autocorrect, and voice dictation for future generations.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
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
                        Text("Official Links & Social Media", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        TextButton(onClick = { showEditLinksDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Links", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Links", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Official Website Link
                    ContactRow(
                        icon = Icons.Default.Public,
                        label = "Official Website",
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

                    // Facebook Account Link
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

                    // Email Link
                    ContactRow(
                        icon = Icons.Default.Email,
                        label = "Official Email Support",
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

                    // GitHub / Portfolio Link
                    ContactRow(
                        icon = Icons.Default.Code,
                        label = "Developer GitHub Repository",
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
                text = "Shina AI Keyboard v1.0.0 Pro • Created by Zaigham Abbas",
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
fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

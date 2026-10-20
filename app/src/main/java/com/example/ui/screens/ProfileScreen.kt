package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ZeedViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ZeedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var defaultQuality by remember { mutableStateOf("4K Ultra HD (2160p)") }
    var defaultAudio by remember { mutableStateOf("พากย์ไทย (Thai Dub)") }
    var wifiOnlyDownload by remember { mutableStateOf(true) }
    var hardwareAcceleration by remember { mutableStateOf(true) }
    var cacheSizeMb by remember { mutableStateOf(1240) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "โปรไฟล์ & การตั้งค่า",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // VIP Member Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CinemaSurface,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(ZeedGold4K, ZeedCrimson))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vip_member_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(ZeedGold4K, Color(0xFFFF9100)))
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "VIP",
                                        tint = CinemaBlack,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "สมาชิก VIP 4K Ultra Gold",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = ZeedGold4K
                                    )
                                    Text(
                                        text = "zeed4k_user@cinema.com",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ZeedCrimson)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ตลอดชีพ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        HorizontalDivider(color = CinemaCardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            BenefitChip(icon = Icons.Default.Hd, label = "4K 60fps HDR")
                            BenefitChip(icon = Icons.Default.VolumeUp, label = "Dolby Atmos 5.1")
                            BenefitChip(icon = Icons.Default.Download, label = "ดาวน์โหลดไม่จำกัด")
                        }
                    }
                }
            }

            // Streaming Settings Section
            item {
                Text(
                    text = "การเล่นวิดีโอ & สตรีมมิ่ง",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CinemaSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Quality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    defaultQuality = if (defaultQuality.contains("4K")) "1080p FHD" else "4K Ultra HD (2160p)"
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "ความละเอียดเริ่มต้น", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Text(text = defaultQuality, style = MaterialTheme.typography.labelSmall, color = ZeedGold4K)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }

                        HorizontalDivider(color = CinemaCardBorder)

                        // Default Audio
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    defaultAudio = if (defaultAudio.contains("พากย์ไทย")) "เสียงในฟิล์ม (Original)" else "พากย์ไทย (Thai Dub)"
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "ภาษาเสียงเริ่มต้น", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Text(text = defaultAudio, style = MaterialTheme.typography.labelSmall, color = ZeedCyanNeon)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }

                        HorizontalDivider(color = CinemaCardBorder)

                        // Hardware acceleration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "ฮาร์ดแวร์เร่งความเร็ว 4K (GPU)", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Text(text = "ช่วยให้เล่นภาพลื่นไหลและประหยัดแบตเตอรี่", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Switch(
                                checked = hardwareAcceleration,
                                onCheckedChange = { hardwareAcceleration = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ZeedCrimson
                                )
                            )
                        }

                        HorizontalDivider(color = CinemaCardBorder)

                        // Wi-Fi only download
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "ดาวน์โหลดผ่าน Wi-Fi เท่านั้น", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Text(text = "ป้องกันการใช้อินเทอร์เน็ตมือถือเกินขนาด", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Switch(
                                checked = wifiOnlyDownload,
                                onCheckedChange = { wifiOnlyDownload = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ZeedCrimson
                                )
                            )
                        }
                    }
                }
            }

            // Storage & Maintenance Section
            item {
                Text(
                    text = "หน่วยความจำ & ข้อมูล",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CinemaSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "แคชวิดีโอชั่วคราว", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                Text(text = "$cacheSizeMb MB", style = MaterialTheme.typography.labelSmall, color = ZeedGold4K)
                            }
                            Button(
                                onClick = {
                                    cacheSizeMb = 0
                                    Toast.makeText(context, "ล้างแคชวิดีโอเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CinemaSurface),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(CinemaCardBorder)
                                )
                            ) {
                                Text(text = "ล้างแคช", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // App Version & About
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Zeed4K Ultra HD Streaming",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "เวอร์ชัน 2.4.0 (Build 2026.10)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "ดูหนังออนไลน์ หนังชนโรง คมชัดระดับ 4K UHD",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ZeedGold4K, modifier = Modifier.size(20.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
    }
}

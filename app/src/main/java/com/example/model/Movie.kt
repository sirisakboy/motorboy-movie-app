package com.example.model

enum class MovieCategory(val displayName: String, val iconEmoji: String) {
    ALL("ทั้งหมด", "🔥"),
    CINEMA_NEW("หนังชนโรง", "🎬"),
    THAI_MOVIES("หนังไทย", "🇹🇭"),
    ACTION_SCIFI("แอคชั่น/ไซไฟ", "⚡"),
    HORROR("สยองขวัญ", "👻"),
    ANIME("อนิเมะ", "🎌"),
    SERIES("ซีรีส์", "📺")
}

data class Movie(
    val id: String,
    val title: String,
    val originalTitle: String,
    val category: MovieCategory,
    val resolution: String = "4K UHD",
    val audio: String = "พากย์ไทย 5.1 + ซับไทย",
    val rating: Double = 8.5,
    val year: Int = 2026,
    val duration: String = "2 ชม. 15 นาที",
    val ageRating: String = "15+",
    val synopsis: String,
    val director: String,
    val cast: List<String>,
    val drawableResId: Int? = null,
    val backdropResId: Int? = null,
    val isTrending: Boolean = false,
    val isCinemaRelease: Boolean = false,
    val isExclusive4K: Boolean = true,
    val bitrate: String = "24.5 Mbps",
    val viewsCount: String = "1.2M",
    val videoQualityOptions: List<String> = listOf("4K 2160p (HDR 60fps)", "1080p FHD (VIP)", "720p HD (ประหยัดเน็ต)"),
    val audioTracks: List<String> = listOf("พากย์ไทย 5.1 Dolby Audio", "เสียงในฟิล์ม (Original Soundtrack)"),
    val subtitles: List<String> = listOf("ซับไทย (Thai Sub)", "English Sub", "ปิดคำบรรยาย (Off)"),
    val servers: List<String> = listOf("เซิร์ฟเวอร์ VIP 4K Ultra", "เซิร์ฟเวอร์สำรอง CDN Asia", "เซิร์ฟเวอร์สำรอง Google Cloud")
)

data class WatchHistoryItem(
    val movieId: String,
    val currentPositionSeconds: Int,
    val totalDurationSeconds: Int,
    val lastWatchedTimestamp: Long = System.currentTimeMillis()
) {
    val progress: Float
        get() = if (totalDurationSeconds > 0) currentPositionSeconds.toFloat() / totalDurationSeconds else 0f
}

data class DownloadItem(
    val movieId: String,
    val quality: String = "4K UHD",
    val fileSizeMb: Int = 3850,
    val progressPercent: Int = 100,
    val isCompleted: Boolean = true
)

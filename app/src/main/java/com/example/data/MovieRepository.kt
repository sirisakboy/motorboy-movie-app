package com.example.data

import com.example.R
import com.example.model.DownloadItem
import com.example.model.Movie
import com.example.model.MovieCategory
import com.example.model.WatchHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MovieRepository {

    private val allMoviesList = listOf(
        Movie(
            id = "zeed-001",
            title = "สงครามอวกาศทะลุมิติ: ไครซิส 4K",
            originalTitle = "Cosmic Odyssey: Continuum",
            category = MovieCategory.ACTION_SCIFI,
            resolution = "4K UHD 60fps",
            audio = "พากย์ไทย 5.1 Dolby + ซับไทย",
            rating = 9.2,
            year = 2026,
            duration = "2 ชม. 38 นาที",
            ageRating = "13+",
            synopsis = "มหากาพย์การเดินทางข้ามมิติจักรวาล เมื่อกลุ่มนักบินอวกาศต้องเดินทางผ่านรูหนอนลึกลับเพื่อค้นหาโลกใบใหม่และไขปริศนาแห่งกาลเวลา ภาพคมชัดระดับ 4K Master คมชัดสูงสุดพร้อมระบบเสียงเซอร์ราวด์รอบทิศทาง",
            director = "คริสโตเฟอร์ แวนซ์",
            cast = listOf("แมทธิว สเตอร์ลิง", "เจสสิก้า ชาสเตน", "เดวิด กิออนดี้"),
            drawableResId = R.drawable.img_hero_banner,
            backdropResId = R.drawable.img_hero_banner,
            isTrending = true,
            isCinemaRelease = true,
            isExclusive4K = true,
            bitrate = "32.4 Mbps",
            viewsCount = "2.8M"
        ),
        Movie(
            id = "zeed-002",
            title = "ธี่หยด 2: สมิงเขาขวาง",
            originalTitle = "Tee Yod 2: Ghost Mountain",
            category = MovieCategory.HORROR,
            resolution = "4K UHD",
            audio = "เสียงไทยแท้ 5.1 Dolby Atmos",
            rating = 8.8,
            year = 2026,
            duration = "2 ชม. 05 นาที",
            ageRating = "18+",
            synopsis = "การกลับมาของตำนานความสยองขวัญสุดขีดในป่าดงดิบ เมื่อ 'ยักษ์' ต้องเผชิญหน้ากับความแค้นของวิญญาณร้ายผีชุดดำที่ตามล่าครอบครัวของเขาจนถึงดงสมิงเขาขวาง ความหลอนครั้งใหม่ที่ระทึกจนนั่งไม่ติดเก้าอี้",
            director = "ทวีวัฒน์ วันทา",
            cast = listOf("ณเดชน์ คูกิมิยะ", "เดนิส เจลีลชา", "จูเนียร์ กาจบัณฑิต"),
            drawableResId = R.drawable.img_thai_horror,
            backdropResId = R.drawable.img_thai_horror,
            isTrending = true,
            isCinemaRelease = true,
            isExclusive4K = true,
            bitrate = "28.1 Mbps",
            viewsCount = "3.4M"
        ),
        Movie(
            id = "zeed-003",
            title = "นีออน ฮันเตอร์: สงครามจักรกลทมิฬ",
            originalTitle = "Neon Hunter: Cybernetic Retribution",
            category = MovieCategory.ACTION_SCIFI,
            resolution = "4K UHD HDR10+",
            audio = "พากย์ไทย + ซับไทย",
            rating = 8.7,
            year = 2026,
            duration = "2 ชม. 12 นาที",
            ageRating = "18+",
            synopsis = "ในนครเมโทรโพลิสปี 2099 นักล่าค่าหัวไซบอร์กต้องสืบหาความจริงเบื้องหลังการกบฏของเอไอคอร์ระดับสูง สู่การต่อสู้ด้วยแสงนีออนและความระห่ำไร้ขีดจำกัด ภาพระดับ 4K HDR คอนทราสต์สุดคม",
            director = "เคนจิ ซาโตะ",
            cast = listOf("ไคเลอร์ ไรท์", "เรนะ คาวาโมโตะ", "มาร์ค สตรอง"),
            drawableResId = R.drawable.img_action_scifi,
            backdropResId = R.drawable.img_action_scifi,
            isTrending = true,
            isCinemaRelease = false,
            isExclusive4K = true,
            bitrate = "29.8 Mbps",
            viewsCount = "1.9M"
        ),
        Movie(
            id = "zeed-004",
            title = "มหาเวทย์มังกรสวรรค์: กำเนิดราชันย์ดารา",
            originalTitle = "Celestial Dragon: Stellar Rebirth",
            category = MovieCategory.ANIME,
            resolution = "4K UHD 60fps",
            audio = "พากย์ไทย + ญี่ปุ่นซับไทย",
            rating = 9.4,
            year = 2026,
            duration = "1 ชม. 54 นาที",
            ageRating = "13+",
            synopsis = "อนิเมะฟอร์มยักษ์แห่งปี การต่อสู้ของเด็กหนุ่มผู้สืบทอดพลังสายเลือดมังกรทองโบราณที่ต้องผนึกรอยแยกแห่งห้วงดวงดาว แอนิเมชันระดับ 4K Ultra 60fps สีสันตระการตาและฉากต่อสู้ที่ลื่นไหลระดับโลก",
            director = "ชินจิ ทาคามูระ",
            cast = listOf("ฮิโรกิ คาจิ", "ยูกิ อาโออิ", "ทาคาฮิโระ ซากุไร"),
            drawableResId = R.drawable.img_anime_fantasy,
            backdropResId = R.drawable.img_anime_fantasy,
            isTrending = true,
            isCinemaRelease = true,
            isExclusive4K = true,
            bitrate = "26.0 Mbps",
            viewsCount = "2.5M"
        ),
        Movie(
            id = "zeed-005",
            title = "หลานม่า: ความทรงจำนิรันดร์",
            originalTitle = "How to Make Millions Before Grandma Dies",
            category = MovieCategory.THAI_MOVIES,
            resolution = "4K UHD",
            audio = "เสียงไทยต้นฉบับ 5.1",
            rating = 9.3,
            year = 2025,
            duration = "2 ชม. 06 นาที",
            ageRating = "ทั่วไป",
            synopsis = "เรื่องราวซาบซึ้งกินใจของหลานชายที่ลาออกจากงานเพื่อมาดูแลอาม่าที่กำลังป่วยหนัก ความสัมพันธ์ที่ค่อยๆ ถักทอสายใยแห่งความรักอันบริสุทธิ์ การันตีรายได้ถล่มทลายทั่วเอเชีย",
            director = "พัฒน์ บุญนิธิพัฒน์",
            cast = listOf("บิวกิ้น พุฒิพงศ์", "แต๋ว อุษา เสมคำ", "เผือก พงศธร"),
            drawableResId = R.drawable.img_thai_horror,
            backdropResId = R.drawable.img_thai_horror,
            isTrending = false,
            isCinemaRelease = false,
            isExclusive4K = true,
            bitrate = "22.0 Mbps",
            viewsCount = "4.1M"
        ),
        Movie(
            id = "zeed-006",
            title = "ดูน ภาคสอง: กษัตริย์ทะเลทราย 4K",
            originalTitle = "Dune: Part Two (IMAX Enhanced)",
            category = MovieCategory.ACTION_SCIFI,
            resolution = "4K UHD IMAX",
            audio = "พากย์ไทย + ซับไทย",
            rating = 9.0,
            year = 2025,
            duration = "2 ชม. 46 นาที",
            ageRating = "13+",
            synopsis = "พอล อัทรีเดส รวมพลังกับชนเผ่าเฟรเมนแห่งดาวอาร์ราคิส เปิดศึกชำระแค้นศัตรูที่ทำลายตระกูลของเขา สู่การทำนายแห่งจักรวาลที่สะเทือนทั้งกาแล็กซี มาสเตอร์ IMAX 4K เต็มตา",
            director = "เดนิส วิลเนิฟ",
            cast = listOf("ทิโมธี ชาลาเมต์", "เซนเดยา", "รีเบคก้า เฟอร์กูสัน"),
            drawableResId = R.drawable.img_hero_banner,
            backdropResId = R.drawable.img_hero_banner,
            isTrending = true,
            isCinemaRelease = false,
            isExclusive4K = true,
            bitrate = "35.2 Mbps",
            viewsCount = "3.9M"
        ),
        Movie(
            id = "zeed-007",
            title = "สัปเหร่อ 2: ป่าช้าแตก",
            originalTitle = "The Undertaker II",
            category = MovieCategory.THAI_MOVIES,
            resolution = "4K UHD",
            audio = "เสียงอีสาน/ไทยแท้ 5.1",
            rating = 8.6,
            year = 2026,
            duration = "2 ชม. 10 นาที",
            ageRating = "15+",
            synopsis = "ภาคต่อภาพยนตร์ขวัญใจคนไทย เมื่อพิธีกรรมโบราณแห่งความตายถูกปลุกขึ้นใหม่อีกครั้ง เซียงและพวกต้องหาทางสงบวิญญาณเร่ร่อนในหมู่บ้านโนนคูณ ความเฮฮาผสมความหลอนและปรัชญาชีวิต",
            director = "ธิติ ศรีนวล",
            cast = listOf("ชาติชาย ชินศรี", "ณัฐวุฒิ แสนยะบุตร", "อัจฉรียา ศรีทา"),
            drawableResId = R.drawable.img_thai_horror,
            backdropResId = R.drawable.img_thai_horror,
            isTrending = true,
            isCinemaRelease = true,
            isExclusive4K = true,
            bitrate = "24.0 Mbps",
            viewsCount = "2.9M"
        ),
        Movie(
            id = "zeed-008",
            title = "เดดพูล & วูล์ฟเวอรีน: มหากาพย์ 4K",
            originalTitle = "Deadpool & Wolverine (Uncut)",
            category = MovieCategory.CINEMA_NEW,
            resolution = "4K UHD HDR",
            audio = "พากย์ไทยฮาแตก + ซับไทย",
            rating = 8.9,
            year = 2025,
            duration = "2 ชม. 07 นาที",
            ageRating = "18+",
            synopsis = "การจับคู่สุดกวนและเลือดสาดระหว่างฮีโร่ปากแซ่บเดดพูล กับมนุษย์กลายพันธุ์กรงเล็บเหล็กวูล์ฟเวอรีน เพื่อกอบกู้เส้นเวลาของจักรวาลมาร์เวล พร้อมมุกตลกสุดแสบและการต่อสู้ไร้ขีดจำกัด",
            director = "ชอว์น เลวี่",
            cast = listOf("ไรอัน เรย์โนลด์ส", "ฮิวจ์ แจ็คแมน", "เอ็มม่า คอร์ริน"),
            drawableResId = R.drawable.img_action_scifi,
            backdropResId = R.drawable.img_action_scifi,
            isTrending = true,
            isCinemaRelease = true,
            isExclusive4K = true,
            bitrate = "31.5 Mbps",
            viewsCount = "5.2M"
        ),
        Movie(
            id = "zeed-009",
            title = "มหาเวทย์ผนึกมาร: บทสะเทือนชิบูย่า",
            originalTitle = "Jujutsu Kaisen: Shibuya Incident 4K",
            category = MovieCategory.ANIME,
            resolution = "4K UHD 60fps",
            audio = "พากย์ไทย + ญี่ปุ่นซับไทย",
            rating = 9.5,
            year = 2025,
            duration = "24 ตอน (จบ)",
            ageRating = "18+",
            synopsis = "วันฮาโลวีน ณ ชิบูย่า ม่านเวทมนตร์กักขังผู้คนนับแสน โกโจ ซาโตรุ และเหล่านักไสยเวทย์ต้องเผชิญหน้ากับคำสาประดับพิเศษและแผนการอันมืดมิดของเกะโท สู่การต่อสู้ระดับถล่มเมือง",
            director = "โชตะ โกโชโซโนะ",
            cast = listOf("จุนยะ เอโนกิ", "ยูอิจิ นากามูระ", "ทากาฮิโระ ซากุไร"),
            drawableResId = R.drawable.img_anime_fantasy,
            backdropResId = R.drawable.img_anime_fantasy,
            isTrending = true,
            isCinemaRelease = false,
            isExclusive4K = true,
            bitrate = "27.5 Mbps",
            viewsCount = "4.8M"
        ),
        Movie(
            id = "zeed-010",
            title = "เดอะ สควิด เกม 2: ด่านมรณะ",
            originalTitle = "Squid Game: Season 2 (4K HDR)",
            category = MovieCategory.SERIES,
            resolution = "4K UHD Dolby Vision",
            audio = "พากย์ไทย + เกาหลีซับไทย",
            rating = 8.9,
            year = 2026,
            duration = "8 ตอน (ซีซั่น 2)",
            ageRating = "18+",
            synopsis = "ซองกีฮุนกลับเข้าสู่เกมเดิมพันชีวิตอีกครั้งเพื่อเปิดโปงองค์กรเบื้องหลังการแข่งขันมรณะ ทว่าบททดสอบใหม่เต็มไปด้วยความโหดเหี้ยม จิตวิทยา และพันธมิตรที่ไม่อาจไว้ใจใครได้",
            director = "ฮวังดงฮยอก",
            cast = listOf("อีจองแจ", "อีบยองฮอน", "วีฮาจุน", "อิมซีวาน"),
            drawableResId = R.drawable.img_action_scifi,
            backdropResId = R.drawable.img_action_scifi,
            isTrending = true,
            isCinemaRelease = false,
            isExclusive4K = true,
            bitrate = "28.5 Mbps",
            viewsCount = "6.1M"
        )
    )

    private val _watchlistIds = MutableStateFlow<Set<String>>(setOf("zeed-001", "zeed-002", "zeed-004"))
    val watchlistIds: StateFlow<Set<String>> = _watchlistIds.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<WatchHistoryItem>>(
        listOf(
            WatchHistoryItem("zeed-001", currentPositionSeconds = 4820, totalDurationSeconds = 9480),
            WatchHistoryItem("zeed-002", currentPositionSeconds = 2450, totalDurationSeconds = 7500),
            WatchHistoryItem("zeed-004", currentPositionSeconds = 5100, totalDurationSeconds = 6840)
        )
    )
    val watchHistory: StateFlow<List<WatchHistoryItem>> = _watchHistory.asStateFlow()

    private val _downloads = MutableStateFlow<List<DownloadItem>>(
        listOf(
            DownloadItem("zeed-001", quality = "4K UHD", fileSizeMb = 4850, progressPercent = 100, isCompleted = true),
            DownloadItem("zeed-002", quality = "4K UHD", fileSizeMb = 3920, progressPercent = 100, isCompleted = true)
        )
    )
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    fun getAllMovies(): List<Movie> = allMoviesList

    fun getMovieById(id: String): Movie? = allMoviesList.find { it.id == id }

    fun getTrendingMovies(): List<Movie> = allMoviesList.filter { it.isTrending }

    fun getCinemaNewMovies(): List<Movie> = allMoviesList.filter { it.isCinemaRelease }

    fun getMoviesByCategory(category: MovieCategory): List<Movie> {
        if (category == MovieCategory.ALL) return allMoviesList
        if (category == MovieCategory.CINEMA_NEW) return allMoviesList.filter { it.isCinemaRelease }
        return allMoviesList.filter { it.category == category }
    }

    fun searchMovies(query: String, category: MovieCategory = MovieCategory.ALL): List<Movie> {
        val trimmed = query.trim().lowercase()
        return allMoviesList.filter { movie ->
            val matchCategory = category == MovieCategory.ALL ||
                    (category == MovieCategory.CINEMA_NEW && movie.isCinemaRelease) ||
                    movie.category == category

            val matchQuery = trimmed.isEmpty() ||
                    movie.title.lowercase().contains(trimmed) ||
                    movie.originalTitle.lowercase().contains(trimmed) ||
                    movie.synopsis.lowercase().contains(trimmed) ||
                    movie.director.lowercase().contains(trimmed) ||
                    movie.cast.any { it.lowercase().contains(trimmed) }

            matchCategory && matchQuery
        }
    }

    fun toggleWatchlist(movieId: String) {
        val current = _watchlistIds.value.toMutableSet()
        if (current.contains(movieId)) {
            current.remove(movieId)
        } else {
            current.add(movieId)
        }
        _watchlistIds.value = current
    }

    fun isMovieInWatchlist(movieId: String): Boolean {
        return _watchlistIds.value.contains(movieId)
    }

    fun updateWatchProgress(movieId: String, positionSeconds: Int, totalSeconds: Int) {
        val current = _watchHistory.value.toMutableList()
        val index = current.indexOfFirst { it.movieId == movieId }
        val updated = WatchHistoryItem(movieId, positionSeconds, totalSeconds, System.currentTimeMillis())
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(0, updated)
        }
        _watchHistory.value = current
    }

    fun toggleDownload(movieId: String) {
        val current = _downloads.value.toMutableList()
        val index = current.indexOfFirst { it.movieId == movieId }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(DownloadItem(movieId = movieId, quality = "4K UHD", fileSizeMb = 3900, progressPercent = 100, isCompleted = true))
        }
        _downloads.value = current
    }

    fun isDownloaded(movieId: String): Boolean {
        return _downloads.value.any { it.movieId == movieId && it.isCompleted }
    }
}

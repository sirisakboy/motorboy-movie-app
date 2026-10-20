package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MovieRepository
import com.example.model.DownloadItem
import com.example.model.Movie
import com.example.model.MovieCategory
import com.example.model.WatchHistoryItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

enum class AppTab(val titleTh: String) {
    HOME("หน้าแรก"),
    EXPLORE("หมวดหมู่"),
    SEARCH("ค้นหา"),
    WATCHLIST("รายการของฉัน"),
    PROFILE("โปรไฟล์")
}

data class PlayerState(
    val movie: Movie? = null,
    val isPlaying: Boolean = false,
    val currentPositionSeconds: Int = 0,
    val totalDurationSeconds: Int = 7200,
    val quality: String = "4K 2160p (HDR 60fps)",
    val audioTrack: String = "พากย์ไทย 5.1 Dolby Audio",
    val subtitle: String = "ซับไทย (Thai Sub)",
    val speed: Float = 1.0f,
    val server: String = "เซิร์ฟเวอร์ VIP 4K Ultra",
    val isFullscreen: Boolean = false,
    val showControls: Boolean = true,
    val playbackMode: PlaybackMode = PlaybackMode.AUTO
) {
    val progress: Float
        get() = if (totalDurationSeconds > 0) currentPositionSeconds.toFloat() / totalDurationSeconds else 0f
}

enum class PlaybackMode {
    DAY, NIGHT, AUTO
}

class ZeedViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MovieCategory.ALL)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    private val _selectedDetailMovie = MutableStateFlow<Movie?>(null)
    val selectedDetailMovie: StateFlow<Movie?> = _selectedDetailMovie.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val watchlistIds: StateFlow<Set<String>> = repository.watchlistIds
    val watchHistory: StateFlow<List<WatchHistoryItem>> = repository.watchHistory
    val downloads: StateFlow<List<DownloadItem>> = repository.downloads

    private val _isDownloading = MutableStateFlow<Map<String, Int>>(emptyMap())
    val isDownloading: StateFlow<Map<String, Int>> = _isDownloading.asStateFlow()

    private var playbackJob: Job? = null

    fun togglePlaybackMode() {
        val current = _playerState.value
        val modes = listOf(PlaybackMode.DAY, PlaybackMode.NIGHT, PlaybackMode.AUTO)
        val nextIndex = (modes.indexOf(current.playbackMode) + 1) % modes.size
        _playerState.value = current.copy(playbackMode = modes[nextIndex])
    }

    // ฟังก์ชันดึงโหมดที่ต้องแสดงผลจริง (คำนวณจาก AUTO ตามเวลาเครื่อง)
    fun getEffectivePlaybackMode(mode: PlaybackMode): PlaybackMode {
        if (mode != PlaybackMode.AUTO) return mode
        
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (hour in 6..17) PlaybackMode.DAY else PlaybackMode.NIGHT
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setCategory(category: MovieCategory) {
        _selectedCategory.value = category
    }

    fun openMovieDetail(movie: Movie) {
        _selectedDetailMovie.value = movie
    }

    fun closeMovieDetail() {
        _selectedDetailMovie.value = null
    }

    fun startPlaying(movie: Movie, startPosition: Int = 0) {
        playbackJob?.cancel()
        val totalSecs = when {
            movie.duration.contains("ชม") -> {
                // approximate duration
                7800
            }
            else -> 5400
        }
        val initialPos = if (startPosition > 0) startPosition else {
            // check history
            val history = repository.watchHistory.value.find { it.movieId == movie.id }
            history?.currentPositionSeconds ?: 0
        }

        _playerState.value = PlayerState(
            movie = movie,
            isPlaying = true,
            currentPositionSeconds = initialPos,
            totalDurationSeconds = totalSecs,
            quality = movie.videoQualityOptions.firstOrNull() ?: "4K 2160p (HDR 60fps)",
            audioTrack = movie.audioTracks.firstOrNull() ?: "พากย์ไทย 5.1 Dolby Audio",
            subtitle = movie.subtitles.firstOrNull() ?: "ซับไทย (Thai Sub)",
            server = movie.servers.firstOrNull() ?: "เซิร์ฟเวอร์ VIP 4K Ultra",
            isFullscreen = false,
            showControls = true
        )

        startPlaybackTicker()
    }

    private fun startPlaybackTicker() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _playerState.value
                if (current.isPlaying && current.movie != null) {
                    val nextSec = current.currentPositionSeconds + 1
                    if (nextSec >= current.totalDurationSeconds) {
                        _playerState.value = current.copy(isPlaying = false, currentPositionSeconds = current.totalDurationSeconds)
                        break
                    } else {
                        _playerState.value = current.copy(currentPositionSeconds = nextSec)
                        if (nextSec % 5 == 0) {
                            repository.updateWatchProgress(current.movie.id, nextSec, current.totalDurationSeconds)
                        }
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        val current = _playerState.value
        val newState = !current.isPlaying
        _playerState.value = current.copy(isPlaying = newState)
        if (newState) {
            startPlaybackTicker()
        }
    }

    fun seekTo(seconds: Int) {
        val current = _playerState.value
        val clamped = seconds.coerceIn(0, current.totalDurationSeconds)
        _playerState.value = current.copy(currentPositionSeconds = clamped)
        current.movie?.let {
            repository.updateWatchProgress(it.id, clamped, current.totalDurationSeconds)
        }
    }

    fun seekBy(deltaSeconds: Int) {
        val current = _playerState.value
        seekTo(current.currentPositionSeconds + deltaSeconds)
    }

    fun setQuality(quality: String) {
        _playerState.value = _playerState.value.copy(quality = quality)
    }

    fun setAudioTrack(audio: String) {
        _playerState.value = _playerState.value.copy(audioTrack = audio)
    }

    fun setSubtitle(sub: String) {
        _playerState.value = _playerState.value.copy(subtitle = sub)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(speed = speed)
    }

    fun setServer(server: String) {
        _playerState.value = _playerState.value.copy(server = server)
    }

    fun toggleFullscreen() {
        val current = _playerState.value
        _playerState.value = current.copy(isFullscreen = !current.isFullscreen)
    }

    fun toggleControlsVisibility() {
        val current = _playerState.value
        _playerState.value = current.copy(showControls = !current.showControls)
    }

    fun closePlayer() {
        playbackJob?.cancel()
        val current = _playerState.value
        current.movie?.let {
            repository.updateWatchProgress(it.id, current.currentPositionSeconds, current.totalDurationSeconds)
        }
        _playerState.value = PlayerState()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleWatchlist(movieId: String) {
        repository.toggleWatchlist(movieId)
    }

    fun isWatchlist(movieId: String): Boolean {
        return repository.isMovieInWatchlist(movieId)
    }

    fun downloadMovie(movieId: String) {
        viewModelScope.launch {
            if (repository.isDownloaded(movieId)) {
                repository.toggleDownload(movieId)
                return@launch
            }
            // simulate download progress
            for (p in 10..100 step 15) {
                _isDownloading.value = _isDownloading.value + (movieId to p)
                delay(350)
            }
            _isDownloading.value = _isDownloading.value - movieId
            repository.toggleDownload(movieId)
        }
    }

    fun isDownloaded(movieId: String): Boolean {
        return repository.isDownloaded(movieId)
    }

    fun getAllMovies(): List<Movie> = repository.getAllMovies()

    fun getTrendingMovies(): List<Movie> = repository.getTrendingMovies()

    fun getCinemaNewMovies(): List<Movie> = repository.getCinemaNewMovies()

    fun getMoviesByCategory(category: MovieCategory): List<Movie> = repository.getMoviesByCategory(category)

    fun searchMovies(query: String, category: MovieCategory = MovieCategory.ALL): List<Movie> =
        repository.searchMovies(query, category)

    fun getMovieById(id: String): Movie? = repository.getMovieById(id)
}

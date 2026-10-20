package com.example.data.watchlist

import android.content.Context
import kotlinx.coroutines.flow.Flow

class WatchlistRepository(context: Context) {
    private val watchlistDao = WatchlistDatabase.getDatabase(context).watchlistDao()

    val allWatchlist: Flow<List<WatchlistEntity>> = watchlistDao.getAllWatchlist()

    suspend fun toggleWatchlist(movie: WatchlistEntity) {
        if (watchlistDao.isMovieInWatchlist(movie.movieId)) {
            watchlistDao.removeFromWatchlist(movie)
        } else {
            watchlistDao.addToWatchlist(movie)
        }
    }

    suspend fun isMovieInWatchlist(movieId: String): Boolean {
        return watchlistDao.isMovieInWatchlist(movieId)
    }
}

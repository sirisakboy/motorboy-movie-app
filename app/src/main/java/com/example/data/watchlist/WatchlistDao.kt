package com.example.data.watchlist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_table ORDER BY addedDate DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(movie: WatchlistEntity)

    @Delete
    suspend fun removeFromWatchlist(movie: WatchlistEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_table WHERE movieId = :movieId)")
    suspend fun isMovieInWatchlist(movieId: String): Boolean
}

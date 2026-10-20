package com.example.data.watchlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_table")
data class WatchlistEntity(
    @PrimaryKey val movieId: String,
    val title: String,
    val backdropUrl: String,
    val addedDate: Long = System.currentTimeMillis()
)

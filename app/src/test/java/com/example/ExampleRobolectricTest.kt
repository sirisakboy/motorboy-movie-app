package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MovieRepository
import com.example.model.MovieCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zeed4K", appName)
  }

  @Test
  fun `repository returns movies and filters correctly`() {
    val repo = MovieRepository()
    val movies = repo.getAllMovies()
    assertTrue(movies.isNotEmpty())

    val trending = repo.getTrendingMovies()
    assertTrue(trending.isNotEmpty())

    val searchResult = repo.searchMovies("ธี่หยด")
    assertTrue(searchResult.isNotEmpty())
    assertEquals("Tee Yod 2: Ghost Mountain", searchResult.first().originalTitle)
  }

  @Test
  fun `watchlist toggle functions properly`() {
    val repo = MovieRepository()
    val testId = "zeed-003"
    val initial = repo.isMovieInWatchlist(testId)
    repo.toggleWatchlist(testId)
    assertEquals(!initial, repo.isMovieInWatchlist(testId))
  }
}

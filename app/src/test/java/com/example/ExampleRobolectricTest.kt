package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.TimelineMeta
import com.example.ui.LifeTrackerUiState
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LifeTracker", appName)
  }

  @Test
  fun testDashboardScreenRendering() {
    val meta = TimelineMeta(
        targetYears = 5,
        totalWeeks = 260,
        inceptionTimestamp = System.currentTimeMillis()
    )
    val state = LifeTrackerUiState.Dashboard(
        meta = meta,
        currentWeekIndex = 12,
        selectedWeekIndex = 12,
        weekColors = emptyMap(),
        selectedWeekTasks = emptyList(),
        currentWeekTasks = emptyList()
    )
    composeTestRule.setContent {
        MyApplicationTheme {
            DashboardScreen(
                state = state,
                onSelectWeek = {},
                onAddTask = { _, _, _ -> },
                onToggleTask = {},
                onDeleteTask = {},
                onReset = {}
            )
        }
    }
  }
}


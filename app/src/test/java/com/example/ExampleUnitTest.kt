package com.example

import com.example.model.DateFilterState
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun dateFilterState_isActiveCheck() {
    assertFalse(DateFilterState.All.isActive)
    assertTrue(DateFilterState.Today.isActive)
    assertTrue(DateFilterState.ThisWeek.isActive)
    assertTrue(DateFilterState.SpecificDate(System.currentTimeMillis(), "Today").isActive)
    assertTrue(DateFilterState.CustomRange(1000L, 2000L, "Range").isActive)
  }

  @Test
  fun dateFormat_keysMatch() {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val now = System.currentTimeMillis()
    val key = sdf.format(Date(now))
    assertNotNull(key)
    assertTrue(key.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
  }
}


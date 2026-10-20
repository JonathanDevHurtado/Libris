package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Libris", appName)
  }

  @Test
  fun `verify app resources and colors`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val continueReading = context.getString(R.string.continue_reading)
    assertEquals("Continuar Leyendo", continueReading)
  }
}

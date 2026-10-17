package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.export.DocxExporter
import com.example.model.DocumentStats
import com.example.model.WordCraftTemplates
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
    assertEquals("WordCraft", appName)
  }

  @Test
  fun `sample document contains Open Studio Handbook content`() {
    val sample = WordCraftTemplates.createSampleDocument()
    assertEquals("The Open Studio Handbook", sample.title)
    assertTrue(sample.blocks.isNotEmpty())
    val stats = DocumentStats.calculate(sample)
    assertTrue("Word count should be > 50", stats.words > 50)
  }

  @Test
  fun `docx exporter generates valid zip archive`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sample = WordCraftTemplates.createSampleDocument()
    val docxFile = DocxExporter.exportToDocx(context, sample)
    assertNotNull(docxFile)
    assertTrue("DOCX file should exist", docxFile.exists())
    assertTrue("DOCX file size should be > 100 bytes", docxFile.length() > 100)
  }
}

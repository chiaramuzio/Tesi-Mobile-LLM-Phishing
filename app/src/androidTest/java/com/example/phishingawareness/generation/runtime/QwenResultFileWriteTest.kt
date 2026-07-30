package com.example.phishingawareness.generation.runtime

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QwenResultFileWriteTest {

    @Test
    fun writeResultFile_createsRecoverableExternalFile() {
        val applicationContext =
            ApplicationProvider.getApplicationContext<android.content.Context>()

        val resultDirectory =
            requireNotNull(
                applicationContext.getExternalFilesDir(
                    "qwen-results"
                )
            )

        assertTrue(
            resultDirectory.exists() ||
                    resultDirectory.mkdirs()
        )

        val resultFile =
            File(
                resultDirectory,
                "QWEN_RESULT_FILE_WRITE_PROBE.txt"
            )

        val expectedContent =
            buildString {
                appendLine("QWEN_RESULT_FILE_WRITE_PROBE|SUCCESS")
                appendLine("package=${applicationContext.packageName}")
                appendLine("path=${resultFile.absolutePath}")
            }

        resultFile.writeText(
            expectedContent,
            Charsets.UTF_8
        )

        assertTrue(resultFile.exists())
        assertTrue(resultFile.length() > 0L)
        assertEquals(
            expectedContent,
            resultFile.readText(
                Charsets.UTF_8
            )
        )

        println(
            "QWEN_RESULT_FILE_WRITE_PATH|" +
                    resultFile.absolutePath
        )
    }
}
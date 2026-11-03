
package com.filemaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.widget.*
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextShape
import java.util.zip.ZipInputStream

class TextViewerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        val name = intent.getStringExtra("name") ?: "Документ"
        val archive = intent.getBooleanExtra("archive", false)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 24, 18, 18)
            setBackgroundColor(Color.rgb(16, 17, 22))
        }

        val title = TextView(this).apply {
            text = name
            textSize = 20f
            setTextColor(Color.WHITE)
        }

        val body = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.LTGRAY)
            setPadding(0, 20, 0, 0)
        }

        root.addView(title)
        root.addView(
            ScrollView(this).apply {
                addView(body)
            },
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)

        try {
            val result = contentResolver.openInputStream(uri!!)?.use { input ->

                when {
                    archive -> {
                        val zis = ZipInputStream(input)
                        val entries = mutableListOf<String>()

                        var entry = zis.nextEntry
                        while (entry != null) {
                            entries.add(
                                (if (entry.isDirectory) "📁 " else "📄 ") + entry.name
                            )
                            entry = zis.nextEntry
                        }

                        zis.close()
                        "Содержимое ZIP-архива:\n\n" +
                            entries.joinToString("\n")
                    }

                    name.endsWith(".docx", true) -> {
                        XWPFDocument(input).use { doc ->
                            doc.paragraphs.joinToString("\n\n") { it.text }
                        }
                    }

                    name.endsWith(".pptx", true) -> {
                        XMLSlideShow(input).use { ppt ->
                            ppt.slides.mapIndexed { index, slide ->
                                val texts = slide.shapes
                                    .filterIsInstance<XSLFTextShape>()
                                    .mapNotNull { it.text.takeIf { text -> text.isNotBlank() } }

                                "СЛАЙД ${index + 1}\n\n" +
                                    texts.joinToString("\n")
                            }.joinToString("\n\n--------------------\n\n")
                        }
                    }

                    name.endsWith(".ppt", true) -> {
                        HSLFSlideShow(input).use { ppt ->
                            ppt.slides.mapIndexed { index, slide ->
                                val texts = slide.shapes
                                    .filterIsInstance<HSLFTextShape>()
                                    .mapNotNull { it.text.takeIf { text -> text.isNotBlank() } }

                                "СЛАЙД ${index + 1}\n\n" +
                                    texts.joinToString("\n")
                            }.joinToString("\n\n--------------------\n\n")
                        }
                    }

                    else -> {
                        input.bufferedReader().use { it.readText() }
                    }
                }
            } ?: "Не удалось открыть файл."

            body.text = result.ifBlank {
                "В документе нет извлекаемого текста."
            }

        } catch (e: Exception) {
            body.text = "Ошибка чтения документа:\n${e.message}"
        }
    }
}

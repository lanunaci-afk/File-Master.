package com.filemaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.util.zip.ZipInputStream

class TextViewerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        val name = intent.getStringExtra("name") ?: "Документ"
        val archive = intent.getBooleanExtra("archive", false)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18,24,18,18); setBackgroundColor(Color.rgb(16,17,22)) }
        val title = TextView(this).apply { text = name; textSize = 20f; setTextColor(Color.WHITE) }
        val body = TextView(this).apply { textSize = 15f; setTextColor(Color.LTGRAY); setPadding(0,20,0,0) }
        root.addView(title); root.addView(ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
        try {
            val input = contentResolver.openInputStream(uri!!)!!
            val result = if (archive) {
                val zis = ZipInputStream(input)
                val entries = mutableListOf<String>()
                var e = zis.nextEntry
                while (e != null) { entries.add((if(e.isDirectory) "📁 " else "📄 ") + e.name); e = zis.nextEntry }
                zis.close()
                "Содержимое ZIP-архива:\n\n" + entries.joinToString("\n")
            } else if (name.endsWith(".docx", true)) {
                val doc = XWPFDocument(input)
                doc.paragraphs.joinToString("\n\n") { it.text }
            } else {
                input.bufferedReader().use { it.readText() }
            }
            body.text = result.ifBlank { "В документе нет извлекаемого текста." }
        } catch (e: Exception) {
            body.text = "Не удалось прочитать документ: ${e.message}\n\nПоддерживается просмотр текста DOCX. Старый формат DOC может потребовать внешний офисный просмотрщик."
        }
    }
}

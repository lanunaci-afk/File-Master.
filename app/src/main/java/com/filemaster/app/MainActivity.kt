
package com.filemaster.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private var folder: DocumentFile? = null
    private var currentItems: List<DocumentFile> = emptyList()

    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                runCatching {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                folder = DocumentFile.fromTreeUri(this, uri)
                folder?.let { showFolder(it) }
            }
        }

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                openFile(uri, contentResolver.getType(uri), getFileName(uri))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun getFileName(uri: Uri): String? {
        return runCatching {
            contentResolver.query(
                uri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    private fun handleIncomingIntent(incoming: Intent?) {
        if (incoming == null) return

        val uri: Uri? = incoming.data
            ?: if (android.os.Build.VERSION.SDK_INT >= 33) {
                incoming.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                incoming.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            }

        if (uri != null &&
            (incoming.action == Intent.ACTION_VIEW ||
             incoming.action == Intent.ACTION_SEND)
        ) {
            openFile(uri, incoming.type ?: contentResolver.getType(uri), getFileName(uri))
        }
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 22, 20, 12)
            setBackgroundColor(Color.rgb(16, 17, 22))
        }

        val title = TextView(this).apply {
            text = "File Master"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(title)
        root.addView(label("Управляй документами и папками в одном месте"))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        actions.addView(actionButton("📁  Выбрать папку") {
            folderPicker.launch(null)
        }, weight())

        actions.addView(actionButton("＋  Открыть файл") {
            filePicker.launch(arrayOf("*/*"))
        }, weight())

        root.addView(actions)

        search = EditText(this).apply {
            hint = "Поиск в выбранной папке…"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            setSingleLine(true)
            setPadding(14, 8, 14, 8)
            background = rounded("#222630")
            addTextChangedListener(SimpleTextWatcher { filterItems(it) })
        }

        val sp = LinearLayout.LayoutParams(-1, 52)
        sp.topMargin = 18
        root.addView(search, sp)

        root.addView(label("ФАЙЛЫ И ПАПКИ"))

        val scroll = ScrollView(this)
        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
        showEmpty()
    }

    private fun weight() =
        LinearLayout.LayoutParams(0, 52, 1f).apply {
            marginEnd = 6
        }

    private fun label(s: String) = TextView(this).apply {
        text = s
        textSize = 13f
        setTextColor(Color.rgb(150, 157, 174))
        setPadding(0, 16, 0, 10)
    }

    private fun actionButton(s: String, click: () -> Unit) =
        Button(this).apply {
            text = s
            textSize = 12f
            setTextColor(Color.WHITE)
            background = rounded("#30394B")
            setOnClickListener { click() }
        }

    private fun rounded(hex: String) =
        GradientDrawable().apply {
            setColor(Color.parseColor(hex))
            cornerRadius = 18f
        }

    private fun showEmpty() {
        list.removeAllViews()
        list.addView(
            label("Выбери папку, чтобы просмотреть её содержимое, или открой отдельный файл.")
        )
    }

    private fun showFolder(dir: DocumentFile) {
        currentItems = dir.listFiles().sortedWith(
            compareByDescending<DocumentFile> { it.isDirectory }
                .thenBy { it.name?.lowercase() }
        )
        search.setText("")
        render(currentItems)
    }

    private fun filterItems(q: String) {
        if (folder == null) return
        val filtered = currentItems.filter {
            it.name.orEmpty().contains(q, true)
        }
        render(filtered)
    }

    private fun render(items: List<DocumentFile>) {
        list.removeAllViews()

        if (items.isEmpty()) {
            list.addView(label("Ничего не найдено"))
            return
        }

        items.forEach { file ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12, 12, 8, 12)
                background = rounded("#1D2029")
            }

            val icon = TextView(this).apply {
                text = if (file.isDirectory) "📁" else iconFor(file.name.orEmpty())
                textSize = 23f
                setPadding(0, 0, 12, 0)
            }

            val name = TextView(this).apply {
                text = file.name ?: "Без имени"
                textSize = 15f
                setTextColor(Color.WHITE)
                maxLines = 2
            }

            row.addView(icon)
            row.addView(name, LinearLayout.LayoutParams(0, -2, 1f))

            row.setOnClickListener {
                if (file.isDirectory) {
                    folder = file
                    showFolder(file)
                } else {
                    openFile(
                        file.uri,
                        mimeFor(file.name.orEmpty()),
                        file.name
                    )
                }
            }

            val lp = LinearLayout.LayoutParams(-1, -2)
            lp.bottomMargin = 8
            list.addView(row, lp)
        }
    }

    private fun iconFor(n: String): String =
        when (n.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "📕"
            "doc", "docx", "rtf" -> "📝"
            "zip" -> "🗜️"
            "jpg", "jpeg", "png", "webp" -> "🖼️"
            "mp3", "wav", "m4a" -> "🎵"
            "mp4", "mkv" -> "🎬"
            else -> "📄"
        }

    private fun mimeFor(n: String): String =
        when (n.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "rtf" -> "application/rtf"
            "zip" -> "application/zip"
            else -> "*/*"
        }

    private fun openFile(uri: Uri, mime: String?, name: String?) {
        val type = mime ?: mimeFor(name.orEmpty())

        when {
            type == "application/pdf" ||
                name?.endsWith(".pdf", true) == true -> {
                startActivity(
                    Intent(this, PdfViewerActivity::class.java)
                        .setData(uri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            }

            name?.endsWith(".docx", true) == true ||
                type.contains("word") ||
                name?.endsWith(".doc", true) == true -> {
                startActivity(
                    Intent(this, TextViewerActivity::class.java)
                        .setData(uri)
                        .putExtra("name", name)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            }

            name?.endsWith(".zip", true) == true ||
                type == "application/zip" -> {
                startActivity(
                    Intent(this, TextViewerActivity::class.java)
                        .setData(uri)
                        .putExtra("archive", true)
                        .putExtra("name", name)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            }

            else -> {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, type)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                try {
                    startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(
                        this,
                        "Для этого формата нужен совместимый просмотрщик",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

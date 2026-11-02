package com.filemaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.Gravity
import android.widget.*
import android.net.Uri

class PdfViewerActivity : Activity() {
    private var renderer: PdfRenderer? = null
    private var descriptor: ParcelFileDescriptor? = null
    private var pageIndex = 0
    private lateinit var image: ImageView
    private lateinit var counter: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(12,18,12,12); setBackgroundColor(Color.rgb(16,17,22)) }
        counter = TextView(this).apply { setTextColor(Color.WHITE); gravity = Gravity.CENTER; textSize = 16f }
        image = ImageView(this).apply { adjustViewBounds = true; scaleType = ImageView.ScaleType.FIT_CENTER }
        val buttons = LinearLayout(this).apply { gravity = Gravity.CENTER }
        val prev = Button(this).apply { text = "← Назад"; setOnClickListener { if(pageIndex>0) { pageIndex--; showPage() } } }
        val next = Button(this).apply { text = "Вперёд →"; setOnClickListener { if((renderer?.pageCount ?: 0)>pageIndex+1) { pageIndex++; showPage() } } }
        buttons.addView(prev); buttons.addView(next)
        root.addView(counter); root.addView(ScrollView(this).apply { addView(image) }, LinearLayout.LayoutParams(-1,0,1f)); root.addView(buttons)
        setContentView(root)
        try {
            descriptor = contentResolver.openFileDescriptor(intent.data!!, "r")
            renderer = PdfRenderer(descriptor!!)
            showPage()
        } catch (e: Exception) { counter.text = "PDF ашылмады: ${e.message}" }
    }
    private fun showPage() {
        val page = renderer?.openPage(pageIndex) ?: return
        val scale = 2
        val bitmap = Bitmap.createBitmap(page.width*scale, page.height*scale, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        image.setImageBitmap(bitmap)
        counter.text = "Страница ${pageIndex+1} из ${renderer?.pageCount}"
        page.close()
    }
    override fun onDestroy() { renderer?.close(); descriptor?.close(); super.onDestroy() }
}

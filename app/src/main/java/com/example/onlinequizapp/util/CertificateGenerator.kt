package com.example.onlinequizapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.onlinequizapp.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object CertificateGenerator {

    /*
     * High-resolution certificate design matching the official template:
     * 2048 x 1448 px (2x master scale of 1024 x 724)
     *
     * PDF page:
     * A4 Landscape (842 x 595 pt)
     */
    private const val DESIGN_WIDTH = 2048f
    private const val DESIGN_HEIGHT = 1448f

    private const val A4_WIDTH = 842
    private const val A4_HEIGHT = 595

    /**
     * Generates a high-quality PDF certificate for passing scores (>= 80%).
     */
    fun generateCertificate(
        context: Context,
        studentName: String,
        quizName: String,
        score: Double,
        category: String,
        certificateId: String,
        dateMillis: Long? = null
    ): File? {
        if (score < 80.0) {
            return null
        }

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            A4_WIDTH,
            A4_HEIGHT,
            1
        ).create()

        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // Scale 2048x1448 design coordinates to A4 PDF page
        val scaleX = A4_WIDTH.toFloat() / DESIGN_WIDTH
        val scaleY = A4_HEIGHT.toFloat() / DESIGN_HEIGHT

        canvas.save()
        canvas.scale(scaleX, scaleY)

        drawCertificate(
            context = context,
            canvas = canvas,
            studentName = studentName,
            quizName = quizName,
            score = score,
            category = category,
            certificateId = certificateId,
            dateMillis = dateMillis
        )

        canvas.restore()
        pdf.finishPage(page)

        val certificateDirectory = File(
            context.getExternalFilesDir(null),
            "QuizMaster Certificates"
        )
        if (!certificateDirectory.exists()) {
            certificateDirectory.mkdirs()
        }

        val safeStudentName = studentName.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val safeQuizName = quizName.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(
            certificateDirectory,
            "${safeStudentName}_${safeQuizName}_Certificate.pdf"
        )

        FileOutputStream(file).use { output ->
            pdf.writeTo(output)
        }
        pdf.close()

        return file
    }

    /**
     * Generates a crisp Bitmap representation of the certificate for in-app preview.
     */
    fun generateCertificateBitmap(
        context: Context,
        studentName: String,
        quizName: String,
        score: Double,
        category: String,
        certificateId: String,
        dateMillis: Long? = null
    ): Bitmap? {
        if (score < 80.0) {
            return null
        }

        return try {
            val bitmap = Bitmap.createBitmap(
                DESIGN_WIDTH.toInt(),
                DESIGN_HEIGHT.toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawCertificate(
                context = context,
                canvas = canvas,
                studentName = studentName,
                quizName = quizName,
                score = score,
                category = category,
                certificateId = certificateId,
                dateMillis = dateMillis
            )
            bitmap
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Common drawing pipeline rendering text and template elements onto the 2048x1448 canvas.
     */
    private fun drawCertificate(
        context: Context,
        canvas: Canvas,
        studentName: String,
        quizName: String,
        score: Double,
        category: String,
        certificateId: String,
        dateMillis: Long?
    ) {
        // 1. Draw Pristine Certificate Background Template (2048x1448)
        val background = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.certificate_background
        )
        if (background != null) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                isDither = true
            }
            canvas.drawBitmap(
                background,
                null,
                RectF(0f, 0f, DESIGN_WIDTH, DESIGN_HEIGHT),
                bgPaint
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isSubpixelText = true
            isDither = true
        }

        // 2. Student Name in Calligraphic Script (Great Vibes font)
        val cursiveTypeface = ResourcesCompat.getFont(context, R.font.great_vibes)
            ?: Typeface.create("cursive", Typeface.NORMAL)

        paint.color = Color.rgb(15, 29, 61) // #0F1D3D Dark Navy
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = cursiveTypeface

        var nameSize = 124f
        paint.textSize = nameSize
        val maxNameWidth = 1150f
        val measuredNameWidth = paint.measureText(studentName)
        if (measuredNameWidth > maxNameWidth) {
            nameSize *= (maxNameWidth / measuredNameWidth)
            paint.textSize = nameSize
        }
        canvas.drawText(studentName, 1024f, 580f, paint)

        // 3. Quiz Name (Bold Sans-Serif Indigo)
        paint.color = Color.rgb(67, 56, 202) // #4338CA Indigo
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        var quizSize = 48f
        paint.textSize = quizSize
        val maxQuizWidth = 1200f
        val measuredQuizWidth = paint.measureText(quizName)
        if (measuredQuizWidth > maxQuizWidth) {
            quizSize *= (maxQuizWidth / measuredQuizWidth)
            paint.textSize = quizSize
        }
        canvas.drawText(quizName, 1024f, 734f, paint)

        // 4. Score Percentage (Bold Dark Slate inside Laurel Badge)
        paint.color = Color.rgb(15, 23, 42) // #0F172A Dark Slate
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 86f
        val formattedScore = "${score.roundToInt()}%"
        canvas.drawText(formattedScore, 1024f, 932f, paint)

        // 5. Category (Left Aligned under CATEGORY Card)
        paint.color = Color.rgb(30, 41, 59) // #1E293B
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        var catSize = 30f
        paint.textSize = catSize
        val maxCatWidth = 360f
        val measuredCatWidth = paint.measureText(category)
        if (measuredCatWidth > maxCatWidth) {
            catSize *= (maxCatWidth / measuredCatWidth)
            paint.textSize = catSize
        }
        canvas.drawText(category, 502f, 1076f, paint)

        // 6. Completion Date (Left Aligned under COMPLETION DATE Card)
        val date = if (dateMillis != null && dateMillis > 0) Date(dateMillis) else Date()
        val formattedDate = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date)

        paint.color = Color.rgb(30, 41, 59)
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        var dateSize = 30f
        paint.textSize = dateSize
        val maxDateWidth = 360f
        val measuredDateWidth = paint.measureText(formattedDate)
        if (measuredDateWidth > maxDateWidth) {
            dateSize *= (maxDateWidth / measuredDateWidth)
            paint.textSize = dateSize
        }
        canvas.drawText(formattedDate, 936f, 1076f, paint)

        // 7. Certificate ID (Left Aligned under CERTIFICATE ID Card)
        paint.color = Color.rgb(30, 41, 59)
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        var idSize = 28f
        paint.textSize = idSize
        val maxIdWidth = 400f
        val measuredIdWidth = paint.measureText(certificateId)
        if (measuredIdWidth > maxIdWidth) {
            idSize *= (maxIdWidth / measuredIdWidth)
            paint.textSize = idSize
        }
        canvas.drawText(certificateId, 1350f, 1076f, paint)
    }
}

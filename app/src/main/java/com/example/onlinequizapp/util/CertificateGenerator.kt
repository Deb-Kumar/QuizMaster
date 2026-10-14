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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object CertificateGenerator {

    /*
     * 4K Ultra-High Definition Certificate Design matching the official master template:
     * 4096 x 2896 px (~1.414:1 A4 Landscape aspect ratio, 300+ DPI print grade)
     *
     * PDF page:
     * A4 Landscape (842 x 595 pt)
     */
    private const val DESIGN_WIDTH = 4096f
    private const val DESIGN_HEIGHT = 2896f

    private const val A4_WIDTH = 842
    private const val A4_HEIGHT = 595

    const val VERIFICATION_BASE_URL = "https://quizmaster-dd2f8.web.app/verify/"

    /**
     * Rule 2: Generate and issue a certificate only when quiz_score_percentage > 80.
     * 80.0% -> Ineligible (null)
     * 80.01% -> Eligible
     * 92.0% -> Eligible
     * 100.0% -> Eligible
     */
    fun isEligible(scorePercentage: Double): Boolean {
        return !scorePercentage.isNaN() && !scorePercentage.isInfinite() && scorePercentage > 80.0 && scorePercentage <= 100.0
    }

    /**
     * Generates a high-quality 300 DPI PDF certificate for passing scores (> 80%).
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
        if (!isEligible(score)) {
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

        // Scale 4096x2896 design coordinates to A4 PDF page
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

        val safeStudentName = studentName.replace("[^a-zA-Z0-9_-]".toRegex(), "_").trim('_').ifBlank { "Student" }
        val safeQuizName = quizName.replace("[^a-zA-Z0-9_-]".toRegex(), "_").trim('_').ifBlank { "Quiz" }
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
     * Generates a crisp 4K Bitmap representation of the certificate for in-app preview and export.
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
        if (!isEligible(score)) {
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
            // Fallback to 2048x1448 if device memory is constrained
            try {
                val fallbackBitmap = Bitmap.createBitmap(2048, 1448, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(fallbackBitmap)
                canvas.scale(0.5f, 0.5f)
                drawCertificate(context, canvas, studentName, quizName, score, category, certificateId, dateMillis)
                fallbackBitmap
            } catch (_: Throwable) {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Common drawing pipeline rendering text, signature, and QR code onto the 4K (4096x2896) canvas.
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
        // 1. Draw 4K Master Template Background (Image 2 design, 4096x2896)
        val background = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.certificate_background
        ) ?: BitmapFactory.decodeResource(
            context.resources,
            R.drawable.certificate_template
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
        // Position: Uplifted by 1 to 2 dp, centered between "THIS CERTIFIES THAT" and completion line
        val cursiveTypeface = ResourcesCompat.getFont(context, R.font.great_vibes)
            ?: Typeface.create("cursive", Typeface.NORMAL)

        paint.color = Color.rgb(15, 29, 61) // #0F1D3D Dark Navy
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = cursiveTypeface

        var nameSize = 272f
        paint.textSize = nameSize
        val maxNameWidth = 2360f
        val measuredNameWidth = paint.measureText(studentName)
        if (measuredNameWidth > maxNameWidth) {
            nameSize *= (maxNameWidth / measuredNameWidth)
            paint.textSize = nameSize
        }
        // Baseline uplifted 1-2 dp to 1072f
        canvas.drawText(studentName, 2048f, 1072f, paint)

        // 3. Quiz Name (Bold Sans-Serif Deep Purple / Royal Blue)
        // Position: Font increased by 2 dp (108f), centered in dedicated gap
        paint.color = Color.rgb(59, 30, 180) // #3B1EB4 Deep Purple / Royal Blue
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        var quizSize = 108f // Increased by 2 dp
        paint.textSize = quizSize
        val maxQuizWidth = 2400f
        val measuredQuizWidth = paint.measureText(quizName)
        if (measuredQuizWidth > maxQuizWidth) {
            quizSize *= (maxQuizWidth / measuredQuizWidth)
            paint.textSize = quizSize
        }
        canvas.drawText(quizName, 2048f, 1368f, paint)

        // 4. Score Percentage (ExtraBold Dark Navy inside Laurel Score Capsule)
        // Position: Uplifted 2 dp inside laurel capsule (baseline ~ 1804f)
        paint.color = Color.rgb(15, 23, 42) // #0F172A Dark Slate / Navy
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val formattedScore = "${score.roundToInt()}%"
        paint.textSize = if (formattedScore.length > 3) 160f else 172f
        canvas.drawText(formattedScore, 2048f, 1804f, paint)

        // 5. Category (Left Aligned under CATEGORY label card)
        // Position: x = 996f, baseline = 2152f
        paint.color = Color.rgb(15, 29, 61) // #0F1D3D Dark Navy
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        var catSize = 64f
        paint.textSize = catSize
        val maxCatWidth = 720f
        val measuredCatWidth = paint.measureText(category)
        if (measuredCatWidth > maxCatWidth) {
            catSize *= (maxCatWidth / measuredCatWidth)
            paint.textSize = catSize
        }
        canvas.drawText(category, 996f, 2152f, paint)

        // 6. Completion Date (Left Aligned under COMPLETION DATE label card)
        // Position: x = 1864f, baseline = 2152f
        val date = if (dateMillis != null && dateMillis > 0) Date(dateMillis) else Date()
        val formattedDate = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date)

        paint.color = Color.rgb(15, 29, 61)
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        var dateSize = 64f
        paint.textSize = dateSize
        val maxDateWidth = 760f
        val measuredDateWidth = paint.measureText(formattedDate)
        if (measuredDateWidth > maxDateWidth) {
            dateSize *= (maxDateWidth / measuredDateWidth)
            paint.textSize = dateSize
        }
        canvas.drawText(formattedDate, 1864f, 2152f, paint)

        // 7. Certificate ID (Left Aligned under CERTIFICATE ID label card)
        // Position: x = 2820f, baseline = 2152f
        paint.color = Color.rgb(15, 29, 61)
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)

        var idSize = 60f
        paint.textSize = idSize
        val maxIdWidth = 840f
        val measuredIdWidth = paint.measureText(certificateId)
        if (measuredIdWidth > maxIdWidth) {
            idSize *= (maxIdWidth / measuredIdWidth)
            paint.textSize = idSize
        }
        canvas.drawText(certificateId, 2820f, 2152f, paint)

        // 8. Authorized Signature (Deb - Kumar transparent PNG)
        // Position: Lower-left centered above signature line at y = 2468f
        val signatureBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.authorized_person_signature
        )
        if (signatureBitmap != null) {
            val sigCenterX = 1016f
            val sigBottomY = 2468f
            val sigTargetWidth = 760f
            val sigAspect = signatureBitmap.height.toFloat() / signatureBitmap.width.toFloat()
            val sigTargetHeight = sigTargetWidth * sigAspect
            val sigRect = RectF(
                sigCenterX - (sigTargetWidth / 2f),
                sigBottomY - sigTargetHeight,
                sigCenterX + (sigTargetWidth / 2f),
                sigBottomY
            )
            val sigPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                isDither = true
            }
            canvas.drawBitmap(signatureBitmap, null, sigRect, sigPaint)
        }

        // 9. Machine-Readable QR Code Verification (4K resolution 192x192)
        // Position: Lower-right inside purple rounded box placeholder (x: 3632..3824, y: 2292..2484)
        val verificationUrl = "$VERIFICATION_BASE_URL$certificateId"
        val qrBitmap = generateQrBitmap(verificationUrl, 192)
        if (qrBitmap != null) {
            val qrRect = RectF(3632f, 2292f, 3824f, 2484f)
            val qrPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                isDither = true
            }
            canvas.drawBitmap(qrBitmap, null, qrRect, qrPaint)
        }
    }

    /**
     * Generates a high-contrast machine-readable QR code Bitmap using ZXing.
     */
    fun generateQrBitmap(content: String, size: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
            )
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val black = Color.rgb(15, 23, 42)
            val white = Color.WHITE
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) black else white)
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}

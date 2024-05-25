package com.petscanqr.app

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import com.petscanqr.app.databinding.ActivityQrcodeBinding
import java.io.OutputStream
import java.util.EnumMap
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class QRCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrcodeBinding
    private var mascotaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        mascotaId = intent.getStringExtra("MASCOTA_ID") ?: ""
        generateQRCode(mascotaId)

        binding.btnDownloadQR.setOnClickListener {
            saveQRCodeToDownloads()
        }
    }

    private fun saveQRCodeToDownloads() {
        showDownloadNotification()

        val qrBitmap = (binding.qrCodeImageView.drawable).toBitmap()

        val fileName = "qr_code_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val resolver = contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            val outputStream: OutputStream? = resolver.openOutputStream(it)
            outputStream?.use { stream ->
                qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }

            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, it))
        }

        showDownloadCompleteNotification()
    }

    private fun showDownloadNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "download_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Download Channel", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Cambia esto con tu propio icono de notificación
            .setContentTitle(getString(R.string.downloadQNotifyTittle))
            .setContentText(getString(R.string.downloadQNotifyBody))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setProgress(100, 0, true) // Agrega una barra de progreso

        notificationManager.notify(1, builder.build())
    }

    private fun showDownloadCompleteNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "download_channel"

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Cambia esto con tu propio icono de notificación
            .setContentTitle(getString(R.string.downloadQNotifyCompleteTittle))
            .setContentText(getString(R.string.downloadQNotifyCompleteBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, builder.build())
    }

    private fun generateQRCode(mascotaId: String) {
        val qrCodeText = mascotaId

        val qrCodeWriter = QRCodeWriter()

        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2

            val qrCodeSize = 300

            val bitMatrix: BitMatrix =
                qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints)

            val barcodeBitmap = Bitmap.createBitmap(qrCodeSize, qrCodeSize, Bitmap.Config.ARGB_8888)
            for (x in 0 until qrCodeSize) {
                for (y in 0 until qrCodeSize) {
                    barcodeBitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }

            val logoBitmap = BitmapFactory.decodeResource(resources, R.drawable.pets)

            val finalBitmap = combineBitmaps(barcodeBitmap, logoBitmap)

            binding.qrCodeImageView.setImageBitmap(finalBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun combineBitmaps(qrBitmap: Bitmap, logoBitmap: Bitmap): Bitmap {
        val combined = qrBitmap.copy(qrBitmap.config, true)
        val canvas = Canvas(combined)

        val xPos = (combined.width - logoBitmap.width) / 2
        val yPos = (combined.height - logoBitmap.height) / 2

        canvas.drawBitmap(logoBitmap, xPos.toFloat(), yPos.toFloat(), null)

        return combined
    }
}
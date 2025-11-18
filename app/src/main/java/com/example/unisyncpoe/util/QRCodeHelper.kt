package com.example.unisyncpoe.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Hashtable

/**
 * Helper class for QR code generation and scanning
 */
object QRCodeHelper {
    
    /**
     * Generate QR code bitmap from string data
     */
    fun generateQRCode(data: String, width: Int = 512, height: Int = 512): Bitmap? {
        return try {
            val hints = Hashtable<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints)
            
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: WriterException) {
            null
        }
    }
    
    /**
     * Parse QR code data (expects JSON format)
     */
    fun parseQRData(qrData: String): Map<String, String>? {
        return try {
            // Simple JSON parsing - in production, use proper JSON library
            val map = mutableMapOf<String, String>()
            val pairs = qrData.removeSurrounding("{", "}").split(",")
            pairs.forEach { pair ->
                val keyValue = pair.split(":")
                if (keyValue.size == 2) {
                    val key = keyValue[0].trim().removeSurrounding("\"")
                    val value = keyValue[1].trim().removeSurrounding("\"")
                    map[key] = value
                }
            }
            map
        } catch (e: Exception) {
            null
        }
    }
}


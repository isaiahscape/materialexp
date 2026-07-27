package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.File

object ThumbnailUtils {
    private const val TAG = "ThumbnailUtils"

    fun getApkIcon(context: Context, apkPath: String): Bitmap? {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(apkPath, 0) ?: return null
            val appInfo = packageInfo.applicationInfo ?: return null
            appInfo.sourceDir = apkPath
            appInfo.publicSourceDir = apkPath
            val iconDrawable = appInfo.loadIcon(packageManager)
            
            val bitmap = Bitmap.createBitmap(
                iconDrawable.intrinsicWidth.coerceAtLeast(1),
                iconDrawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
            iconDrawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error loading APK icon: ${e.message}")
            null
        }
    }

    fun getAudioAlbumArt(path: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val art = retriever.embeddedPicture
            if (art != null) {
                BitmapFactory.decodeByteArray(art, 0, art.size)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading album art: ${e.message}")
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

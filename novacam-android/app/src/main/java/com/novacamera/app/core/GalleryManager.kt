package com.novacamera.app.core

import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

data class GalleryItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val dateTaken: Long,
    val mimeType: String,
)

class GalleryManager(private val context: Context) {
    fun latest(limit: Int = 100): List<GalleryItem> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MIME_TYPE,
        )
        // Only show photos NovaCam itself saved (Pictures/NovaCam), not every
        // image on the device — otherwise this pulls in the user's entire
        // camera roll (screenshots, downloads, WhatsApp media, etc.).
        val selection: String?
        val selectionArgs: Array<String>?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
            selectionArgs = arrayOf(Environment.DIRECTORY_PICTURES + "/NovaCam/")
        } else {
            selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            selectionArgs = arrayOf("%/Pictures/NovaCam/%")
        }
        val items = mutableListOf<GalleryItem>()
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            while (cursor.moveToNext() && items.size < limit) {
                val id = cursor.getLong(idColumn)
                items += GalleryItem(
                    id = id,
                    uri = "${MediaStore.Images.Media.EXTERNAL_CONTENT_URI}/$id",
                    displayName = cursor.getString(nameColumn),
                    dateTaken = cursor.getLong(dateColumn),
                    mimeType = cursor.getString(mimeColumn),
                )
            }
        }
        return items
    }
}

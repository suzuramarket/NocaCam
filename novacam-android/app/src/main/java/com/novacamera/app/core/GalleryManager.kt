package com.novacamera.app.core

import android.content.Context
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
        val items = mutableListOf<GalleryItem>()
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
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
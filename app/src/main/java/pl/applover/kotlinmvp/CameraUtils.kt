package pl.applover.kotlinmvp

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.util.ArrayList

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 11/04/2018.
 */
fun Uri.getPositionedBitmap(contentResolver: ContentResolver, maxUploadSize: Int = 1024): Bitmap {
    var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, this)
    try {
        val degrees = getRotation(this, contentResolver)
        if (degrees != 0f)
            bitmap = bitmap.rotate(degrees)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bitmap.compress(maxUploadSize)
}


fun getRotation(fileUri: Uri, contentResolver: ContentResolver): Float {
    val rotation = getRotation(getPathFromUri(fileUri, contentResolver))
    if (rotation == 0f) {
        //if first method returned zero roration angle, check if photo is rotated with second method
        try {
            val orientationColumn = ArrayList<String>()
            orientationColumn.add(MediaStore.Images.ImageColumns.ORIENTATION)
            val cursor = contentResolver.query(fileUri, orientationColumn.toTypedArray(), null, null, null)
            if (!cursor.moveToFirst()) {
                cursor.close()
                return 0f
            }
            val rot = cursor.getInt(0)
            cursor.close()
            return rot.toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0f
        }
    } else return rotation
}

fun getRotation(path: String): Float {
    //First method to get rotation angle for chosen picture
    return try {
        val exifInterface = ExifInterface(path)
        when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } catch (e: Exception) {
        e.printStackTrace()
        0f
    }
}

fun getPathFromUri(fileUri: Uri, contentResolver: ContentResolver): String {
    /**This works for Gallery uris, throws exception for camera (file) uri*/
    val wholeID = DocumentsContract.getDocumentId(fileUri)
    val id = ArrayList<String>()
    id.add(wholeID.split(":")[1])

    val column = ArrayList<String>()
    column.add(MediaStore.Images.Media.DATA)

    val sel = MediaStore.Images.Media._ID + "=?"
    val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column.toTypedArray(), sel, id.toTypedArray(), null)

    var filePath = ""
    val columnIndex = cursor.getColumnIndex(column[0])
    if (cursor.moveToFirst())
        filePath = cursor.getString(columnIndex)
    cursor.close()
    return filePath
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    return try {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    } catch (e: Exception) {
        e.printStackTrace()
        this
    }
}

fun Bitmap.compress(maxUploadSize: Int): Bitmap {
    var scaleFactor = Math.max(this.width / maxUploadSize, this.height / maxUploadSize)
    scaleFactor = if (scaleFactor < 1) 1 else scaleFactor
    return Bitmap.createScaledBitmap(this, this.width / scaleFactor, this.height / scaleFactor, false)
}

fun getPathCameraUri(fileUri: Uri, contentResolver: ContentResolver): String {
    val result: String
    val cursor = contentResolver.query(fileUri, null, null, null, null)
    if (cursor == null) { // Source is Dropbox or other similar local file path
        result = fileUri.path
    } else {
        cursor.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}

fun getRotationCamera(fileUri: Uri, contentResolver: ContentResolver): Float {
    val rotation = getRotation(getPathCameraUri(fileUri, contentResolver))
    if (rotation == 0f) {
        /**if first method returned zero rotation angle, check if photo is rotated with second method
         * second method works on certain phones and might throw an exception */
        try {
            val orientationColumn = java.util.ArrayList<String>()
            orientationColumn.add(MediaStore.Images.ImageColumns.ORIENTATION)
            val cursor = contentResolver.query(fileUri, orientationColumn.toTypedArray(), null, null, null)
            if (!cursor.moveToFirst()) {
                cursor.close()
                return 0f
            }
            val rot = cursor.getInt(0)
            cursor.close()
            return rot.toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0f
        }
    } else return rotation
}

fun Uri.getPositionedCameraPhoto(contentResolver: ContentResolver, maxUploadSize: Int = 1024): Bitmap {
    var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, this)
    try {
        val degrees = getRotationCamera(this, contentResolver)
        if (degrees != 0f)
            bitmap = bitmap.rotate(degrees)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return bitmap.compress(maxUploadSize)
}
package com.valdizz.imageselection

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix


import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import kotlinx.android.synthetic.main.activity_image_selection.*


/**
 * Selecting a picture from the gallery.
 * @autor Vlad Kornev
 * @version 1.1
 */
class ImageSelectionActivity : AppCompatActivity() {

    private var imageUri: Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selection)
        btnSelectImage.setOnClickListener { selectImageFromGallery() }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = INTENT_TYPE }
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let {
                imageUri = it
                if (imageUri != Uri.EMPTY) imageView.setImageBitmap(rotateImageIfRequired(imageUri))
            }
        }
    }

    private fun rotateImageIfRequired(imageUri: Uri): Bitmap {
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        val exifInterface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentResolver.openInputStream(imageUri)?.let { ExifInterface(it) }
        } else {
            imageUri.path?.let { ExifInterface(it) }
        }

        val orientation = exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(imageBitmap, ANGLE_90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(imageBitmap, ANGLE_180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(imageBitmap, ANGLE_270)
            else -> imageBitmap
        }
    }

    private fun rotateImage(image: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val rotatedImage = Bitmap.createBitmap(image, BITMAP_X, BITMAP_Y, image.width, image.height, matrix, true)
        image.recycle()
        return rotatedImage
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(IMAGE_URI, imageUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        imageUri = savedInstanceState?.getParcelable(IMAGE_URI) ?: Uri.EMPTY
        if (imageUri != Uri.EMPTY) imageView.setImageBitmap(rotateImageIfRequired(imageUri))
    }

    private companion object {

        private const val SELECT_IMAGE = 101
        private const val IMAGE_URI = "IMAGE_URI"
        private const val INTENT_TYPE = "image/*"
        private const val ANGLE_90 = 90f
        private const val ANGLE_180 = 180f
        private const val ANGLE_270 = 270f
        private const val BITMAP_X = 0
        private const val BITMAP_Y = 0
    }
}

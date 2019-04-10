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

class ImageSelectionActivity : AppCompatActivity() {

    private val SELECT_IMAGE = 101
    private val IMAGE_URI = "IMAGE_URI"
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selection)
        btnSelectImage.setOnClickListener { selectImageFromGallery() }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            imageUri = data?.data
            imageUri?.let { imageView.setImageBitmap(rotateImageIfRequired(imageUri as Uri)) }
        }
    }

    private fun rotateImageIfRequired(imageUri: Uri): Bitmap {
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

        val exifInterface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val inputStream = contentResolver.openInputStream(imageUri)
            inputStream?.let{ ExifInterface(inputStream) }
        } else {
            val imagePath = imageUri.path
            imagePath?.let { ExifInterface(imagePath) }
        }

        val orientation = exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(imageBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(imageBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(imageBitmap, 270f)
            else -> imageBitmap
        }
    }

    private fun rotateImage(image: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val rotatedImage = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        image.recycle()
        return rotatedImage
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        imageUri?.let { outState.putParcelable(IMAGE_URI, imageUri) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        val uri: Uri? = savedInstanceState?.getParcelable(IMAGE_URI)
        uri?.let {
            imageUri = uri
            imageView.setImageBitmap(rotateImageIfRequired(uri))
        }
    }
}

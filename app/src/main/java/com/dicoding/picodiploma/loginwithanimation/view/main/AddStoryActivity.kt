package com.dicoding.picodiploma.loginwithanimation.view.main

import MainViewModel
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityAddStoryBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private lateinit var viewModel: MainViewModel

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            binding.imagePlaceholder.setImageURI(uri)
        } else {
            showToast("Image selection failed")
        }
    }

    private val launcherCamera = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val imageUri = saveBitmapToFile(bitmap)
            currentImageUri = imageUri
            binding.imagePlaceholder.setImageBitmap(bitmap)
        } else {
            showToast("Image capture failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnUpload.setOnClickListener { uploadStory() }
        binding.btnCamera.setOnClickListener { startCamera() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.loadingProgressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.uploadStatus.collectLatest { isSuccess ->
                if (isSuccess) {
                    showToast("Story uploaded successfully")
                    navigateToMainActivity()
                } else {
                    showToast("Failed to upload story")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { message ->
                message?.let {
                    if (it.isNotEmpty()) {
                        showToast(it)
                    }
                }
            }
        }
    }

    private fun startCamera() {
        launcherCamera.launch(null)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadStory() {
        val description = binding.etDescription.text.toString().trim()
        if (currentImageUri != null && description.isNotBlank()) {
            val file = uriToFile(currentImageUri!!)
            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            viewModel.postStory(descriptionPart, photoPart)
        } else {
            showToast("Please select an image and enter a description")
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val file = File.createTempFile("captured_image", ".jpg", cacheDir).apply {
            FileOutputStream(this).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }
        return Uri.fromFile(file)
    }

    private fun uriToFile(uri: Uri): File {
        val contentResolver = contentResolver
        val file = File.createTempFile("temp_image", ".jpg", cacheDir)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("Failed to open InputStream from URI")
        return file
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}

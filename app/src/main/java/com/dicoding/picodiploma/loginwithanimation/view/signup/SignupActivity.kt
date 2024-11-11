package com.dicoding.picodiploma.loginwithanimation.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.data.remote.request.RegisterRequest
import com.dicoding.picodiploma.loginwithanimation.data.repository.UserRepository
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivitySignupBinding
import com.dicoding.picodiploma.loginwithanimation.di.Injection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = Injection.provideUserRepository(this)

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 8) {
                    val request = RegisterRequest(name, email, password)
                    performRegister(request)
                } else {
                    Toast.makeText(this, "Email tidak valid atau password kurang dari 8 karakter", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performRegister(request: RegisterRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userRepository.registerUser(request)
                withContext(Dispatchers.Main) {
                    if (response.error == false) {
                        showDialogSuccess(request.email)
                    } else {
                        Toast.makeText(this@SignupActivity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignupActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignupActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDialogSuccess(email: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Yeah!")
            setMessage("Akun dengan $email berhasil dibuat. Yuk, login dan belajar coding.")
            setPositiveButton("Lanjut") { _, _ ->
                finish()
            }
            create()
            show()
        }
    }

    private fun playAnimation() {
        val titleAnimation = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).setDuration(500)
        val nameAnimation = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.TRANSLATION_X, -200f, 0f).setDuration(500)
        val emailAnimation = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.TRANSLATION_X, 200f, 0f).setDuration(500)
        val passwordAnimation = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.TRANSLATION_X, -200f, 0f).setDuration(500)
        val signupButtonAnimation = ObjectAnimator.ofFloat(binding.signupButton, View.SCALE_X, 0.5f, 1f).setDuration(500)
        val signupButtonAnimationY = ObjectAnimator.ofFloat(binding.signupButton, View.SCALE_Y, 0.5f, 1f).setDuration(500)

        AnimatorSet().apply {
            playTogether(titleAnimation, nameAnimation, emailAnimation, passwordAnimation, signupButtonAnimation, signupButtonAnimationY)
            start()
        }
    }
}

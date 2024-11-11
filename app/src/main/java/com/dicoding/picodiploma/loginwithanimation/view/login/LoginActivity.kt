package com.dicoding.picodiploma.loginwithanimation.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.common.Resource
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityLoginBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
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
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            viewModel.loginUser(email, password) { response ->
                when (response) {
                    is Resource.Loading -> {
                        binding.loadingProgressBar.visibility = View.VISIBLE
                        binding.loginButton.isEnabled = false
                    }
                    is Resource.Success -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.loginButton.isEnabled = true

                        val loginResponse = response.data
                        if (loginResponse?.loginResult != null) {
                            AlertDialog.Builder(this).apply {
                                setTitle("Success")
                                setMessage("Login successful!")
                                setPositiveButton("Continue") { _, _ ->
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                create()
                                show()
                            }
                        } else {
                            showAlertDialog("Login Failed", "Invalid email or password.")
                        }
                    }
                    is Resource.Error -> {
                        binding.loadingProgressBar.visibility = View.GONE
                        binding.loginButton.isEnabled = true
                        showAlertDialog("Login Failed", response.message ?: "Something went wrong.")
                    }
                }
            }
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Retry", null)
            create()
            show()
        }
    }

    private fun playAnimation() {
        val titleAnimation = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).setDuration(500)
        val messageAnimation = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 0f, 1f).setDuration(500)
        val emailAnimation = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.TRANSLATION_X, -200f, 0f).setDuration(500)
        val passwordAnimation = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.TRANSLATION_X, 200f, 0f).setDuration(500)
        val loginButtonAnimation = ObjectAnimator.ofFloat(binding.loginButton, View.SCALE_X, 0.5f, 1f).setDuration(500)
        val loginButtonAnimationY = ObjectAnimator.ofFloat(binding.loginButton, View.SCALE_Y, 0.5f, 1f).setDuration(500)

        AnimatorSet().apply {
            playTogether(titleAnimation, messageAnimation, emailAnimation, passwordAnimation, loginButtonAnimation, loginButtonAnimationY)
            start()
        }
    }
}

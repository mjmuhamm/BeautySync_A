package com.example.beautysync_kotlin.beautician.info

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.databinding.ActivityStripesTermsOfServiceBinding

class StripesTermsOfService : AppCompatActivity() {
    private lateinit var binding : ActivityStripesTermsOfServiceBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStripesTermsOfServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.webViewClient = WebViewClient()
        binding.webView.apply {
            loadUrl("https://stripe.com/connect-account/legal")
            settings.javaScriptEnabled = true
        }

        binding.backButton.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else { onBackPressedDispatcher.onBackPressed() }

        }

    }
}
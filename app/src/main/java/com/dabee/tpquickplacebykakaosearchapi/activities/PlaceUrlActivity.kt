package com.dabee.tpquickplacebykakaosearchapi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.dabee.tpquickplacebykakaosearchapi.R
import com.dabee.tpquickplacebykakaosearchapi.databinding.ActivityPlaceUrlBinding

class PlaceUrlActivity : AppCompatActivity() {

    val binding by lazy { ActivityPlaceUrlBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.webView.webViewClient= WebViewClient() // 현재 웹뷰안에 웹문서를 열리도록..
        binding.webView.webChromeClient = WebChromeClient() // 웹페이지의 다이얼로그 같은 것들이 발동하도록.

        binding.webView.settings.javaScriptEnabled=true // 웹뷰에서 JS 를 실행하도록 설정.  이 3가지 설정 필수

        var placeUrl:String=intent.getStringExtra("place_url") ?:""
        binding.webView.loadUrl(placeUrl)

    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) binding.webView.goBack()
        else super.onBackPressed()
    }
}
package com.example

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.content.pm.ActivityInfo
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 1. ปลดล็อคการหมุนหน้าจอ ให้หมุนได้อิสระตามผู้ใช้งาน (สำคัญมากสำหรับแอปดูหนัง)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContent {
            MyApplicationTheme {
                WebViewScreen("https://www.zeed4k.com/")
            }
        }
    }
}

@Composable
fun WebViewScreen(url: String) {
    var isLoading by remember { mutableStateOf(true) }
    var webView: WebView? by remember { mutableStateOf(null) }

    androidx.activity.compose.BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    setBackgroundColor(0)

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            isLoading = true
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            if (newProgress == 100) {
                                isLoading = false
                            }
                        }

                        // 2. รองรับการขยายวิดีโอเต็มหน้าจอ (Fullscreen Video)
                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            super.onShowCustomView(view, callback)
                        }
                    }

                    settings.apply {
                        // --- High Performance Video Settings ---
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        
                        // อนุญาตให้วิดีโอเล่นเอง (Autoplay) เพื่อความสะดวกในการดูหนัง
                        mediaPlaybackRequiresUserGesture = false
                        
                        // เพิ่มประสิทธิภาพการแสดงผล (Hardware Acceleration)
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        
                        // Cache สำหรับสตรีมมิ่งวิดีโอขนาดใหญ่
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        
                        allowFileAccess = true
                        allowContentAccess = true
                    }
                    
                    loadUrl(url)
                    webView = this
                }
            }
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

package com.example

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ZeedCrimson
import com.example.ui.theme.ZeedGold4K
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var webView: WebView? = null
    private var fullscreenContainer: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // ปลดล็อคการหมุนหน้าจออิสระตามเซนเซอร์
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setupBackPressHandler()

        setContent {
            MyApplicationTheme {
                WebViewScreen(
                    onWebViewCreated = { webView = it },
                    onShowCustomView = { view, callback ->
                        showFullscreenVideo(view, callback)
                    },
                    onHideCustomView = {
                        hideFullscreenVideo()
                    }
                )
            }
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) {
                    hideFullscreenVideo()
                } else if (webView?.canGoBack() == true) {
                    webView?.goBack()
                } else {
                    // ปิด App เมื่อไม่สามารถถอยกลับได้แล้ว
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun showFullscreenVideo(view: View, callback: WebChromeClient.CustomViewCallback) {
        if (customView != null) {
            callback.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback

        // ซ่อน System Bars เมื่อเข้าสู่โหมดเต็มจอ
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val decorView = window.decorView as ViewGroup
        fullscreenContainer = object : FrameLayout(this) {
            init {
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        }.apply {
            addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            decorView.addView(this, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }

        webView?.visibility = View.GONE
    }

    private fun hideFullscreenVideo() {
        if (customView == null) return

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

        val decorView = window.decorView as ViewGroup
        fullscreenContainer?.let {
            decorView.removeView(it)
        }
        fullscreenContainer = null
        customView = null

        webView?.visibility = View.VISIBLE
        try {
            customViewCallback?.onCustomViewHidden()
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        // ป้องกัน Memory Leak
        webView?.let {
            it.stopLoading()
            it.clearHistory()
            it.removeAllViews()
            it.destroy()
        }
        webView = null
        customView = null
        customViewCallback = null
        fullscreenContainer = null
    }

    override fun onBackPressed() {
        // Method นี้ถูกแทนที่ด้วย setupBackPressHandler() แล้ว
    }
}

@Composable
fun WebViewScreen(
    onWebViewCreated: (WebView) -> Unit,
    onShowCustomView: (View, WebChromeClient.CustomViewCallback) -> Unit,
    onHideCustomView: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableStateOf(0.1f) }
    var isError by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf("https://www.zeed4k.com/") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // WebView
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
                            isError = false
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failMsg: String?
                        ) {
                            isError = true
                            isLoading = false
                        }
                    }
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            loadProgress = newProgress / 100f
                            if (newProgress >= 95) {
                                isLoading = false
                            }
                        }

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            if (view != null && callback != null) {
                                onShowCustomView(view, callback)
                            }
                        }

                        override fun onHideCustomView() {
                            onHideCustomView()
                        }
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        mediaPlaybackRequiresUserGesture = false
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        allowFileAccess = true
                        allowContentAccess = true
                    }
                    
                    loadUrl(currentUrl)
                    onWebViewCreated(this)
                }
            }
        )

        // Cinematic Loading Animation Overlay
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(400)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CinemaDarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ZEED",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ZeedGold4K)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "4K",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color.Black
                            )
                        }
                    }

                    Text(
                        text = "กำลังโหลดภาพยนตร์และซีรีส์ 4K...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )

                    LinearProgressIndicator(
                        progress = { loadProgress },
                        modifier = Modifier
                            .width(200.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ZeedCrimson,
                        trackColor = Color(0xFF1E2638)
                    )
                }
            }
        }

        // Custom Error Screen with Clock & Date (The "Dreamy" Screen)
        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(animationSpec = tween(800)),
            exit = fadeOut(animationSpec = tween(800)),
            modifier = Modifier.fillMaxSize()
        ) {
            ErrorScreen(onRetry = {
                isError = false
                isLoading = true
                // In a real app, you would call webView.reload()
            })
        }
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
    var currentDate by remember { mutableStateOf(SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())) }

    // Update clock every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
    ) {
        // Background Image with Blur and Gradient Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = com.example.R.drawable.img_error_screen),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(10.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                                CinemaDarkBackground
                            )
                        )
                    )
            )
        }

        // Floating Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Digital Clock Animation
            var clockScale by remember { animatable(1f) }
            LaunchedEffect(Unit) {
                while(true) {
                    clockScale.animateTo(1.05f, animationSpec = tween(1000))
                    clockScale.animateTo(1f, animationSpec = tween(1000))
                }
            }

            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 64.sp,
                    letterSpacing = 4.sp
                ),
                color = Color.White,
                modifier = Modifier.scale(clockScale)
            )
            
            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                ),
                color = ZeedGold4K
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Error Message with Glow
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "การเชื่อมต่อขัดข้อง",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "กรุณาตรวจสอบอินเทอร์เน็ตของคุณ\nและลองใหม่อีกครั้ง",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ZeedCrimson)
            ) {
                Text("ลองใหม่อีกครั้ง", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helper extension for scale since I missed importing it
private fun Modifier.scale(scale: Float): Modifier = this.then(
    androidx.compose.ui.draw.scale(scale)
)

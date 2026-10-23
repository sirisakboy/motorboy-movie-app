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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceVariant
import com.example.ui.theme.CinemaCardBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ZeedCrimson
import com.example.ui.theme.ZeedGold4K
import com.example.ui.theme.ZeedCyanNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.*

data class WatchHistory(
    val title: String,
    val episode: String,
    val url: String,
    val progress: Float, // 0.0 to 1.0
    val lastWatchedTime: String
)

object HistoryStorage {
    private const val PREF_NAME = "zeed4k_history_pref"
    private const val KEY_HISTORY = "watch_history_json"

    fun loadHistory(context: android.content.Context): List<WatchHistory> {
        val prefs = context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<WatchHistory>()
        try {
            val jsonArray = org.json.JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    WatchHistory(
                        title = obj.optString("title", ""),
                        episode = obj.optString("episode", "ภาพยนตร์เต็มเรื่อง"),
                        url = obj.optString("url", ""),
                        progress = obj.optDouble("progress", 0.0).toFloat(),
                        lastWatchedTime = obj.optString("lastWatchedTime", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveHistory(context: android.content.Context, list: List<WatchHistory>) {
        try {
            val jsonArray = org.json.JSONArray()
            for (item in list) {
                val obj = org.json.JSONObject().apply {
                    put("title", item.title)
                    put("episode", item.episode)
                    put("url", item.url)
                    put("progress", item.progress.toDouble())
                    put("lastWatchedTime", item.lastWatchedTime)
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREF_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_HISTORY, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class MainActivity : ComponentActivity() {
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var webView: WebView? = null
    private var fullscreenContainer: FrameLayout? = null

    // Interface เพื่อให้ JS ใน WebView เรียกใช้ฟังก์ชันใน App ได้
    inner class WebAppInterface(
        private val onResumeRequested: () -> Unit,
        private val onRecordHistory: (String, String, String, Float) -> Unit
    ) {
        @android.webkit.JavascriptInterface
        fun openContinueWatching() {
            runOnUiThread {
                onResumeRequested()
            }
        }

        @android.webkit.JavascriptInterface
        fun recordWatchHistory(title: String, episode: String, url: String, progress: Float) {
            runOnUiThread {
                onRecordHistory(title, episode, url, progress)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // ป้องกันหน้าจอดับหรือล็อคหน้าจอขณะรับชมภาพยนตร์และใช้งานแอป
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = false

        setupBackPressHandler()

        setContent {
            MyApplicationTheme {
                WebViewScreen(
                    onWebViewCreated = { webView -> 
                        this.webView = webView 
                    },
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

        // มั่นใจว่าหน้าจอไม่ดับในโหมดเต็มจอ
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        view.keepScreenOn = true

        customView = view
        customViewCallback = callback

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val decorView = window.decorView as ViewGroup
        fullscreenContainer = object : FrameLayout(this) {
            init {
                setBackgroundColor(android.graphics.Color.BLACK)
                keepScreenOn = true
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
        // Handled by onBackPressedDispatcher
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Fix: ดึง Context ไว้ด้านบนสุดของ Composable function 
    // เพื่อไม่ให้ละเมิดกฎ @Composable invocations can only happen from the context of a @Composable function
    val context = androidx.compose.ui.platform.LocalContext.current as? MainActivity

    val currentView = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    // State for "Continue Watching" feature
    var showHistorySheet by remember { mutableStateOf(false) }
    val watchHistoryList = remember {
        mutableStateListOf<WatchHistory>().apply {
            if (context != null) {
                addAll(HistoryStorage.loadHistory(context))
            }
        }
    }

    val onRecordHistory: (String, String, String, Float) -> Unit = { title, episode, url, progress ->
        if (title.isNotBlank() && url.isNotBlank() && url != "https://www.zeed4k.com/" && !url.endsWith("zeed4k.com/")) {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeStr = "วันนี้ " + formatter.format(Date())

            val existingIndex = watchHistoryList.indexOfFirst { it.url == url || it.title == title }
            val item = WatchHistory(
                title = title,
                episode = episode,
                url = url,
                progress = progress,
                lastWatchedTime = timeStr
            )

            if (existingIndex >= 0) {
                watchHistoryList.removeAt(existingIndex)
            }
            watchHistoryList.add(0, item)

            context?.let { ctx ->
                HistoryStorage.saveHistory(ctx, watchHistoryList.toList())
            }
        }
    }

    // JS Injection for Continue Watching Button & Native Bridge
    LaunchedEffect(webViewInstance) {
        webViewInstance?.let { webView ->
            webView.addJavascriptInterface(
                context?.WebAppInterface(
                    onResumeRequested = { showHistorySheet = true },
                    onRecordHistory = onRecordHistory
                ) ?: object : Any() {
                    @android.webkit.JavascriptInterface
                    fun openContinueWatching() {}
                    @android.webkit.JavascriptInterface
                    fun recordWatchHistory(title: String, episode: String, url: String, progress: Float) {}
                },
                "AndroidApp"
            )

            val jsCode = """
                (function() {
                    function injectButton() {
                        var drawer = document.getElementById('cvi-nav-drawer');
                        if (drawer && !document.getElementById('app-continue-btn')) {
                            var btn = document.createElement('div');
                            btn.id = 'app-continue-btn';
                            btn.innerHTML = '🎬 ดูต่อล่าสุด';
                            btn.style.cssText = 'background: linear-gradient(45deg, #FFD700, #E63946); color: white; padding: 12px; margin: 15px; border-radius: 10px; text-align: center; font-weight: bold; cursor: pointer; box-shadow: 0 4px 15px rgba(0,0,0,0.3);';
                            btn.onclick = function() { AndroidApp.openContinueWatching(); };

                            var header = drawer.querySelector('.drawer-header');
                            if (header) { header.prepend(btn); } else { drawer.prepend(btn); }
                        }
                    }
                    setInterval(injectButton, 2000);
                })();
            """.trimIndent()
            webView.evaluateJavascript(jsCode, null)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CinemaDarkBackground,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            Surface(
                color = CinemaSurface,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, CinemaCardBorder.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = CinemaSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.height(64.dp)
                ) {
                    val isHome = currentUrl == "https://www.zeed4k.com/" || currentUrl.endsWith("zeed4k.com/")

                    // 1. หน้าแรก
                    NavigationBarItem(
                        selected = isHome,
                        onClick = {
                            currentUrl = "https://www.zeed4k.com/"
                            webViewInstance?.loadUrl("https://www.zeed4k.com/")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "หน้าแรก",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "หน้าแรก",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ZeedGold4K,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = ZeedCrimson
                        )
                    )

                    // 2. ปุ่ม ประวัติการดู (โดดเด่น ไม่ทับซ้อนหน้าแรก)
                    NavigationBarItem(
                        selected = showHistorySheet,
                        onClick = { showHistorySheet = true },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (watchHistoryList.isNotEmpty()) {
                                        Badge(
                                            containerColor = ZeedCrimson,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "${watchHistoryList.size}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "ประวัติการดู",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "ประวัติการดู",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ZeedGold4K,
                            selectedTextColor = ZeedGold4K,
                            unselectedIconColor = ZeedGold4K,
                            unselectedTextColor = ZeedGold4K,
                            indicatorColor = CinemaSurfaceVariant
                        )
                    )

                    // 3. ย้อนกลับ
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (webViewInstance?.canGoBack() == true) {
                                webViewInstance?.goBack()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "ย้อนกลับ",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "ย้อนกลับ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    // 4. รีเฟรช
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            webViewInstance?.reload()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "รีเฟรช",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "รีเฟรช",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CinemaDarkBackground)
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
                    keepScreenOn = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            isLoading = true
                            isError = false
                            if (url != null) currentUrl = url
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            if (url != null) currentUrl = url

                            val trackerJs = """
                                (function() {
                                    try {
                                        var href = window.location.href;
                                        if (!href || href === 'https://www.zeed4k.com/' || href.endsWith('zeed4k.com/')) {
                                            return;
                                        }
                                        var title = document.title || '';
                                        if (!title) return;

                                        var cleanTitle = title
                                            .replace(/ดูหนังออนไลน์|ดูซีรีส์|ดูหนัง|พากย์ไทย|ซับไทย|เต็มเรื่อง|ZEED4K|Zeed4K|4K Ultra HD|4K|HD/gi, '')
                                            .replace(/[-–|]/g, '')
                                            .trim();
                                        if (!cleanTitle) cleanTitle = title;

                                        var epMatch = title.match(/EP\.?\s*(\d+)|ตอนที่\s*(\d+)/i);
                                        var ep = epMatch ? ('EP. ' + (epMatch[1] || epMatch[2])) : 'ภาพยนตร์เต็มเรื่อง';

                                        var progress = 0.05;
                                        var video = document.querySelector('video');
                                        if (video && video.duration && !isNaN(video.duration) && video.duration > 0) {
                                            progress = Math.min(1.0, Math.max(0.05, video.currentTime / video.duration));
                                        }

                                        if (window.AndroidApp && window.AndroidApp.recordWatchHistory) {
                                            window.AndroidApp.recordWatchHistory(cleanTitle, ep, href, progress);
                                        }

                                        if (video && !video.dataset.appTracked) {
                                            video.dataset.appTracked = 'true';
                                            video.addEventListener('timeupdate', function() {
                                                if (video.duration > 0) {
                                                    var p = Math.min(1.0, Math.max(0.05, video.currentTime / video.duration));
                                                    if (window.AndroidApp && window.AndroidApp.recordWatchHistory) {
                                                        window.AndroidApp.recordWatchHistory(cleanTitle, ep, href, p);
                                                    }
                                                }
                                            });
                                        }
                                    } catch(e) {}
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(trackerJs, null)
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
                    webViewInstance = this
                    onWebViewCreated(this)
                }
            }
        )

        // Floating "Continue Watching" Quick Resume Pill (Removed from here to avoid overlap, moved to Sidebar)
        /* 
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
        ) {
            ...
        }
        */

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

        // Custom Error Screen
        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(animationSpec = tween(800)),
            exit = fadeOut(animationSpec = tween(800)),
            modifier = Modifier.fillMaxSize()
        ) {
            ErrorScreen(onRetry = {
                isError = false
                isLoading = true
                webViewInstance?.reload()
            })
        }
        }
    }

    // Continue Watching History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = CinemaSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = ZeedGold4K
                        )
                        Text(
                            text = if (watchHistoryList.isNotEmpty()) "ประวัติการรับชมจริง (${watchHistoryList.size})" else "ประวัติการรับชม",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (watchHistoryList.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    watchHistoryList.clear()
                                    context?.let { ctx ->
                                        HistoryStorage.saveHistory(ctx, emptyList())
                                    }
                                }
                            ) {
                                Text(
                                    text = "ล้างประวัติ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ZeedCrimson
                                )
                            }
                        }
                        IconButton(onClick = { showHistorySheet = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Text(
                    text = "เรื่องและ EP ล่าสุดที่คุณรับชมจริงบน Zeed4K สามารถกดเล่นต่อได้ทันที",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (watchHistoryList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ยังไม่มีประวัติการรับชม",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "เมื่อคุณเริ่มดูภาพยนตร์หรือซีรีส์บน Zeed4K\nระบบจะบันทึกประวัติและ EP ที่ดูค้างไว้ที่นี่โดยอัตโนมัติ",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 350.dp)
                    ) {
                    items(watchHistoryList) { item ->
                        Surface(
                            onClick = {
                                currentUrl = item.url
                                webViewInstance?.loadUrl(item.url)
                                showHistorySheet = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = CinemaSurfaceVariant,
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(CinemaCardBorder, ZeedCrimson.copy(alpha = 0.5f)))
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ZeedCrimson)
                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = item.episode,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                            Text(
                                                text = "• ${item.lastWatchedTime}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                watchHistoryList.remove(item)
                                                context?.let { ctx ->
                                                    HistoryStorage.saveHistory(ctx, watchHistoryList.toList())
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "ลบ",
                                                tint = TextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(ZeedCrimson),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Resume",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }

                                // Progress bar
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "ดูไปแล้ว ${(item.progress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ZeedCyanNeon
                                        )
                                        Text(
                                            text = "4K Ultra HD",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ZeedGold4K
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { item.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = ZeedGold4K,
                                        trackColor = Color(0xFF1E2638)
                                    )
                                }
                            }
                        }
                    }
                }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    // ใช้งาน Local variables สำหรับแสดงวันที่และเวลา
    val localeTh = java.util.Locale("th", "TH")
    val localeEn = java.util.Locale.ENGLISH
    
    val dateTh = remember { SimpleDateFormat("EEEE d MMMM yyyy", localeTh).format(Date()) }
    val dateEn = remember { SimpleDateFormat("EEE dd MMM yyyy", localeEn).format(Date()) }
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", localeEn).format(Date())) }

    // Animation states for a modern look
    val infiniteTransition = rememberInfiniteTransition(label = "error_animations")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", localeEn).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
    ) {
        // Cinematic background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1F2C),
                            CinemaDarkBackground,
                            Color(0xFF080B10)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- New Date & Time Section ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 60.sp,
                        letterSpacing = 4.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.scale(pulseScale)
                )
                Text(
                    text = dateTh,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = ZeedGold4K,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = dateEn.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    ),
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Animated "No Internet" Icon with soft glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            color = ZeedCrimson.copy(alpha = glowAlpha),
                            radius = size.minDimension / 2
                        )
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "No Internet",
                    tint = Color.White,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Error Text
            Text(
                text = "การเชื่อมต่อขัดข้อง",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = "ไม่สามารถเข้าถึงเครือข่ายได้ในขณะนี้\nกรุณาตรวจสอบสัญญาณอินเทอร์เน็ตของคุณ",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Value-Added "Tip" Section (Non-boring Ad)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CinemaCardBorder, RoundedCornerShape(16.dp)),
                color = CinemaSurfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tip",
                            tint = ZeedGold4K,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "คำแนะนำสำหรับคุณ",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ZeedGold4K
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "คุณสามารถตั้งค่า 'ดาวน์โหลดล่วงหน้า' เพื่อเก็บภาพยนตร์ 4K ไว้ดูแบบออฟไลน์ได้ ทำให้รับชมได้ต่อเนื่องแม้ไม่มีเน็ต!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Modern Action Button
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ZeedCrimson),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ลองใหม่อีกครั้ง", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

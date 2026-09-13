package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        FishingForecastApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingForecastApp() {
  val context = LocalContext.current
  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var showInfoDialog by remember { mutableStateOf(false) }

  // Runtime Location Permission Request
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    if (fineLocationGranted || coarseLocationGranted) {
      // Permission granted - trigger location in webview if needed
      webViewInstance?.reload()
    }
  }

  LaunchedEffect(Unit) {
    val fineGranted = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!fineGranted) {
      permissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("main_scaffold"),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Fishing Forecast",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Shore Jigging & Marine Predictor",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
          actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
          IconButton(
            onClick = { webViewInstance?.reload() },
            modifier = Modifier.testTag("refresh_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Ανανέωση Πρόγνωσης"
            )
          }
          IconButton(
            onClick = { showInfoDialog = true },
            modifier = Modifier.testTag("info_button")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Πληροφορίες Αλγορίθμου"
            )
          }
        }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .testTag("webview_container")
    ) {
      FishingWebView(
        onWebViewCreated = { webViewInstance = it }
      )
    }

    if (showInfoDialog) {
      AlgorithmInfoDialog(onDismiss = { showInfoDialog = false })
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FishingWebView(
  onWebViewCreated: (WebView) -> Unit,
  modifier: Modifier = Modifier
) {
  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { ctx ->
      WebView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Use default LAYER_TYPE_NONE so Android WebView utilizes system window compositor
        // without attempting to open non-existent Linux DRM hardware render nodes
        setLayerType(View.LAYER_TYPE_NONE, null)
        setBackgroundColor(android.graphics.Color.parseColor("#070e17"))

        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          databaseEnabled = true
          allowFileAccess = true
          allowContentAccess = true
          setGeolocationEnabled(true)
          cacheMode = WebSettings.LOAD_DEFAULT
          useWideViewPort = true
          loadWithOverviewMode = true
        }

        webChromeClient = object : WebChromeClient() {
          override fun onGeolocationPermissionsShowPrompt(
            origin: String?,
            callback: GeolocationPermissions.Callback?
          ) {
            // Automatically grant origin geolocation in app
            callback?.invoke(origin, true, false)
          }

          override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            Log.d("FishingWebView", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()}")
            return true
          }
        }

        webViewClient = object : WebViewClient() {
          override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
          }

          override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
            Log.w("FishingWebView", "Render process gone; reloading gracefully")
            view?.destroy()
            return true
          }
        }

        loadUrl("file:///android_asset/fishing_forecast.html")
        onWebViewCreated(this)
      }
    },
    update = {
      // WebView updates if needed
    }
  )
}

@Composable
fun AlgorithmInfoDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Αλγόριθμος Fishing Score",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
      )
    },
    text = {
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp)
      ) {
        Text(
          text = "Σχεδιασμένο για Shore Jigging και παράκτιες τεχνικές:\n\n" +
            "• Βαρομετρική Πίεση (30%): Πτώση πίεσης = τροφοληπτικό ξέσπασμα (frenzy) αρπακτικών πριν το μέτωπο.\n\n" +
            "• Άνεμος & Μποφόρ (25%): 2-4 Μποφόρ είναι το ιδανικό παράθυρο για κυματισμό επιφανείας, αφρό και οξυγόνο.\n\n" +
            "• Σελήνη & Solunar (25%): Νέα Σελήνη και Πανσέληνος με +20% bonus κατά τα Solunar Major/Minor παράθυρα.\n\n" +
            "• Κύμα & Swell (20%): Ήπιος κυματισμός 0.3m-0.8m δημιουργεί άριστο αφρό στους κάβους, ενώ >1.5m είναι επικίνδυνο.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Κλείσιμο", color = MaterialTheme.colorScheme.primary)
      }
    }
  )
}


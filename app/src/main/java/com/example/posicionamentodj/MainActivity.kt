package com.example.posicionamentodj

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.posicionamentodj.ui.theme.PosicionamentoDJTheme

class MainActivity : ComponentActivity(), LocationListener {
    private var url: String = "https://www.google.com/maps"
    private val mutableUrl = mutableStateOf(url)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PosicionamentoDJTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(mutableUrl) { getLocation() }
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        mutableUrl.value = "https://www.google.com/maps/search/?api=1&query=" + location.latitude + "," + location.longitude
        Log.i("PosicionamentoDJ", "New url received: " + mutableUrl.value)
    }

    private fun getLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasInternetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocationPermission)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

        if (!hasInternetPermission)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 1)

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5f, this)

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            Toast.makeText(this, "Buscando localização", Toast.LENGTH_LONG).show()
        else
            Toast.makeText(this, "GPS desabilitado", Toast.LENGTH_LONG).show()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainContent(mutableUrl: MutableState<String>, updateLocationCaller: () -> Unit) {
    val url by mutableUrl

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            updateLocationCaller()
            Log.i("PosicionamentoDJ", "New url requested")
        }) {
            Text(text = "BUSCAR LAT LONG")
        }

        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                if (!isInEditMode)
                    settings.javaScriptEnabled = true
                loadUrl(url)
            }
        }, update = {it.loadUrl(url)})
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview(mutableUrl: MutableState<String> = mutableStateOf("https://www.google.com/maps")) {
    PosicionamentoDJTheme {
        MainContent(mutableUrl) {  }
    }
}


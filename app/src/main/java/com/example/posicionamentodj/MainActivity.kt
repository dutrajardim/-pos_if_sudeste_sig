package com.example.posicionamentodj

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.posicionamentodj.ui.theme.PosicionamentoDJTheme
import kotlinx.parcelize.Parcelize

@Parcelize
data class Point (val location: Location?) : Parcelable {
    fun getMapUrl (): String {
        return if (location == null) "https://www.google.com/maps"
            else "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
    }
    override fun toString(): String {
        return if (location == null) ""
            else "Latitude: ${location.latitude}\nLongitude: ${location.longitude}\nAltitude: ${location.altitude}"
    }
}

class MainActivity : ComponentActivity(), LocationListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PosicionamentoDJTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent { getLocation() }
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.i("PosicionamentoDJ", "New point received: $location")
    }

    private fun getLocation(): Point {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasInternetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocationPermission || !hasInternetPermission) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 1)
            return Point(null)
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) Log.i( "PosicionamentoDJ", "Buscando localização")
        else Toast.makeText(this, "GPS desabilitado", Toast.LENGTH_LONG).show()

        return Point(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MainContent(getLocation: () -> Point) {
    val (url, setUrl) = rememberSaveable { mutableStateOf("https://www.google.com/maps") }
    val (pointA, setPointA) = rememberSaveable { mutableStateOf(Point(null)) }
    val (pointB, setPointB) = rememberSaveable { mutableStateOf(Point(null))}

    Column (
        modifier = Modifier
            .padding(bottom = 10.dp, top = 5.dp, start = 5.dp, end = 5.dp)
    ) {

        Button(
            onClick = {
                setPointA(Point(null))
                setPointB(Point(null)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp)
        ) { Text(text = "Limpar")}

        PointController(title = "Ponto A", text = pointA.toString(), onSeeTouch = {setUrl(pointA.getMapUrl())}, onReadTouch = {setPointA(getLocation())})
        PointController(title = "Ponto B", text = pointB.toString(), onSeeTouch = {setUrl(pointB.getMapUrl())}, onReadTouch = {setPointB(getLocation())})

        val context = LocalContext.current

        Button(
            onClick = {
                if (pointA.location !== null && pointB.location !== null) {
                    val result = floatArrayOf(1f)
                    Location.distanceBetween(
                        pointA.location.latitude,
                        pointA.location.longitude,
                        pointA.location.latitude,
                        pointB.location.longitude,
                        result
                    )
                    Toast.makeText(context, "Distância: ${result[0]}", Toast.LENGTH_LONG).show()
                }
                else Toast.makeText(context, "Leia os pontos", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp)
        ) { Text(text = "Calcular distância")}

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

@Composable
fun PointController(title: String, text: String, onSeeTouch: () -> Unit, onReadTouch: () -> Unit) {
    Column {

        Row (
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) { Text(text = title, modifier = Modifier.padding(3.dp)) }

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Column {
                Button(
                    colors = ButtonDefaults.buttonColors(Color.Yellow, Color.Black),
                    modifier = Modifier.width(180.dp),
                    onClick = onReadTouch,
                    shape = RoundedCornerShape(5.dp)
                ) { Text("Ler") }

                Button(
                    colors = ButtonDefaults.buttonColors(Color.Blue),
                    modifier = Modifier.width(180.dp),
                    onClick = onSeeTouch,
                    shape = RoundedCornerShape(5.dp)
                ) { Text("Ver") }
            }
            Text(text = text)
        }
    }

}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun GreetingPreview(mutableUrl: MutableState<String> = mutableStateOf("https://www.google.com/maps")) {
    PosicionamentoDJTheme {
        MainContent { Point(null) }
    }
}


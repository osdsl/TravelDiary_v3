package com.example.traveldiary

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.widget.TextView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class Map : AppCompatActivity() {
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnHomePress = findViewById<Button>(R.id.btn_return_to_main)
        btnHomePress.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Инициализация OSMDroid
        Configuration.getInstance().userAgentValue = packageName
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)

        // Настройка начальной позиции (Москва)
        val Point1 = GeoPoint(55.751574, 37.573856)
        val Point2 = GeoPoint(55.721574, 37.573856)
        val Point3 = GeoPoint(55.751574, 37.543856)
        mapView.controller.setZoom(12.0)
        mapView.controller.setCenter(Point1)

        // Добавление маркера с описанием
        addMarker(Point1, "Маркер 1", "Указатель карты")
        addMarker(Point2, "Маркер 2", "Указатель карты")
        addMarker(Point3, "Маркер 3", "Указатель карты")

    }
    private fun addMarker(point: GeoPoint, title: String, description: String) {
        val marker = Marker(mapView).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            this.title = title
            snippet = description
            setOnMarkerClickListener { _, _ ->
                showInfoWindow()
                true
            }
            icon = resources.getDrawable(org.osmdroid.library.R.drawable.marker_default)
            icon.setTint(Color.RED) // Кастомизация цвета
        }

        // Кастомное InfoWindow
        marker.infoWindow = object : InfoWindow(R.layout.custom_info_window, mapView) {
            override fun onOpen(item: Any?) {
                val marker = item as Marker
                mView.findViewById<TextView>(R.id.title).text = marker.title
                mView.findViewById<TextView>(R.id.description).text = marker.snippet
            }

            override fun onClose() {}
        }

        mapView.overlays.add(marker)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
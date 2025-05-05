package com.example.traveldiary

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivIcon: ImageView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var citySpinner: Spinner
    // Соответствие русских названий и API-имен
    private val cityMap = mapOf(
        "Москва" to "Moscow",
        "Санкт-Петербург" to "Saint Petersburg",
        "Казань" to "Kazan",
        "Сочи" to "Sochi",
        "Екатеринбург" to "Yekaterinburg"
    )
    private val viewModel: WeatherViewModel by viewModels()
    private val apiKey = "2c9b7e4fc8aa65c2f2343539cff9e157"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMapPress = findViewById<Button>(R.id.btn_map)
        btnMapPress.setOnClickListener{
            val intent = Intent(this, Map::class.java)
            startActivity(intent)
        }

        val btnNotesPress = findViewById<Button>(R.id.btn_create_note)
        btnNotesPress.setOnClickListener{
            val intent = Intent(this, Notes::class.java)
            startActivity(intent)
        }

        textView = findViewById(R.id.tv_notes_list)
        dbHelper = DatabaseHelper(this)
        tvTemp = findViewById(R.id.tvTemp)
        tvDescription = findViewById(R.id.tvDescription)
        ivIcon = findViewById(R.id.ivIcon)
        citySpinner = findViewById(R.id.citySpinner)

        loadRecentNotes()
        setupSpinner()
        observeViewModel()

    }

    private fun setupSpinner() {
        // Адаптер для Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.cities,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapter

        // Обработчик выбора города
        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCity = parent?.getItemAtPosition(position).toString()
                val apiCityName = cityMap[selectedCity] ?: "Moscow"
                viewModel.loadWeather(apiCityName, apiKey)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.weatherData.observe(this) { weather ->
            tvTemp.text = "${weather.main.temp}°C"
            tvDescription.text = weather.weather.firstOrNull()?.description ?: "N/A"

//            // Загрузка иконки (пример с Glide)
//            val iconUrl =
//                "https://openweathermap.org/img/wn/${weather.weather.firstOrNull()?.icon}@2x.png"
//            Glide.with(this)
//                .load(iconUrl)
//                .into(ivIcon)
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecentNotes() {
        lifecycleScope.launch {
            try {
                // Проверка пустой базы
                if (dbHelper.getRecentNotes().isEmpty()) {
                    withContext(Dispatchers.IO) {
                        dbHelper.addTestNote("Тестовая заметка 1")
                        dbHelper.addTestNote("Тестовая заметка 2")
                        dbHelper.addTestNote("Тестовая заметка 3")
                    }
                }

                // Повторная загрузка
                val notes = withContext(Dispatchers.IO) {
                    dbHelper.getRecentNotes()
                }

                // Обновляем UI
                if (notes.isEmpty()) {
                    textView.text = getString(R.string.no_notes)
                    return@launch
                }

                val formattedText = notes.joinToString("\n\n")
                textView.text = formattedText

            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка загрузки заметок: ${e.message}")
                textView.text = getString(R.string.load_error)
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.load_error_details, e.message ?: "Неизвестная ошибка"),
                        Snackbar.LENGTH_LONG
                    ).setAction("Повторить") { loadRecentNotes() }.show()
                }
            }
        }
    }

    override fun onDestroy() {
        // Сохраняем резервную копию и закрываем соединение
        lifecycleScope.launch(Dispatchers.IO) {
            dbHelper.backupDatabase()
            dbHelper.close()
        }
        super.onDestroy()
    }
}
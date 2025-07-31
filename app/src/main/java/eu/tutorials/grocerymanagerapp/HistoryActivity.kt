package com.tutorials.grocerymanagerapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyListView: ListView
    private lateinit var historyAdapter: ArrayAdapter<String>
    private val displayList = mutableListOf<String>()
    private var historyMap: HashMap<String, MutableList<String>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyListView = findViewById(R.id.history_list_view)

        // Get map from Intent
        val map = intent.getSerializableExtra("history_map")
        if (map is HashMap<*, *>) {
            historyMap = map as HashMap<String, MutableList<String>>
        }

        rebuildDisplayList()

        historyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        historyListView.adapter = historyAdapter

        // Long click to clear history by date
        historyListView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedItem = displayList[position]
            if (!selectedItem.startsWith("📅 ")) return@setOnItemLongClickListener true

            val date = selectedItem.removePrefix("📅 ").trim()
            historyMap.remove(date)
            rebuildDisplayList()
            historyAdapter.notifyDataSetChanged()
            Toast.makeText(this, "History for $date cleared", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun rebuildDisplayList() {
        displayList.clear()
        val sortedDates = historyMap.keys.sortedDescending() // newest first
        for (date in sortedDates) {
            displayList.add("📅 $date")
            displayList.addAll(historyMap[date] ?: listOf())
        }
    }
}

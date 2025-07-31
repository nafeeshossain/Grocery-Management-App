package com.tutorials.grocerymanagerapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.ImageButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("grocerymanagerapp")  // Load native C++ library
        }
    }

    private val VOICE_INPUT_REQUEST_CODE = 101

    // Native functions
    external fun addItemNative(name: String, quantity: Int, price: Float)
    external fun getItemsNative(): String
    external fun getTotalCostNative(): Float
    external fun clearItemsNative()

    // 🟡 Move historyList here (class-level)
    private val historyMap = HashMap<String, MutableList<String>>()  // key = date, value = list of entries



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI references
        val itemInput = findViewById<EditText>(R.id.item_input)
        val quantityInput = findViewById<EditText>(R.id.quantity_input)
        val priceInput = findViewById<EditText>(R.id.price_input)
        val addButton = findViewById<Button>(R.id.add_button)
        val clearButton = findViewById<Button>(R.id.clear_button)
        val groceryListView = findViewById<TextView>(R.id.grocery_list)
        val totalTextView = findViewById<TextView>(R.id.total_text)
        val budgetInput = findViewById<EditText>(R.id.budget_input)
        val budgetWarning = findViewById<TextView>(R.id.budget_warning)
        val historyButton = findViewById<Button>(R.id.history_button)
        val fullMicButton = findViewById<ImageButton>(R.id.full_mic_button)

        // ➕ Add Button Click
        addButton.setOnClickListener {
            val name = itemInput.text.toString().trim()
            val quantity = quantityInput.text.toString().toIntOrNull() ?: 1
            val price = priceInput.text.toString().toFloatOrNull() ?: 0f

            if (name.isNotEmpty()) {
                addItemNative(name, quantity, price)

                val entry = "$quantity x $name at ₹$price"
                val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                if (!historyMap.containsKey(today)) {
                    historyMap[today] = mutableListOf()
                }
                historyMap[today]?.add(entry)



                // Update UI
                val items = getItemsNative()
                groceryListView.text = items
                val total = getTotalCostNative()
                totalTextView.text = "Total: ₹$total"


                // Budget check
                val budget = budgetInput.text.toString().toFloatOrNull()
                if (budget != null && total > budget) {
                    budgetWarning.text = "⚠ Budget exceeded!"
                    budgetWarning.setTextColor(0xFFFF0000.toInt())
                    budgetWarning.visibility = TextView.VISIBLE
                } else {
                    budgetWarning.visibility = TextView.GONE
                }

                // Clear inputs
                itemInput.text.clear()
                quantityInput.text.clear()
                priceInput.text.clear()
            } else {
                Toast.makeText(this, "Enter item name", Toast.LENGTH_SHORT).show()
            }
        }

        // 🗑 Clear Button Click
        clearButton.setOnClickListener {
            clearItemsNative()
            groceryListView.text = ""
            totalTextView.text = "Total: ₹0.0"
            budgetWarning.visibility = TextView.GONE
        }

        // 📜 History Button Click
        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("history_map", HashMap(historyMap))  // Send as Serializable
            startActivity(intent)

        }

        // 🎙 Voice Input
        fullMicButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak item, quantity and price")

            try {
                startActivityForResult(intent, VOICE_INPUT_REQUEST_CODE)
            } catch (e: Exception) {
                Toast.makeText(this, "Speech not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 🎙 Voice input result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VOICE_INPUT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)?.lowercase() ?: ""


            val words = spokenText.split(" ")
            var quantity = 1
            var price = 0f
            val nameParts = mutableListOf<String>()

            val numberWords = mapOf(
                "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
                "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
                "ten" to 10
            )

            for (word in words) {
                val lowerWord = word.lowercase()
                when {
                    lowerWord.matches(Regex("\\d+")) -> {
                        if (quantity == 1) quantity = lowerWord.toInt()
                        else price = lowerWord.toFloat()
                    }
                    lowerWord.matches(Regex("\\d+\\.\\d+")) -> {
                        price = lowerWord.toFloat()
                    }
                    numberWords.containsKey(lowerWord) -> {
                        if (quantity == 1) quantity = numberWords[lowerWord] ?: 1
                        else price = (numberWords[lowerWord] ?: 0).toFloat()
                    }
                    else -> nameParts.add(lowerWord)
                }
            }

            val itemName = nameParts.joinToString(" ").trim()

            if (itemName.isNotEmpty() && price > 0) {
                addItemNative(itemName, quantity, price)
                val entry = "$quantity x $itemName at ₹$price"
                val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                if (!historyMap.containsKey(today)) {
                    historyMap[today] = mutableListOf()
                }
                historyMap[today]?.add(entry)



                val groceryListView = findViewById<TextView>(R.id.grocery_list)
                val totalTextView = findViewById<TextView>(R.id.total_text)
                val budgetInput = findViewById<EditText>(R.id.budget_input)
                val budgetWarning = findViewById<TextView>(R.id.budget_warning)

                val items = getItemsNative()
                groceryListView.text = items
                val total = getTotalCostNative()
                totalTextView.text = "Total: ₹$total"


                val budget = budgetInput.text.toString().toFloatOrNull()
                if (budget != null && total > budget) {
                    budgetWarning.text = "⚠ Budget exceeded!"
                    budgetWarning.visibility = TextView.VISIBLE
                } else {
                    budgetWarning.visibility = TextView.GONE
                }

                Toast.makeText(this, "Added: $quantity x $itemName for ₹$price", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Could not understand. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

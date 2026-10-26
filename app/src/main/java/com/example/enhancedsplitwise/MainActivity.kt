
package com.example.enhancedsplitwise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            showMessage("You're on Home")
        }

        findViewById<LinearLayout>(R.id.navGroups).setOnClickListener {
            startActivity(Intent(this, GroupsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navActivity).setOnClickListener {
            showMessage("Activity screen coming next!")
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            showMessage("Profile screen coming next!")
        }

        findViewById<TextView>(R.id.txtSeeAll).setOnClickListener {
            showMessage("Your groups will appear here!")
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpensesFromDatabase()
    }

    private fun loadExpensesFromDatabase() {
        val container =
            findViewById<LinearLayout>(R.id.expensesContainer)

        container.removeAllViews()

        val loadingMessage = TextView(this).apply {
            text = "Loading expenses..."
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(4, 12, 4, 12)
        }

        container.addView(loadingMessage)

        lifecycleScope.launch {
            try {
                val response = ApiClient.getExpenses()
                val json = JSONObject(response)
                val expensesArray = json.getJSONArray("expenses")

                container.removeAllViews()

                if (expensesArray.length() == 0) {
                    val emptyMessage = TextView(this@MainActivity).apply {
                        text = "No expenses yet. Add your first one!"
                        textSize = 14f
                        setTextColor(Color.GRAY)
                        setPadding(4, 12, 4, 12)
                    }

                    container.addView(emptyMessage)
                    return@launch
                }

                for (i in 0 until expensesArray.length()) {
                    val item = expensesArray.getJSONObject(i)

                    val titleText =
                        item.optString("title", "Untitled expense")

                    val amountValue =
                        item.optDouble("amount", 0.0)

                    val groupName =
                        item.optString("group_name", "Unknown group")

                    val payerName =
                        item.optString("paid_by_name", "Unknown payer")

                    val splitType =
                        item.optString("split_type", "EQUAL")

                    val displaySplit = when (splitType) {
                        "EQUAL" -> "Equally"
                        "UNEQUAL" -> "Unequally"
                        "PERCENTAGE" -> "By percentage"
                        "EXACT" -> "Exact amounts"
                        else -> splitType
                    }

                    val card = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(18, 14, 18, 14)
                        setBackgroundResource(R.drawable.group_card)

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        params.bottomMargin = 12
                        layoutParams = params
                    }

                    val title = TextView(this@MainActivity).apply {
                        text = titleText
                        textSize = 16f
                        setTextColor(Color.rgb(24, 60, 50))
                        setTypeface(null, Typeface.BOLD)
                    }

                    val details = TextView(this@MainActivity).apply {
                        text = "$groupName · Paid by $payerName"
                        textSize = 12f
                        setTextColor(Color.GRAY)
                        setPadding(0, 5, 0, 5)
                    }

                    val amount = TextView(this@MainActivity).apply {
                        text = "₹${"%.2f".format(amountValue)} · $displaySplit"
                        textSize = 14f
                        setTextColor(Color.rgb(52, 132, 90))
                    }

                    card.addView(title)
                    card.addView(details)
                    card.addView(amount)

                    container.addView(card)
                }

            } catch (e: Exception) {
                container.removeAllViews()

                val errorMessage = TextView(this@MainActivity).apply {
                    text = "Couldn't load expenses. Check your backend connection."
                    textSize = 14f
                    setTextColor(Color.GRAY)
                    setPadding(4, 12, 4, 12)
                }

                container.addView(errorMessage)

                Log.e(
                    "MainActivity",
                    "Failed to load expenses",
                    e
                )
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}

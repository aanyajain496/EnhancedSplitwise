package com.example.enhancedsplitwise

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddExpenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val titleInput = findViewById<EditText>(R.id.etExpenseTitle)
        val amountInput = findViewById<EditText>(R.id.etAmount)

        val groupSpinner = findViewById<Spinner>(R.id.spinnerGroup)
        val paidBySpinner = findViewById<Spinner>(R.id.spinnerPaidBy)
        val splitSpinner = findViewById<Spinner>(R.id.spinnerSplitMethod)

        // Temporary options matching the records in your database
        setupSpinner(
            groupSpinner,
            listOf("Select a group", "Flat 4B")
        )

        setupSpinner(
            paidBySpinner,
            listOf("Select who paid", "Test User")
        )

        setupSpinner(
            splitSpinner,
            listOf("Equally", "Unequally", "By percentage", "Exact amounts")
        )

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnScanReceipt).setOnClickListener {
            Toast.makeText(
                this,
                "Opening receipt picker...",
                Toast.LENGTH_SHORT
            ).show()

            receiptPicker.launch("image/*")
        }

        val saveButton = findViewById<Button>(R.id.btnSaveExpense)

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val amount = amountInput.text.toString().trim().toDoubleOrNull()

            when {
                title.isBlank() -> {
                    titleInput.error = "Enter an expense name"
                    titleInput.requestFocus()
                }

                amount == null || !amount.isFinite() || amount <= 0.0 -> {
                    amountInput.error = "Enter a valid amount"
                    amountInput.requestFocus()
                }

                groupSpinner.selectedItemPosition == 0 -> {
                    showMessage("Please select a group")
                }

                paidBySpinner.selectedItemPosition == 0 -> {
                    showMessage("Please select who paid")
                }

                else -> {
                    val group = groupSpinner.selectedItem.toString()
                    val paidBy = paidBySpinner.selectedItem.toString()
                    val splitMethod = splitSpinner.selectedItem.toString()

                    val splitType = when (splitMethod) {
                        "Equally" -> "EQUAL"
                        "Unequally" -> "UNEQUAL"
                        "By percentage" -> "PERCENTAGE"
                        "Exact amounts" -> "EXACT"
                        else -> "EQUAL"
                    }

                    saveButton.isEnabled = false
                    saveButton.text = "Saving..."

                    lifecycleScope.launch {
                        try {
                            ApiClient.createExpense(
                                title = title,
                                amount = amount,
                                groupId = 1,
                                paidBy = 1,
                                splitType = splitType
                            )

                            // Update the dashboard after the API succeeds.
                            ExpenseStore.expenses.add(
                                Expense(
                                    title = title,
                                    amount = amount,
                                    group = group,
                                    paidBy = paidBy,
                                    splitMethod = splitMethod
                                )
                            )

                            Toast.makeText(
                                this@AddExpenseActivity,
                                "Expense saved to database!",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()

                        } catch (e: Exception) {
                            showMessage(
                                "Could not save expense: ${
                                    e.message ?: "Check your connection"
                                }"
                            )
                        } finally {
                            saveButton.isEnabled = true
                            saveButton.text = "Save Expense"
                        }
                    }
                }
            }
        }
    }

    private val receiptPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                recognizeReceipt(uri)
            }
        }

    private fun recognizeReceipt(uri: Uri) {
        val titleInput = findViewById<EditText>(R.id.etExpenseTitle)
        val amountInput = findViewById<EditText>(R.id.etAmount)

        try {
            val image = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text

                    if (recognizedText.isBlank()) {
                        showMessage("No text found. Try a clearer receipt image.")
                        return@addOnSuccessListener
                    }

                    val lines = recognizedText.lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    // Suggest a title from the first readable line.
                    titleInput.setText(lines.firstOrNull() ?: "Receipt")

                    // Find a likely total, preferring lines containing TOTAL.
                    val totalLine = lines.lastOrNull {
                        it.contains("grand total", ignoreCase = true) ||
                                it.contains("total", ignoreCase = true)
                    } ?: lines.lastOrNull {
                        Regex("""\d+[.,]\d{2}""").containsMatchIn(it)
                    }

                    val amountMatch = totalLine?.let {
                        Regex("""\d+(?:,\d{3})*(?:\.\d{2})?""")
                            .findAll(it)
                            .lastOrNull()
                    }

                    val amountText = amountMatch?.value?.replace(",", "")

                    if (amountText != null &&
                        (amountText.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        amountInput.setText(amountText)
                        showMessage("Receipt scanned! Review the title and amount.")
                    } else {
                        showMessage("Text scanned. Please enter the total amount.")
                    }
                }
                .addOnFailureListener { error ->
                    showMessage("Receipt scanning failed: ${error.localizedMessage}")
                }
                .addOnCompleteListener {
                    recognizer.close()
                }
        } catch (error: Exception) {
            showMessage("Could not open receipt: ${error.localizedMessage}")
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            items
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter = adapter
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

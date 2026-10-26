package com.example.enhancedsplitwise

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class GroupsActivity : AppCompatActivity() {

    private lateinit var groupsContainer: LinearLayout
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        groupsContainer = findViewById(R.id.groupsContainer)
        statusText = findViewById(R.id.txtGroupsStatus)

        findViewById<Button>(R.id.btnCreateGroup).setOnClickListener {
            showCreateGroupDialog()
        }

        findViewById<Button>(R.id.btnAddMember).setOnClickListener {
            showAddMemberDialog()
        }


        loadGroups()
    }

    private fun loadGroups() {
        if (!ApiClient.isLoggedIn()) {
            statusText.text = "Please log in to load your groups."
            showLoginDialog {
                loadGroups()
            }
            return
        }

        statusText.text = "Loading groups..."
        groupsContainer.removeAllViews()

        lifecycleScope.launch {
            try {
                val response = ApiClient.getGroups()
                val json = JSONObject(response)
                val groups = json.getJSONArray("groups")

                groupsContainer.removeAllViews()

                if (groups.length() == 0) {
                    statusText.text = "No groups yet. Create your first group!"
                    return@launch
                }

                statusText.text = "${groups.length()} group(s)"

                for (i in 0 until groups.length()) {
                    val group = groups.getJSONObject(i)
                    val name = group.optString("name", "Unnamed group")
                    val role = group.optString("my_role", "MEMBER")
                    val members = group.optInt("member_count", 0)

                    val card = LinearLayout(this@GroupsActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(24, 20, 24, 20)
                        setBackgroundResource(R.drawable.group_card)

                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = 16
                        }
                    }

                    val title = TextView(this@GroupsActivity).apply {
                        text = name
                        textSize = 18f
                        setTextColor(Color.rgb(24, 60, 50))
                        setTypeface(null, Typeface.BOLD)
                    }

                    val details = TextView(this@GroupsActivity).apply {
                        text = "$members member(s) · Role: $role"
                        textSize = 14f
                        setTextColor(Color.GRAY)
                        setPadding(0, 8, 0, 0)
                    }

                    card.addView(title)
                    card.addView(details)
                    groupsContainer.addView(card)
                }
            } catch (e: Exception) {
                statusText.text =
                    "Couldn't load groups. Please log in and check your connection."
                Toast.makeText(
                    this@GroupsActivity,
                    e.message ?: "Unable to load groups",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showCreateGroupDialog() {
        val input = EditText(this).apply {
            hint = "Enter group name"
            setSingleLine(true)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Create a group")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isBlank()) {
                    Toast.makeText(
                        this,
                        "Please enter a group name",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    createGroup(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoginDialog(onLoginSuccess: () -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 16, 40, 0)
        }

        val emailInput = EditText(this).apply {
            hint = "Email"
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        val passwordInput = EditText(this).apply {
            hint = "Password"
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(emailInput)
        layout.addView(passwordInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Log in to Splitwise")
            .setView(layout)
            .setPositiveButton("Log in", null)
            .setNegativeButton("Cancel", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(
                        androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener {
                        val email = emailInput.text.toString().trim()
                        val password = passwordInput.text.toString()

                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(
                                this,
                                "Enter your email and password",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }

                        lifecycleScope.launch {
                            try {
                                ApiClient.login(email, password)
                                dialog.dismiss()
                                Toast.makeText(
                                    this@GroupsActivity,
                                    "Login successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoginSuccess()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@GroupsActivity,
                                    e.message ?: "Login failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                dialog.show()
            }
    }
    private fun createGroup(name: String) {
        if (!ApiClient.isLoggedIn()) {
            showLoginDialog {
                createGroup(name)
            }
            return
        }

        lifecycleScope.launch {
            try {
                statusText.text = "Creating group..."

                ApiClient.createGroup(name)

                Toast.makeText(
                    this@GroupsActivity,
                    "Group created!",
                    Toast.LENGTH_SHORT
                ).show()

                loadGroups()
            } catch (e: Exception) {
                statusText.text = "Couldn't create group."
                Toast.makeText(
                    this@GroupsActivity,
                    e.message ?: "Unable to create group",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showAddMemberDialog() {
        if (!ApiClient.isLoggedIn()) {
            showLoginDialog { showAddMemberDialog() }
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 8, 40, 0)
        }

        val groupIdInput = EditText(this).apply {
            hint = "Group ID (e.g. 2)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val emailInput = EditText(this).apply {
            hint = "Member's registered email"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        layout.addView(groupIdInput)
        layout.addView(emailInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add a group member")
            .setView(layout)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(
                        androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener {
                        val groupId = groupIdInput.text.toString().toIntOrNull()
                        val email = emailInput.text.toString().trim()

                        if (groupId == null || groupId <= 0 || email.isBlank()) {
                            Toast.makeText(
                                this,
                                "Enter a valid group ID and email",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }

                        lifecycleScope.launch {
                            try {
                                val response = ApiClient.addGroupMember(groupId, email)
                                Toast.makeText(
                                    this@GroupsActivity,
                                    JSONObject(response).optString(
                                        "message",
                                        "Member added successfully"
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                                dialog.dismiss()
                                loadGroups()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@GroupsActivity,
                                    e.message ?: "Unable to add member",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                dialog.show()
            }
    }
}

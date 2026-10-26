package com.example.enhancedsplitwise

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {

    // Address of the backend from the Android emulator
    private const val BASE_URL = "http://10.0.2.2:3001"

    @Volatile
    private var accessToken: String? = null
    fun isLoggedIn(): Boolean {
        return !accessToken.isNullOrBlank()
    }

    fun setAccessToken(token: String) {
        accessToken = token
    }

    private fun addAuthHeader(connection: HttpURLConnection) {
        accessToken?.let {
            connection.setRequestProperty("Authorization", "Bearer $it")
        }
    }


    suspend fun login(email: String, password: String): String =
        withContext(Dispatchers.IO) {
            val connection =
                URL("$BASE_URL/api/v1/auth/login")
                    .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val body = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream.bufferedReader().use { it.readText() }

                if (responseCode !in 200..299) {
                    throw Exception("Login failed: $response")
                }

                val json = JSONObject(response)
                val token = json.getString("accessToken")
                setAccessToken(token)
                token
            } finally {
                connection.disconnect()
            }
        }

    suspend fun checkHealth(): String = withContext(Dispatchers.IO) {
        val connection = URL("$BASE_URL/api/v1/health")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream.bufferedReader().use { it.readText() }

            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode: $response")
            }

            response
        } finally {
            connection.disconnect()
        }
    }

    suspend fun createExpense(
        title: String,
        amount: Double,
        groupId: Int,
        paidBy: Int,
        splitType: String
    ): String = withContext(Dispatchers.IO) {

        val connection = URL("$BASE_URL/api/v1/expenses")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            val body = JSONObject().apply {
                put("title", title)
                put("amount", amount)
                put("group_id", groupId)
                put("paid_by", paidBy)
                put("split_type", splitType)
            }

            connection.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream.bufferedReader().use { it.readText() }

            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode: $response")
            }

            response
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getExpenses(): String = withContext(Dispatchers.IO) {
        val connection = URL("$BASE_URL/api/v1/expenses")
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream.bufferedReader().use { it.readText() }

            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode: $response")
            }

            response
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getGroups(): String = withContext(Dispatchers.IO) {
        val connection =
            URL("$BASE_URL/api/v1/groups")
                .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            addAuthHeader(connection)

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream.bufferedReader().use { it.readText() }

            if (responseCode !in 200..299) {
                throw Exception("Fetching groups failed: $response")
            }

            response
        } finally {
            connection.disconnect()
        }
    }

    suspend fun createGroup(name: String, type: String = "OTHER"): String =
        withContext(Dispatchers.IO) {
            val connection =
                URL("$BASE_URL/api/v1/groups")
                    .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )
                addAuthHeader(connection)

                val body = JSONObject().apply {
                    put("name", name)
                    put("type", type)
                }

                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream.bufferedReader().use { it.readText() }

                if (responseCode !in 200..299) {
                    throw Exception("Creating group failed: $response")
                }

                response
            } finally {
                connection.disconnect()
            }
        }

    suspend fun addGroupMember(groupId: Int, email: String): String =
        withContext(Dispatchers.IO) {
            val connection = URL("$BASE_URL/api/v1/groups/$groupId/members")
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                addAuthHeader(connection)

                val body = JSONObject().apply {
                    put("email", email.trim())
                    put("role", "MEMBER")
                }

                connection.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val response = stream.bufferedReader().use { it.readText() }

                if (responseCode !in 200..299) {
                    throw Exception("Failed to add member: $response")
                }

                response
            } finally {
                connection.disconnect()
            }
        }

    suspend fun getGroupDetails(groupId: Int): String =
        withContext(Dispatchers.IO) {
            val connection = URL("$BASE_URL/api/v1/groups/$groupId")
                .openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                addAuthHeader(connection)

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val response = stream.bufferedReader().use { it.readText() }

                if (responseCode !in 200..299) {
                    throw Exception("Failed to load group details: $response")
                }

                response
            } finally {
                connection.disconnect()
            }
        }
}

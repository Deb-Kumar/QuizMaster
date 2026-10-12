package com.example.onlinequizapp.data.session

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Result data class for an AI API key connection test.
 */
data class AiConnectionResult(
    val isSuccess: Boolean,
    val statusCode: Int? = null,
    val modelName: String? = null,
    val aiResponse: String,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
)

/**
 * Lightweight connection test service for verifying OpenAI and Google Gemini
 * API keys and retrieving live response verification from the models.
 */
object AiConnectionTester {

    val DEFAULT_OPENAI_MODELS = listOf(
        "gpt-4o-mini",
        "gpt-3.5-turbo",
        "gpt-4o"
    )

    suspend fun testOpenAi(apiKey: String): AiConnectionResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiConnectionResult(
                isSuccess = false,
                aiResponse = "OpenAI API key cannot be blank. Please enter your API key."
            )
        }

        val candidateModels = linkedSetOf<String>().apply {
            val saved = AiConfigManager.getOpenAiModel()
            if (saved.isNotBlank()) add(saved)
            addAll(DEFAULT_OPENAI_MODELS)
        }.toList()

        var lastStatusCode = 0
        var lastErrorMsg = ""

        for (model in candidateModels) {
            try {
                val url = URL("https://api.openai.com/v1/chat/completions")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 12000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }

                val requestJson = JSONObject().apply {
                    put("model", model)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Respond in 1 sentence: confirm OpenAI API connection is working for QuizMaster.")
                        })
                    }
                    put("messages", messages)
                    put("max_tokens", 200)
                    put("temperature", 0.5)
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }

                val statusCode = conn.responseCode
                lastStatusCode = statusCode
                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (isSuccess) {
                    val json = JSONObject(responseBody)
                    val returnedModel = json.optString("model", model)
                    val choices = json.optJSONArray("choices")
                    val replyText = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")?.trim()
                        ?: "OpenAI connection verified successfully!"

                    AiConfigManager.setOpenAiModel(model)

                    return@withContext AiConnectionResult(
                        isSuccess = true,
                        statusCode = statusCode,
                        modelName = returnedModel,
                        aiResponse = replyText
                    )
                } else if (statusCode == 404 || statusCode == 429 || statusCode == 503 || ((statusCode == 400 || statusCode == 404) && (responseBody.contains("model") || responseBody.contains("quota")))) {
                    // Model does not exist, rate-limited, or user tier does not have access -> try next candidate model
                    lastErrorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (_: Exception) {
                        responseBody.take(250)
                    }
                    continue
                } else {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        errJson.optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody.take(250)
                    }
                    return@withContext AiConnectionResult(
                        isSuccess = false,
                        statusCode = statusCode,
                        modelName = model,
                        aiResponse = "Error ($statusCode): $errorMsg"
                    )
                }
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: e.message ?: "Network error"
            }
        }

        AiConnectionResult(
            isSuccess = false,
            statusCode = if (lastStatusCode != 0) lastStatusCode else null,
            aiResponse = if (lastErrorMsg.isNotBlank()) "Error ($lastStatusCode): $lastErrorMsg" else "Connection failed: Could not connect to OpenAI."
        )
    }

    val DEFAULT_OPENROUTER_MODELS = listOf(
        "nvidia/nemotron-3-ultra-550b-a55b:free"
    )

    suspend fun testOpenRouter(apiKey: String): AiConnectionResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiConnectionResult(
                isSuccess = false,
                aiResponse = "OpenRouter API key cannot be blank. Please enter your API key."
            )
        }

        val candidateModels = linkedSetOf<String>().apply {
            val saved = AiConfigManager.getOpenRouterModel()
            if (saved.isNotBlank()) add(saved)
            addAll(DEFAULT_OPENROUTER_MODELS)
        }.toList()

        var lastStatusCode = 0
        var lastErrorMsg = ""

        for (model in candidateModels) {
            try {
                val url = URL("https://openrouter.ai/api/v1/chat/completions")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 20000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                    setRequestProperty("HTTP-Referer", "https://quizmaster.example.com")
                    setRequestProperty("X-Title", "QuizMaster")
                }

                val requestJson = JSONObject().apply {
                    put("model", model)
                    put("stream", false)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Respond in 1 sentence: confirm OpenRouter API connection is working for QuizMaster.")
                        })
                    }
                    put("messages", messages)
                    put("max_tokens", 200)
                    put("temperature", 0.5)
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }

                val statusCode = conn.responseCode
                lastStatusCode = statusCode
                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (isSuccess) {
                    val json = JSONObject(responseBody)
                    val returnedModel = json.optString("model", model)
                    val choices = json.optJSONArray("choices")
                    val replyText = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")?.trim()
                        ?: "OpenRouter connection verified successfully!"

                    AiConfigManager.setOpenRouterModel(model)

                    return@withContext AiConnectionResult(
                        isSuccess = true,
                        statusCode = statusCode,
                        modelName = returnedModel,
                        aiResponse = replyText
                    )
                } else if (statusCode == 404 || statusCode == 429 || statusCode == 503 || (statusCode == 400 && responseBody.contains("model"))) {
                    // Model unavailable, rate-limited on free tier, or not found -> fallback to next candidate
                    lastErrorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (_: Exception) {
                        responseBody.take(250)
                    }
                    continue
                } else {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        errJson.optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody.take(250)
                    }
                    return@withContext AiConnectionResult(
                        isSuccess = false,
                        statusCode = statusCode,
                        modelName = model,
                        aiResponse = "Error ($statusCode): $errorMsg"
                    )
                }
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: e.message ?: "Network error"
            }
        }

        AiConnectionResult(
            isSuccess = false,
            statusCode = if (lastStatusCode != 0) lastStatusCode else null,
            aiResponse = if (lastErrorMsg.isNotBlank()) "Error ($lastStatusCode): $lastErrorMsg" else "Connection failed: Could not connect to OpenRouter."
        )
    }

    val DEFAULT_CLAUDE_MODELS = listOf(
        "claude-3-5-haiku-latest",
        "claude-3-5-haiku-20241022",
        "claude-3-5-sonnet-latest",
        "claude-3-5-sonnet-20241022",
        "claude-3-haiku-20240307"
    )

    suspend fun testClaude(apiKey: String): AiConnectionResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiConnectionResult(
                isSuccess = false,
                aiResponse = "Claude (Anthropic) API key cannot be blank. Please enter your API key."
            )
        }

        val candidateModels = linkedSetOf<String>().apply {
            val saved = AiConfigManager.getClaudeModel()
            if (saved.isNotBlank()) add(saved)
            addAll(DEFAULT_CLAUDE_MODELS)
        }.toList()

        var lastStatusCode = 0
        var lastErrorMsg = ""

        for (model in candidateModels) {
            try {
                val url = URL("https://api.anthropic.com/v1/messages")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 20000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-api-key", apiKey.trim())
                    setRequestProperty("anthropic-version", "2023-06-01")
                }

                val requestJson = JSONObject().apply {
                    put("model", model)
                    put("max_tokens", 200)
                    put("temperature", 0.5)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Respond in 1 sentence: confirm Claude API connection is working for QuizMaster.")
                        })
                    }
                    put("messages", messages)
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }

                val statusCode = conn.responseCode
                lastStatusCode = statusCode
                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (isSuccess) {
                    val json = JSONObject(responseBody)
                    val returnedModel = json.optString("model", model)
                    val contentArr = json.optJSONArray("content")
                    val sb = StringBuilder()
                    if (contentArr != null) {
                        for (i in 0 until contentArr.length()) {
                            val block = contentArr.optJSONObject(i)
                            val t = block?.optString("text", "") ?: ""
                            if (t.isNotBlank()) sb.append(t)
                        }
                    }
                    val replyText = if (sb.isNotBlank()) sb.toString().trim() else "Claude connection verified successfully!"

                    AiConfigManager.setClaudeModel(model)

                    return@withContext AiConnectionResult(
                        isSuccess = true,
                        statusCode = statusCode,
                        modelName = returnedModel,
                        aiResponse = replyText
                    )
                } else if (statusCode == 404 || statusCode == 429 || statusCode == 529 || ((statusCode == 400 || statusCode == 404) && (responseBody.contains("model") || responseBody.contains("not_found")))) {
                    lastErrorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (_: Exception) {
                        responseBody.take(250)
                    }
                    continue
                } else {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        errJson.optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody.take(250)
                    }
                    return@withContext AiConnectionResult(
                        isSuccess = false,
                        statusCode = statusCode,
                        modelName = model,
                        aiResponse = "Error ($statusCode): $errorMsg"
                    )
                }
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: e.message ?: "Network error"
            }
        }

        AiConnectionResult(
            isSuccess = false,
            statusCode = if (lastStatusCode != 0) lastStatusCode else null,
            aiResponse = if (lastErrorMsg.isNotBlank()) "Error ($lastStatusCode): $lastErrorMsg" else "Connection failed: Could not connect to Claude."
        )
    }

    val DEFAULT_DEEPSEEK_MODELS = listOf(
        "deepseek-chat",
        "deepseek-reasoner",
        "deepseek-v3"
    )

    suspend fun testDeepSeek(apiKey: String): AiConnectionResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiConnectionResult(
                isSuccess = false,
                aiResponse = "DeepSeek API key cannot be blank. Please enter your API key."
            )
        }

        val candidateModels = linkedSetOf<String>().apply {
            val saved = AiConfigManager.getDeepSeekModel()
            if (saved.isNotBlank()) add(saved)
            addAll(DEFAULT_DEEPSEEK_MODELS)
        }.toList()

        var lastStatusCode = 0
        var lastErrorMsg = ""

        for (model in candidateModels) {
            try {
                val url = URL("https://api.deepseek.com/chat/completions")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 20000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }

                val requestJson = JSONObject().apply {
                    put("model", model)
                    put("stream", false)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Respond in 1 sentence: confirm DeepSeek API connection is working for QuizMaster.")
                        })
                    }
                    put("messages", messages)
                    put("max_tokens", 200)
                    if (model != "deepseek-reasoner") {
                        put("temperature", 0.5)
                    }
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }

                val statusCode = conn.responseCode
                lastStatusCode = statusCode
                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (isSuccess) {
                    val json = JSONObject(responseBody)
                    val returnedModel = json.optString("model", model)
                    val choices = json.optJSONArray("choices")
                    val msg = choices?.optJSONObject(0)?.optJSONObject("message")
                    val replyText = msg?.optString("content")?.trim()?.ifBlank {
                        msg.optString("reasoning_content")?.trim()
                    } ?: "DeepSeek connection verified successfully!"

                    AiConfigManager.setDeepSeekModel(model)

                    return@withContext AiConnectionResult(
                        isSuccess = true,
                        statusCode = statusCode,
                        modelName = returnedModel,
                        aiResponse = replyText
                    )
                } else if (statusCode == 404 || statusCode == 429 || statusCode == 503 || ((statusCode == 400 || statusCode == 404) && responseBody.contains("model"))) {
                    lastErrorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (_: Exception) {
                        responseBody.take(250)
                    }
                    continue
                } else {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        errJson.optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody.take(250)
                    }
                    return@withContext AiConnectionResult(
                        isSuccess = false,
                        statusCode = statusCode,
                        modelName = model,
                        aiResponse = "Error ($statusCode): $errorMsg"
                    )
                }
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: e.message ?: "Network error"
            }
        }

        AiConnectionResult(
            isSuccess = false,
            statusCode = if (lastStatusCode != 0) lastStatusCode else null,
            aiResponse = if (lastErrorMsg.isNotBlank()) "Error ($lastStatusCode): $lastErrorMsg" else "Connection failed: Could not connect to DeepSeek."
        )
    }

    val DEFAULT_AGENTROUTER_MODELS = listOf(
        "deepseek-v4-flash",
        "gpt-6-astra",
        "claude-opus-4-8",
        "deepseek-r1",
        "claude-opus-5",
        "glm-5.3",
        "gpt-5.6-sol"
    )

    suspend fun testAgentRouter(apiKey: String): AiConnectionResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiConnectionResult(
                isSuccess = false,
                aiResponse = "AgentRouter API key cannot be blank. Please enter your API key."
            )
        }

        val candidateModels = linkedSetOf<String>().apply {
            val saved = AiConfigManager.getAgentRouterModel()
            if (saved.isNotBlank()) add(saved)
            addAll(DEFAULT_AGENTROUTER_MODELS)
        }.toList()

        val endpoints = listOf(
            "https://co.agentrouter.org/v1/chat/completions",
            "https://agentrouter.org/v1/chat/completions"
        )

        var lastStatusCode = 0
        var lastErrorMsg = ""

        for (model in candidateModels) {
            for (endpoint in endpoints) {
                try {
                    val url = URL(endpoint)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 20000
                        readTimeout = 30000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                    }

                    val requestJson = JSONObject().apply {
                        put("model", model)
                        put("stream", false)
                        val messages = JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "Respond in 1 sentence: confirm AgentRouter API connection is working for QuizMaster.")
                            })
                        }
                        put("messages", messages)
                        put("max_tokens", 200)
                        put("temperature", 0.5)
                    }

                    OutputStreamWriter(conn.outputStream).use { writer ->
                        writer.write(requestJson.toString())
                        writer.flush()
                    }

                    val statusCode = conn.responseCode
                    lastStatusCode = statusCode
                    val isSuccess = statusCode in 200..299
                    val stream = if (isSuccess) conn.inputStream else conn.errorStream
                    val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                    if (isSuccess) {
                        val json = JSONObject(responseBody)
                        val returnedModel = json.optString("model", model)
                        val choices = json.optJSONArray("choices")
                        val msg = choices?.optJSONObject(0)?.optJSONObject("message")
                        val replyText = msg?.optString("content")?.trim()?.ifBlank {
                            msg.optString("reasoning_content")?.trim()
                        } ?: "AgentRouter connection verified successfully!"

                        AiConfigManager.setAgentRouterModel(model)

                        return@withContext AiConnectionResult(
                            isSuccess = true,
                            statusCode = statusCode,
                            modelName = returnedModel,
                            aiResponse = replyText
                        )
                    } else if (statusCode == 404 || statusCode == 429 || statusCode == 503 || ((statusCode == 400 || statusCode == 404) && responseBody.contains("model"))) {
                        lastErrorMsg = try {
                            JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                        } catch (_: Exception) {
                            responseBody.take(250)
                        }
                        continue
                    } else {
                        val errorMsg = try {
                            val errJson = JSONObject(responseBody)
                            errJson.optJSONObject("error")?.optString("message") ?: responseBody
                        } catch (e: Exception) {
                            responseBody.take(250)
                        }
                        return@withContext AiConnectionResult(
                            isSuccess = false,
                            statusCode = statusCode,
                            modelName = model,
                            aiResponse = "Error ($statusCode): $errorMsg"
                        )
                    }
                } catch (e: Exception) {
                    lastErrorMsg = e.localizedMessage ?: e.message ?: "Network error"
                }
            }
        }

        AiConnectionResult(
            isSuccess = false,
            statusCode = if (lastStatusCode != 0) lastStatusCode else null,
            aiResponse = if (lastErrorMsg.isNotBlank()) "Error ($lastStatusCode): $lastErrorMsg" else "Connection failed: Could not connect to AgentRouter."
        )
    }

    val DEFAULT_GEMINI_MODELS = listOf(
        "gemini-2.0-flash",
        "gemini-1.5-flash",
        "gemini-2.5-flash",
        "gemini-2.5-flash-lite",
        "gemini-2.0-flash-lite",
        "gemini-1.5-flash-8b",
        "gemini-1.5-pro",
        "gemini-2.0-flash-001",
        "gemini-1.5-flash-latest"
    )

    suspend fun discoverGeminiModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext emptyList()
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey.trim()}")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 10000
            }
            if (conn.responseCode in 200..299) {
                val body = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(body)
                val arr = json.optJSONArray("models") ?: return@withContext emptyList()
                val discovered = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val m = arr.optJSONObject(i) ?: continue
                    val name = m.optString("name", "").removePrefix("models/")
                    val methods = m.optJSONArray("supportedGenerationMethods")
                    var supportsGenerate = false
                    if (methods != null) {
                        for (j in 0 until methods.length()) {
                            if (methods.optString(j) == "generateContent") {
                                supportsGenerate = true
                                break
                            }
                        }
                    }

                    val lower = name.lowercase()
                    val isNonText = lower.contains("tts") ||
                            lower.contains("audio") ||
                            lower.contains("image") ||
                            lower.contains("live") ||
                            lower.contains("embed") ||
                            lower.contains("transcribe") ||
                            lower.contains("computer-use") ||
                            lower.contains("realtime") ||
                            lower.contains("imagen")

                    if (supportsGenerate && name.isNotBlank() && !isNonText) {
                        discovered.add(name)
                    }
                }
                discovered.sortedWith(
                    compareByDescending<String> { it == "gemini-2.0-flash" }
                        .thenByDescending { it == "gemini-2.5-flash" }
                        .thenByDescending { it.contains("2.0-flash") }
                        .thenByDescending { it.contains("2.5-flash") }
                        .thenByDescending { it.contains("flash") }
                        .thenByDescending { it.contains("gemini") }
                )
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun testGemini(apiKey: String): AiConnectionResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext AiConnectionResult(
                isSuccess = false,
                aiResponse = "Google Gemini API key cannot be blank. Please enter your API key."
            )
        }

        val discovered = discoverGeminiModels(apiKey)
        val modelsToTry = linkedSetOf<String>().apply {
            val saved = AiConfigManager.getGeminiModel()
            if (saved.isNotBlank() && !saved.contains("tts") && !saved.contains("image") && !saved.contains("audio")) {
                add(saved)
            }
            addAll(discovered)
            addAll(DEFAULT_GEMINI_MODELS)
        }.toList()

        var lastStatusCode = 0
        var lastErrorMsg = ""

        for (model in modelsToTry) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey.trim()}")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 12000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                val requestJson = JSONObject().apply {
                    val contents = JSONArray().apply {
                        put(JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "Respond in 1 sentence: confirm Google Gemini API connection is working for QuizMaster.")
                                })
                            }
                            put("parts", parts)
                        })
                    }
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("maxOutputTokens", 300)
                        put("temperature", 0.5)
                    })
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(requestJson.toString())
                    writer.flush()
                }

                val statusCode = conn.responseCode
                lastStatusCode = statusCode
                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (isSuccess) {
                    val json = JSONObject(responseBody)
                    val candidates = json.optJSONArray("candidates")
                    val candidateObj = candidates?.optJSONObject(0)
                    val contentObj = candidateObj?.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val sb = StringBuilder()
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.optJSONObject(i)
                            val t = part?.optString("text", "") ?: ""
                            if (t.isNotBlank()) sb.append(t)
                        }
                    }
                    val fullText = sb.toString().trim()
                    val reply = if (fullText.length > 5) {
                        fullText
                    } else {
                        "Google Gemini API connection verified successfully!"
                    }

                    AiConfigManager.setGeminiModel(model)

                    return@withContext AiConnectionResult(
                        isSuccess = true,
                        statusCode = statusCode,
                        modelName = model,
                        aiResponse = reply
                    )
                } else if (statusCode == 404 || statusCode == 429 || statusCode == 503 || statusCode == 500 || statusCode == 502 || statusCode == 504 || statusCode == 529 ||
                    ((statusCode == 400 || statusCode == 404) && (responseBody.contains("response modalities") || responseBody.contains("not supported") || responseBody.contains("model"))) ||
                    responseBody.contains("high demand", ignoreCase = true) || responseBody.contains("RESOURCE_EXHAUSTED", ignoreCase = true) || responseBody.contains("overloaded", ignoreCase = true)) {
                    lastErrorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (_: Exception) {
                        responseBody.take(250)
                    }
                    continue
                } else {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        errJson.optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody.take(250)
                    }
                    return@withContext AiConnectionResult(
                        isSuccess = false,
                        statusCode = statusCode,
                        modelName = model,
                        aiResponse = "Error ($statusCode): $errorMsg"
                    )
                }
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: e.message ?: "Network error"
            }
        }

        AiConnectionResult(
            isSuccess = false,
            statusCode = if (lastStatusCode != 0) lastStatusCode else null,
            aiResponse = if (lastErrorMsg.isNotBlank()) "Error ($lastStatusCode): $lastErrorMsg" else "Connection failed: Could not connect to Gemini."
        )
    }
}

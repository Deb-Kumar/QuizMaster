package com.example.onlinequizapp.data.ai

import com.example.onlinequizapp.data.session.AiConfigManager
import com.example.onlinequizapp.data.session.AiConnectionTester
import com.example.onlinequizapp.domain.ai.AIQuizGenerator
import com.example.onlinequizapp.domain.ai.AiProvider
import com.example.onlinequizapp.domain.ai.AiQuizRequest
import com.example.onlinequizapp.domain.model.QuestionDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiQuizProvider : AIQuizGenerator {

    override val provider: AiProvider = AiProvider.GEMINI

    override suspend fun generateQuestions(
        request: AiQuizRequest,
        apiKey: String
    ): Result<List<QuestionDraft>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Google Gemini API key is missing. Please add your API key in settings."))
        }

        val savedModel = AiConfigManager.getGeminiModel()
        val candidateModels = linkedSetOf<String>().apply {
            if (savedModel.isNotBlank() && !savedModel.contains("tts") && !savedModel.contains("image") && !savedModel.contains("audio")) {
                add(savedModel)
            }
            addAll(AiConnectionTester.DEFAULT_GEMINI_MODELS)
        }.toList()

        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey.trim()}"
                val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 25000
                    readTimeout = 30000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }

                val prompt = AIQuizPromptHelper.buildPrompt(request)

                val payload = JSONObject().apply {
                    val contents = JSONArray().apply {
                        put(JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            }
                            put("parts", parts)
                        })
                    }
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("responseMimeType", "application/json")
                    })
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val statusCode = conn.responseCode
                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (!isSuccess) {
                    val errorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (_: Exception) {
                        responseBody.take(200)
                    }

                    // Fatal authentication errors where all other models would fail with the same key
                    if (statusCode == 401 || (statusCode == 403 && (responseBody.contains("API_KEY_INVALID") || responseBody.contains("API key not valid")))) {
                        return@withContext Result.failure(Exception("Gemini error ($statusCode): Invalid API key. Please check your Gemini API key in settings."))
                    }

                    // Model is not found (404), high demand / overloaded (503), rate-limited (429), server error (500/502/504), or model config issue (400)
                    val isTransientOrModelSpecific = statusCode == 404 ||
                            statusCode == 429 ||
                            statusCode == 503 ||
                            statusCode == 500 ||
                            statusCode == 502 ||
                            statusCode == 504 ||
                            statusCode == 529 ||
                            (statusCode == 400 && (responseBody.contains("response modalities") || responseBody.contains("not supported") || responseBody.contains("model"))) ||
                            responseBody.contains("high demand", ignoreCase = true) ||
                            responseBody.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                            responseBody.contains("overloaded", ignoreCase = true) ||
                            responseBody.contains("temporarily", ignoreCase = true)

                    if (isTransientOrModelSpecific) {
                        lastError = Exception("Gemini ($model): $errorMsg")
                        delay(400)
                        continue
                    }

                    return@withContext Result.failure(Exception("Gemini error ($statusCode): $errorMsg"))
                }

                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (text.isNullOrBlank()) {
                    lastError = Exception("No content returned from Gemini ($model).")
                    continue
                }

                val drafts = AIQuizValidator.parseAndValidate(text, request.questionType)
                if (drafts.isEmpty()) {
                    lastError = Exception("Gemini ($model) returned unparseable questions format.")
                    continue
                }

                AiConfigManager.setGeminiModel(model)
                return@withContext Result.success(drafts)
            } catch (e: Exception) {
                lastError = e
                delay(400)
            }
        }

        Result.failure(lastError ?: Exception("Could not generate questions: All Google Gemini models are currently experiencing high demand. Please try again in a few moments or switch to another provider."))
    }
}

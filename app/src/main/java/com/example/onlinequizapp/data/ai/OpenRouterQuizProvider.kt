package com.example.onlinequizapp.data.ai

import com.example.onlinequizapp.data.session.AiConfigManager
import com.example.onlinequizapp.data.session.AiConnectionTester
import com.example.onlinequizapp.domain.ai.AIQuizGenerator
import com.example.onlinequizapp.domain.ai.AiProvider
import com.example.onlinequizapp.domain.ai.AiQuizRequest
import com.example.onlinequizapp.domain.model.QuestionDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Quiz Generator utilizing OpenRouter Free models (e.g. NVIDIA Nemotron 3 Ultra 550B Free, Llama 3.3 Free).
 */
class OpenRouterQuizProvider : AIQuizGenerator {

    override val provider: AiProvider = AiProvider.OPENROUTER

    override suspend fun generateQuestions(
        request: AiQuizRequest,
        apiKey: String
    ): Result<List<QuestionDraft>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("OpenRouter API key is missing. Please add your API key in settings."))
        }

        val savedModel = AiConfigManager.getOpenRouterModel()
        val candidateModels = linkedSetOf<String>().apply {
            if (savedModel.isNotBlank()) add(savedModel)
            addAll(AiConnectionTester.DEFAULT_OPENROUTER_MODELS)
        }.toList()

        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                val endpoint = "https://openrouter.ai/api/v1/chat/completions"
                val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 30000
                    readTimeout = 40000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                    setRequestProperty("HTTP-Referer", "https://quizmaster.example.com")
                    setRequestProperty("X-Title", "QuizMaster")
                }

                val prompt = AIQuizPromptHelper.buildPrompt(request)

                val payload = JSONObject().apply {
                    put("model", model)
                    put("stream", false)
                    put("max_tokens", 4000)
                    val messages = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are an academic test maker. Always return only valid JSON without markdown wrapping or conversational text.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        })
                    }
                    put("messages", messages)
                    put("temperature", 0.7)
                }

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val statusCode = conn.responseCode
                if (statusCode == 404 || statusCode == 429 || statusCode == 503) {
                    // Try next model if current model is unavailable, rate-limited, or server busy
                    continue
                }

                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (!isSuccess) {
                    if ((statusCode == 400 || statusCode == 404) && (responseBody.contains("model") || responseBody.contains("not found"))) {
                        continue
                    }
                    val errorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (e: Exception) {
                        responseBody.take(200)
                    }
                    return@withContext Result.failure(Exception("OpenRouter error ($statusCode): $errorMsg"))
                }

                val json = JSONObject(responseBody)
                val choices = json.optJSONArray("choices")
                val text = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content")
                    ?: return@withContext Result.failure(Exception("No content returned from OpenRouter."))

                val drafts = AIQuizValidator.parseAndValidate(text, request.questionType)
                if (drafts.isEmpty()) {
                    return@withContext Result.failure(Exception("OpenRouter returned invalid or unparseable questions format."))
                }

                AiConfigManager.setOpenRouterModel(model)
                return@withContext Result.success(drafts)
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Failed to generate quiz using OpenRouter free-tier models."))
    }
}


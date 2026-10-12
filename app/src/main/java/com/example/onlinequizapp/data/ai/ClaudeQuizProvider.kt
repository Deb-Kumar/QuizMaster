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
 * Quiz Generator utilizing Anthropic Claude Messages API (Claude 3.5 Haiku, Sonnet).
 */
class ClaudeQuizProvider : AIQuizGenerator {

    override val provider: AiProvider = AiProvider.CLAUDE

    override suspend fun generateQuestions(
        request: AiQuizRequest,
        apiKey: String
    ): Result<List<QuestionDraft>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Anthropic Claude API key is missing. Please add your API key in settings."))
        }

        val savedModel = AiConfigManager.getClaudeModel()
        val candidateModels = linkedSetOf<String>().apply {
            if (savedModel.isNotBlank()) add(savedModel)
            addAll(AiConnectionTester.DEFAULT_CLAUDE_MODELS)
        }.toList()

        var lastError: Exception? = null

        for (model in candidateModels) {
            try {
                val endpoint = "https://api.anthropic.com/v1/messages"
                val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 30000
                    readTimeout = 40000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-api-key", apiKey.trim())
                    setRequestProperty("anthropic-version", "2023-06-01")
                }

                val prompt = AIQuizPromptHelper.buildPrompt(request)

                val payload = JSONObject().apply {
                    put("model", model)
                    put("max_tokens", 4096)
                    put("system", "You are an academic test maker. Always return only valid JSON without markdown wrapping or conversational text.")
                    val messages = JSONArray().apply {
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
                if (statusCode == 404 || statusCode == 429 || statusCode == 529) {
                    continue
                }

                val isSuccess = statusCode in 200..299
                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                if (!isSuccess) {
                    if ((statusCode == 400 || statusCode == 404) && (responseBody.contains("model") || responseBody.contains("not_found"))) {
                        continue
                    }
                    val errorMsg = try {
                        JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                    } catch (_: Exception) {
                        responseBody.take(200)
                    }
                    return@withContext Result.failure(Exception("Claude error ($statusCode): $errorMsg"))
                }

                val json = JSONObject(responseBody)
                val contentArr = json.optJSONArray("content")
                val sb = StringBuilder()
                if (contentArr != null) {
                    for (i in 0 until contentArr.length()) {
                        val block = contentArr.optJSONObject(i)
                        val blockText = block?.optString("text", "") ?: ""
                        if (blockText.isNotBlank()) sb.append(blockText)
                    }
                }
                val text = sb.toString().ifBlank {
                    contentArr?.optJSONObject(0)?.optString("text") ?: ""
                }
                if (text.isBlank()) {
                    return@withContext Result.failure(Exception("No content returned from Claude."))
                }

                val drafts = AIQuizValidator.parseAndValidate(text, request.questionType)
                if (drafts.isEmpty()) {
                    return@withContext Result.failure(Exception("Claude returned invalid or unparseable questions format."))
                }

                AiConfigManager.setClaudeModel(model)
                return@withContext Result.success(drafts)
            } catch (e: Exception) {
                lastError = e
            }
        }

        Result.failure(lastError ?: Exception("Failed to generate quiz using Claude."))
    }
}

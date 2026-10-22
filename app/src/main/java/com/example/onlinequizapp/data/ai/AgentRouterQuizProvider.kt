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
 * Quiz Generator utilizing AgentRouter (deepseek-v4-flash, gpt-6-astra, claude-opus-4-8, etc.).
 */
class AgentRouterQuizProvider : AIQuizGenerator {

    override val provider: AiProvider = AiProvider.AGENTROUTER

    override suspend fun generateQuestions(
        request: AiQuizRequest,
        apiKey: String
    ): Result<List<QuestionDraft>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("AgentRouter API key is missing. Please add your API key in settings."))
        }

        val savedModel = AiConfigManager.getAgentRouterModel()
        val candidateModels = linkedSetOf<String>().apply {
            if (savedModel.isNotBlank()) add(savedModel)
            addAll(AiConnectionTester.DEFAULT_AGENTROUTER_MODELS)
        }.toList()

        val endpoints = listOf(
            "https://co.agentrouter.org/v1/chat/completions",
            "https://agentrouter.org/v1/chat/completions"
        )

        var lastError: Exception? = null

        for (model in candidateModels) {
            for (endpoint in endpoints) {
                try {
                    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 25000
                        readTimeout = 40000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
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
                        continue
                    }

                    val isSuccess = statusCode in 200..299
                    val stream = if (isSuccess) conn.inputStream else conn.errorStream
                    val responseBody = BufferedReader(InputStreamReader(stream)).use { it.readText() }

                    if (!isSuccess) {
                        if ((statusCode == 400 || statusCode == 404) && responseBody.contains("model")) {
                            continue
                        }
                        val errorMsg = try {
                            JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                        } catch (e: Exception) {
                            responseBody.take(200)
                        }
                        return@withContext Result.failure(Exception("AgentRouter error ($statusCode): $errorMsg"))
                    }

                    val json = JSONObject(responseBody)
                    val choices = json.optJSONArray("choices")
                    val msg = choices?.optJSONObject(0)?.optJSONObject("message")
                    val text = msg?.optString("content")?.ifBlank {
                        msg.optString("reasoning_content")
                    } ?: return@withContext Result.failure(Exception("No content returned from AgentRouter."))

                    val drafts = AIQuizValidator.parseAndValidate(text, request.questionType)
                    if (drafts.isEmpty()) {
                        return@withContext Result.failure(Exception("AgentRouter returned invalid or unparseable questions format."))
                    }

                    AiConfigManager.setAgentRouterModel(model)
                    return@withContext Result.success(drafts)
                } catch (e: Exception) {
                    lastError = e
                }
            }
        }

        Result.failure(lastError ?: Exception("Failed to generate quiz using AgentRouter."))
    }
}


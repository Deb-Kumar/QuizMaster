package com.example.onlinequizapp.data.ai

import com.example.onlinequizapp.data.session.AiConfigManager
import com.example.onlinequizapp.domain.ai.AIQuizGenerator
import com.example.onlinequizapp.domain.ai.AiProvider

object AIQuizGeneratorFactory {

    fun getGenerator(provider: AiProvider): AIQuizGenerator {
        return when (provider) {
            AiProvider.GEMINI -> GeminiQuizProvider()
            AiProvider.OPENAI -> OpenAiQuizProvider()
            AiProvider.OPENROUTER -> OpenRouterQuizProvider()
            AiProvider.CLAUDE -> ClaudeQuizProvider()
            AiProvider.DEEPSEEK -> DeepSeekQuizProvider()
            AiProvider.AGENTROUTER -> AgentRouterQuizProvider()
        }
    }

    fun getApiKeyForProvider(provider: AiProvider): String {
        return when (provider) {
            AiProvider.GEMINI -> AiConfigManager.getGeminiKey()
            AiProvider.OPENAI -> AiConfigManager.getOpenAiKey()
            AiProvider.OPENROUTER -> AiConfigManager.getOpenRouterKey()
            AiProvider.CLAUDE -> AiConfigManager.getClaudeKey()
            AiProvider.DEEPSEEK -> AiConfigManager.getDeepSeekKey()
            AiProvider.AGENTROUTER -> AiConfigManager.getAgentRouterKey()
        }
    }

    fun hasApiKey(provider: AiProvider): Boolean {
        return getApiKeyForProvider(provider).isNotBlank()
    }
}

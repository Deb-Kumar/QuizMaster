package com.example.onlinequizapp.domain.ai

enum class AiProvider(val displayName: String, val recommendedModel: String) {
    GEMINI("Google Gemini", "gemini-2.0-flash"),
    OPENAI("OpenAI", "gpt-4o-mini"),
    OPENROUTER("OpenRouter (Free)", "nvidia/nemotron-3-ultra-550b-a55b:free"),
    CLAUDE("Claude (Anthropic)", "claude-3-5-haiku-latest"),
    DEEPSEEK("DeepSeek", "deepseek-chat"),
    AGENTROUTER("AgentRouter", "gpt-5.6-sol")
}

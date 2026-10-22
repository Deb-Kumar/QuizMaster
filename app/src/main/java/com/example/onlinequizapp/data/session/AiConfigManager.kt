package com.example.onlinequizapp.data.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages secure persistent storage for AI API keys (OpenAI, Gemini)
 * configured by the user in settings.
 */
object AiConfigManager {

    private const val PREFS_NAME = "quiz_master_ai_config_prefs"
    private const val KEY_OPENAI_KEY = "key_openai_api"
    private const val KEY_GEMINI_KEY = "key_gemini_api"
    private const val KEY_GEMINI_MODEL = "key_gemini_model"

    private const val KEY_OPENAI_MODEL = "key_openai_model"
    private const val KEY_OPENROUTER_KEY = "key_openrouter_api"
    private const val KEY_OPENROUTER_MODEL = "key_openrouter_model"

    private const val KEY_CLAUDE_KEY = "key_claude_api"
    private const val KEY_CLAUDE_MODEL = "key_claude_model"
    private const val KEY_DEEPSEEK_KEY = "key_deepseek_api"
    private const val KEY_DEEPSEEK_MODEL = "key_deepseek_model"
    private const val KEY_AGENTROUTER_KEY = "key_agentrouter_api"
    private const val KEY_AGENTROUTER_MODEL = "key_agentrouter_model"

    // Provider Enabled / Disabled Toggles
    private const val KEY_OPENAI_ENABLED = "key_openai_enabled"
    private const val KEY_GEMINI_ENABLED = "key_gemini_enabled"
    private const val KEY_OPENROUTER_ENABLED = "key_openrouter_enabled"
    private const val KEY_CLAUDE_ENABLED = "key_claude_enabled"
    private const val KEY_DEEPSEEK_ENABLED = "key_deepseek_enabled"
    private const val KEY_AGENTROUTER_ENABLED = "key_agentrouter_enabled"

    private var context: Context? = null
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getPrefs(): SharedPreferences? {
        if (prefs == null && context != null) {
            prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return prefs
    }

    fun getOpenAiKey(): String = getPrefs()?.getString(KEY_OPENAI_KEY, "") ?: ""
    fun setOpenAiKey(key: String) {
        getPrefs()?.edit()?.putString(KEY_OPENAI_KEY, key.trim())?.apply()
    }

    fun getOpenAiModel(): String = getPrefs()?.getString(KEY_OPENAI_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"
    fun setOpenAiModel(model: String) {
        getPrefs()?.edit()?.putString(KEY_OPENAI_MODEL, model.trim())?.apply()
    }

    fun getGeminiKey(): String = getPrefs()?.getString(KEY_GEMINI_KEY, "") ?: ""
    fun setGeminiKey(key: String) {
        getPrefs()?.edit()?.putString(KEY_GEMINI_KEY, key.trim())?.apply()
    }

    fun getGeminiModel(): String = getPrefs()?.getString(KEY_GEMINI_MODEL, "gemini-2.0-flash") ?: "gemini-2.0-flash"
    fun setGeminiModel(model: String) {
        getPrefs()?.edit()?.putString(KEY_GEMINI_MODEL, model.trim())?.apply()
    }

    fun getOpenRouterKey(): String = getPrefs()?.getString(KEY_OPENROUTER_KEY, "") ?: ""
    fun setOpenRouterKey(key: String) {
        getPrefs()?.edit()?.putString(KEY_OPENROUTER_KEY, key.trim())?.apply()
    }

    fun getOpenRouterModel(): String {
        val model = getPrefs()?.getString(KEY_OPENROUTER_MODEL, "nvidia/nemotron-3-ultra-550b-a55b:free") ?: "nvidia/nemotron-3-ultra-550b-a55b:free"
        return if (model.contains("gemma-4") || model.contains("llama-3.3")) "nvidia/nemotron-3-ultra-550b-a55b:free" else model
    }
    fun setOpenRouterModel(model: String) {
        getPrefs()?.edit()?.putString(KEY_OPENROUTER_MODEL, model.trim())?.apply()
    }

    fun getClaudeKey(): String = getPrefs()?.getString(KEY_CLAUDE_KEY, "") ?: ""
    fun setClaudeKey(key: String) {
        getPrefs()?.edit()?.putString(KEY_CLAUDE_KEY, key.trim())?.apply()
    }

    fun getClaudeModel(): String = getPrefs()?.getString(KEY_CLAUDE_MODEL, "claude-3-5-haiku-latest") ?: "claude-3-5-haiku-latest"
    fun setClaudeModel(model: String) {
        getPrefs()?.edit()?.putString(KEY_CLAUDE_MODEL, model.trim())?.apply()
    }

    fun getDeepSeekKey(): String = getPrefs()?.getString(KEY_DEEPSEEK_KEY, "") ?: ""
    fun setDeepSeekKey(key: String) {
        getPrefs()?.edit()?.putString(KEY_DEEPSEEK_KEY, key.trim())?.apply()
    }

    fun getDeepSeekModel(): String = getPrefs()?.getString(KEY_DEEPSEEK_MODEL, "deepseek-chat") ?: "deepseek-chat"
    fun setDeepSeekModel(model: String) {
        getPrefs()?.edit()?.putString(KEY_DEEPSEEK_MODEL, model.trim())?.apply()
    }

    fun getAgentRouterKey(): String = getPrefs()?.getString(KEY_AGENTROUTER_KEY, "") ?: ""
    fun setAgentRouterKey(key: String) {
        getPrefs()?.edit()?.putString(KEY_AGENTROUTER_KEY, key.trim())?.apply()
    }

    fun getAgentRouterModel(): String = getPrefs()?.getString(KEY_AGENTROUTER_MODEL, "deepseek-v4-flash") ?: "deepseek-v4-flash"
    fun setAgentRouterModel(model: String) {
        getPrefs()?.edit()?.putString(KEY_AGENTROUTER_MODEL, model.trim())?.apply()
    }

    fun isOpenAiEnabled(): Boolean = getPrefs()?.getBoolean(KEY_OPENAI_ENABLED, getOpenAiKey().isNotBlank()) ?: false
    fun setOpenAiEnabled(enabled: Boolean) {
        getPrefs()?.edit()?.putBoolean(KEY_OPENAI_ENABLED, enabled)?.apply()
    }

    fun isGeminiEnabled(): Boolean = getPrefs()?.getBoolean(KEY_GEMINI_ENABLED, getGeminiKey().isNotBlank() || true) ?: true
    fun setGeminiEnabled(enabled: Boolean) {
        getPrefs()?.edit()?.putBoolean(KEY_GEMINI_ENABLED, enabled)?.apply()
    }

    fun isOpenRouterEnabled(): Boolean = getPrefs()?.getBoolean(KEY_OPENROUTER_ENABLED, getOpenRouterKey().isNotBlank()) ?: false
    fun setOpenRouterEnabled(enabled: Boolean) {
        getPrefs()?.edit()?.putBoolean(KEY_OPENROUTER_ENABLED, enabled)?.apply()
    }

    fun isClaudeEnabled(): Boolean = getPrefs()?.getBoolean(KEY_CLAUDE_ENABLED, getClaudeKey().isNotBlank()) ?: false
    fun setClaudeEnabled(enabled: Boolean) {
        getPrefs()?.edit()?.putBoolean(KEY_CLAUDE_ENABLED, enabled)?.apply()
    }

    fun isDeepSeekEnabled(): Boolean = getPrefs()?.getBoolean(KEY_DEEPSEEK_ENABLED, getDeepSeekKey().isNotBlank()) ?: false
    fun setDeepSeekEnabled(enabled: Boolean) {
        getPrefs()?.edit()?.putBoolean(KEY_DEEPSEEK_ENABLED, enabled)?.apply()
    }

    fun isAgentRouterEnabled(): Boolean = getPrefs()?.getBoolean(KEY_AGENTROUTER_ENABLED, getAgentRouterKey().isNotBlank()) ?: false
    fun setAgentRouterEnabled(enabled: Boolean) {
        getPrefs()?.edit()?.putBoolean(KEY_AGENTROUTER_ENABLED, enabled)?.apply()
    }

    fun saveAllKeys(
        openAi: String,
        gemini: String,
        openRouter: String = getOpenRouterKey(),
        claude: String = getClaudeKey(),
        deepSeek: String = getDeepSeekKey(),
        agentRouter: String = getAgentRouterKey()
    ) {
        getPrefs()?.edit()
            ?.putString(KEY_OPENAI_KEY, openAi.trim())
            ?.putString(KEY_GEMINI_KEY, gemini.trim())
            ?.putString(KEY_OPENROUTER_KEY, openRouter.trim())
            ?.putString(KEY_CLAUDE_KEY, claude.trim())
            ?.putString(KEY_DEEPSEEK_KEY, deepSeek.trim())
            ?.putString(KEY_AGENTROUTER_KEY, agentRouter.trim())
            ?.apply()
    }

    fun clearAllKeys() {
        getPrefs()?.edit()
            ?.remove(KEY_OPENAI_KEY)
            ?.remove(KEY_GEMINI_KEY)
            ?.remove(KEY_OPENROUTER_KEY)
            ?.remove(KEY_CLAUDE_KEY)
            ?.remove(KEY_DEEPSEEK_KEY)
            ?.remove(KEY_AGENTROUTER_KEY)
            ?.apply()
    }
}


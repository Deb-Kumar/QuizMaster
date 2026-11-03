import React, { useState } from 'react';
import type { Question, Difficulty, QuestionType } from '../../types';
import { generateQuizWithAi, type AiProvider, getSavedAiKeys } from '../../lib/aiQuizGenerator';
import { Sparkles, X, Bot, Key, AlertCircle, ArrowRight, Loader2, Check } from 'lucide-react';

interface AiGenerateModalProps {
  isOpen: boolean;
  onClose: () => void;
  category: string;
  onGenerated: (questions: Question[], topic: string) => void;
}

export const AiGenerateModal: React.FC<AiGenerateModalProps> = ({ 
  isOpen, 
  onClose, 
  category, 
  onGenerated 
}) => {
  const [provider, setProvider] = useState<AiProvider>('AGENTROUTER');
  const [topic, setTopic] = useState('');
  const [difficulty, setDifficulty] = useState<Difficulty>('MEDIUM');
  const [questionType, setQuestionType] = useState<QuestionType>('SINGLE');
  const [questionCount, setQuestionCount] = useState<number>(5);
  const [customKey, setCustomKey] = useState('');
  const [loading, setLoading] = useState(false);
  const [statusText, setStatusText] = useState('');
  const [error, setError] = useState('');

  if (!isOpen) return null;

  const savedKeys = getSavedAiKeys();
  const hasKey = Boolean(customKey.trim() || savedKeys[provider.toLowerCase()]);

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!topic.trim()) {
      setError('Please provide a quiz topic or subject description.');
      return;
    }

    setError('');
    setLoading(true);
    setStatusText(`Connecting to ${provider} AI model...`);

    try {
      const generated = await generateQuizWithAi({
        provider,
        topic: topic.trim(),
        category,
        difficulty,
        questionType,
        questionCount,
        apiKey: customKey.trim()
      });

      if (generated && generated.length > 0) {
        onGenerated(generated, topic.trim());
        onClose();
      } else {
        setError('Failed to generate questions. Please verify your API key or prompt.');
      }
    } catch (err) {
      console.error(err);
      setError('An error occurred during AI generation. Check your network or API quota.');
    } finally {
      setLoading(false);
      setStatusText('');
    }
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-2xl bg-[#090E26] border border-[#16214C] rounded-[28px] p-6 sm:p-8 shadow-2xl relative max-h-[92vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -15px rgba(124, 58, 237, 0.3)'
        }}
      >
        {/* Android Ambient Halo */}
        <div className="absolute -top-20 -right-20 w-48 h-48 bg-purple-600/20 rounded-full blur-3xl pointer-events-none" />

        {/* Header matching Android ProviderSelectStep.kt */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-purple-600 to-indigo-600 flex items-center justify-center shadow-lg shadow-purple-600/30">
              <Bot className="w-6 h-6 text-white" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">AI Quiz Generator</h3>
              <p className="text-xs text-slate-400">Generate high-yield questions automatically</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-900 text-slate-400 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Hero illustration card matching Android img_ai_robot_banner */}
        <div className="my-5 p-4 rounded-2xl bg-gradient-to-r from-[#190B38] to-[#0D153B] border border-purple-500/30 flex items-center justify-between gap-4">
          <div className="space-y-1">
            <span className="text-[10px] font-bold text-purple-300 uppercase tracking-widest px-2 py-0.5 rounded-full bg-purple-500/20">
              Powered by LLM Models
            </span>
            <h4 className="text-sm font-bold text-white">Smart Question Synthesis</h4>
            <p className="text-xs text-slate-300">
              Generates questions, 4 plausible options, verified answer keys, hints, and curriculum explanations in seconds.
            </p>
          </div>
          <div className="w-24 sm:w-28 flex-shrink-0">
            <img 
              src="/img_ai_robot_banner.png" 
              alt="AI Robot" 
              className="w-full h-auto object-contain filter drop-shadow-[0_10px_20px_rgba(0,0,0,0.5)]"
              onError={(e) => {
                (e.target as HTMLImageElement).style.display = 'none';
              }}
            />
          </div>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs font-medium flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleGenerate} className="space-y-4">
          {/* Provider Select Cards */}
          <div>
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">
              Select AI Engine *
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
              {[
                { id: 'AGENTROUTER', name: 'AgentRouter', desc: 'gpt-5.6-sol / gpt-6', badge: 'Active' },
                { id: 'GEMINI', name: 'Google Gemini', desc: '2.0 / 1.5 Flash', badge: 'Fast' },
                { id: 'OPENAI', name: 'OpenAI GPT-4o', desc: 'Mini / 4o Standard', badge: 'Popular' },
                { id: 'OPENROUTER', name: 'OpenRouter', desc: 'Free & Ultra Models', badge: 'Cloud' },
                { id: 'CLAUDE', name: 'Anthropic Claude', desc: 'Claude 3.5 Sonnet', badge: 'High Logic' },
                { id: 'DEEPSEEK', name: 'DeepSeek', desc: 'Chat / Reasoner', badge: 'Reasoner' }
              ].map((p) => {
                const isSelected = provider === p.id;
                return (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => setProvider(p.id as AiProvider)}
                    className={`p-3 rounded-2xl border text-left transition-all cursor-pointer relative ${
                      isSelected
                        ? 'bg-purple-600/15 border-purple-500 shadow-md shadow-purple-600/20'
                        : 'bg-[#05091F] border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-xs text-white">{p.name}</span>
                      <span className="text-[9px] font-bold text-purple-400 bg-purple-500/10 px-1.5 py-0.5 rounded-full">
                        {p.badge}
                      </span>
                    </div>
                    <p className="text-[11px] text-slate-400 mt-1">{p.desc}</p>
                    {isSelected && (
                      <div className="absolute top-2 right-2 w-2 h-2 rounded-full bg-purple-400 animate-ping" />
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Topic / Prompt */}
          <div>
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1">
              Topic, Syllabus or Subject *
            </label>
            <input
              type="text"
              required
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="e.g. Android Jetpack Compose State Management, Side-effects & Coroutines"
              className="w-full bg-[#05091F] border border-slate-800 focus:border-purple-500 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
            />
          </div>

          {/* Parameters Grid */}
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                Question Count
              </label>
              <select
                value={questionCount}
                onChange={(e) => setQuestionCount(Number(e.target.value))}
                className="w-full bg-[#05091F] border border-slate-800 rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-purple-500"
              >
                <option value={3}>3 Questions</option>
                <option value={5}>5 Questions</option>
                <option value={10}>10 Questions</option>
                <option value={15}>15 Questions</option>
                <option value={20}>20 Questions</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                Difficulty
              </label>
              <select
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value as Difficulty)}
                className="w-full bg-[#05091F] border border-slate-800 rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-purple-500"
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                Format
              </label>
              <select
                value={questionType}
                onChange={(e) => setQuestionType(e.target.value as QuestionType)}
                className="w-full bg-[#05091F] border border-slate-800 rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-purple-500"
              >
                <option value="SINGLE">Single Choice</option>
                <option value="MULTIPLE">Multiple Choice</option>
                <option value="MIXED">Mixed Blend</option>
              </select>
            </div>
          </div>

          {/* API Key input */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                <Key className="w-3.5 h-3.5 text-purple-400" />
                <span>{provider} API Key (Optional)</span>
              </label>
              {hasKey && (
                <span className="text-[10px] text-emerald-400 font-semibold flex items-center gap-1">
                  <Check className="w-3 h-3" />
                  Key Available
                </span>
              )}
            </div>
            <input
              type="password"
              value={customKey}
              onChange={(e) => setCustomKey(e.target.value)}
              placeholder="Paste key to override or leave empty to use saved key"
              className="w-full bg-[#05091F] border border-slate-800 focus:border-purple-500 rounded-xl px-4 py-2 text-white placeholder-slate-600 text-xs focus:outline-none transition-colors"
            />
          </div>

          {/* Live Generating Status */}
          {loading && (
            <div className="p-3 rounded-xl bg-purple-500/10 border border-purple-500/30 text-purple-300 text-xs flex items-center gap-2.5">
              <Loader2 className="w-4 h-4 animate-spin text-purple-400 shrink-0" />
              <span>{statusText || 'Synthesizing verified exam questions...'}</span>
            </div>
          )}

          {/* Action buttons */}
          <div className="pt-3 flex items-center justify-end gap-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 text-xs font-semibold transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 disabled:opacity-40 text-white text-xs font-bold shadow-lg shadow-purple-600/30 transition-all flex items-center gap-2 cursor-pointer"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Generating Questions...</span>
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  <span>Generate with AI</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

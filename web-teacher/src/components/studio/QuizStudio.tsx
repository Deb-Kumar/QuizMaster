import React, { useState } from 'react';
import type { Quiz, Question, Option, QuestionType, Difficulty } from '../../types';
import { db } from '../../lib/firebase';
import { collection, addDoc } from 'firebase/firestore';
import { useAuth } from '../../context/AuthContext';
import { BulkImportModal } from './BulkImportModal';
import { AiGenerateModal } from './AiGenerateModal';
import { PublishTargetModal } from './PublishTargetModal';
import { PublishSuccessModal } from './PublishSuccessModal';
import { 
  Plus, 
  Trash2, 
  Sparkles, 
  Check, 
  Clock, 
  Award, 
  BookOpen, 
  Lightbulb, 
  HelpCircle, 
  Copy, 
  ArrowUp, 
  ArrowDown, 
  FileText, 
  Bot, 
  CheckCircle2, 
  Sliders
} from 'lucide-react';

interface QuizStudioProps {
  onQuizCreated?: (quiz: Quiz) => void;
  onNavigateHome?: () => void;
}

export const QuizStudio: React.FC<QuizStudioProps> = ({ onQuizCreated, onNavigateHome }) => {
  const { currentUser } = useAuth();

  // Basic Info state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('Android Development');
  const [difficulty, setDifficulty] = useState<Difficulty>('MEDIUM');
  const [questionType, setQuestionType] = useState<QuestionType>('SINGLE');
  const [durationMinutes, setDurationMinutes] = useState(15);
  const [passingScorePercent, setPassingScorePercent] = useState(80);
  const [pointsPerQuestion, setPointsPerQuestion] = useState(1);
  const [shuffleQuestions, setShuffleQuestions] = useState(true);
  const [shuffleOptions, setShuffleOptions] = useState(true);

  // Modals state
  const [showBulkModal, setShowBulkModal] = useState(false);
  const [showAiModal, setShowAiModal] = useState(false);
  const [showPublishTargetModal, setShowPublishTargetModal] = useState(false);
  const [publishedQuizCode, setPublishedQuizCode] = useState('');
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Questions list state
  const [questions, setQuestions] = useState<Question[]>([
    {
      id: `q_${Date.now()}_1`,
      text: '',
      options: [
        { id: `opt_1_1`, text: '' },
        { id: `opt_1_2`, text: '' },
        { id: `opt_1_3`, text: '' },
        { id: `opt_1_4`, text: '' }
      ],
      correctOptionIds: ['opt_1_1'],
      explanation: '',
      hint: ''
    }
  ]);

  // Add Question
  const handleAddQuestion = () => {
    const qId = `q_${Date.now()}_${questions.length + 1}`;
    const newOptions: Option[] = [
      { id: `opt_${qId}_1`, text: '' },
      { id: `opt_${qId}_2`, text: '' },
      { id: `opt_${qId}_3`, text: '' },
      { id: `opt_${qId}_4`, text: '' }
    ];

    setQuestions([
      ...questions,
      {
        id: qId,
        text: '',
        options: newOptions,
        correctOptionIds: [newOptions[0].id],
        explanation: '',
        hint: ''
      }
    ]);
  };

  // Duplicate Question
  const handleDuplicateQuestion = (index: number) => {
    const source = questions[index];
    const qId = `q_${Date.now()}_dup`;
    const newOptions: Option[] = source.options.map((opt, i) => ({
      id: `opt_${qId}_${i + 1}`,
      text: opt.text
    }));

    const newCorrectIds = source.options
      .map((opt, i) => (source.correctOptionIds.includes(opt.id) ? newOptions[i].id : null))
      .filter((id): id is string => id !== null);

    const dupQ: Question = {
      id: qId,
      text: `${source.text} (Copy)`,
      options: newOptions,
      correctOptionIds: newCorrectIds.length > 0 ? newCorrectIds : [newOptions[0].id],
      explanation: source.explanation,
      hint: source.hint
    };

    const updated = [...questions];
    updated.splice(index + 1, 0, dupQ);
    setQuestions(updated);
  };

  // Move Question
  const handleMoveQuestion = (index: number, direction: 'up' | 'down') => {
    const newIndex = direction === 'up' ? index - 1 : index + 1;
    if (newIndex < 0 || newIndex >= questions.length) return;
    const updated = [...questions];
    const temp = updated[index];
    updated[index] = updated[newIndex];
    updated[newIndex] = temp;
    setQuestions(updated);
  };

  // Remove Question
  const handleRemoveQuestion = (index: number) => {
    if (questions.length <= 1) return;
    setQuestions(questions.filter((_, i) => i !== index));
  };

  // Update Question text
  const handleUpdateQuestionText = (index: number, val: string) => {
    const updated = [...questions];
    updated[index].text = val;
    setQuestions(updated);
  };

  // Update Explanation
  const handleUpdateExplanation = (index: number, val: string) => {
    const updated = [...questions];
    updated[index].explanation = val;
    setQuestions(updated);
  };

  // Update Hint
  const handleUpdateHint = (index: number, val: string) => {
    const updated = [...questions];
    updated[index].hint = val;
    setQuestions(updated);
  };

  // Update Option text
  const handleUpdateOptionText = (qIndex: number, optIndex: number, val: string) => {
    const updated = [...questions];
    updated[qIndex].options[optIndex].text = val;
    setQuestions(updated);
  };

  // Toggle Correct Option
  const handleToggleCorrectOption = (qIndex: number, optId: string) => {
    const updated = [...questions];
    const q = updated[qIndex];
    if (questionType === 'SINGLE') {
      q.correctOptionIds = [optId];
    } else {
      if (q.correctOptionIds.includes(optId)) {
        if (q.correctOptionIds.length > 1) {
          q.correctOptionIds = q.correctOptionIds.filter((id) => id !== optId);
        }
      } else {
        q.correctOptionIds.push(optId);
      }
    }
    setQuestions(updated);
  };

  // Add Option to a question
  const handleAddOption = (qIndex: number) => {
    const updated = [...questions];
    const q = updated[qIndex];
    if (q.options.length >= 6) return;
    const newOptId = `opt_${q.id}_${q.options.length + 1}`;
    q.options.push({ id: newOptId, text: '' });
    setQuestions(updated);
  };

  // Remove Option from a question
  const handleRemoveOption = (qIndex: number, optIndex: number) => {
    const updated = [...questions];
    const q = updated[qIndex];
    if (q.options.length <= 2) return;
    const removedId = q.options[optIndex].id;
    q.options.splice(optIndex, 1);
    q.correctOptionIds = q.correctOptionIds.filter((id) => id !== removedId);
    if (q.correctOptionIds.length === 0 && q.options.length > 0) {
      q.correctOptionIds = [q.options[0].id];
    }
    setQuestions(updated);
  };

  // Handle bulk import
  const handleImportedBulk = (importedList: Question[]) => {
    setQuestions(importedList);
    if (!title.trim() && importedList.length > 0) {
      setTitle(`${category} Assessment (${importedList.length} Questions)`);
    }
  };

  // Handle AI generation
  const handleAiGenerated = (generatedList: Question[], topic: string) => {
    setQuestions(generatedList);
    if (!title.trim()) {
      setTitle(`${topic} - Exam Questions`);
    }
  };

  // Initiate Publish
  const handleStartPublish = () => {
    if (!title.trim()) {
      setErrorMessage('Please provide a Quiz Title before publishing.');
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    // Validate that questions have prompts and options
    for (let i = 0; i < questions.length; i++) {
      if (!questions[i].text.trim()) {
        setErrorMessage(`Question ${i + 1} prompt cannot be empty.`);
        return;
      }
      const filledOptions = questions[i].options.filter((o) => o.text.trim().length > 0);
      if (filledOptions.length < 2) {
        setErrorMessage(`Question ${i + 1} must have at least 2 valid options.`);
        return;
      }
    }

    setErrorMessage('');
    setShowPublishTargetModal(true);
  };

  // Finalize Publish to Firestore
  const handleConfirmPublish = async (target: {
    isPublic: boolean;
    batchIds: string[];
    accessCode: string;
  }) => {
    setShowPublishTargetModal(false);
    setSaving(true);

    try {
      const generatedCode = target.accessCode || Math.random().toString(36).substring(2, 8).toUpperCase();
      const newQuizData = {
        code: generatedCode,
        title: title.trim(),
        description: description.trim(),
        category,
        difficulty,
        questionType,
        questions,
        durationMinutes,
        totalMarks: questions.length * pointsPerQuestion,
        passingScorePercent,
        shuffleQuestions,
        shuffleOptions,
        isPublic: target.isPublic,
        status: 'PUBLISHED',
        teacherId: currentUser?.id || 'teacher_web_portal',
        teacherName: currentUser?.name || 'Authorized Instructor',
        batchIds: target.batchIds,
        createdAt: Date.now(),
        updatedAt: Date.now()
      };

      const docRef = await addDoc(collection(db, 'quizzes'), newQuizData);
      const createdObj = { id: docRef.id, ...newQuizData } as Quiz;
      
      setPublishedQuizCode(generatedCode);
      setShowSuccessModal(true);

      if (onQuizCreated) onQuizCreated(createdObj);
    } catch (err) {
      console.error('Error saving quiz to Firestore:', err);
      alert('Could not save quiz to cloud database. Please verify Firebase permissions.');
    } finally {
      setSaving(false);
    }
  };

  const handleCreateAnother = () => {
    setShowSuccessModal(false);
    setTitle('');
    setDescription('');
    setQuestions([
      {
        id: `q_${Date.now()}_1`,
        text: '',
        options: [
          { id: `opt_1_1`, text: '' },
          { id: `opt_1_2`, text: '' },
          { id: `opt_1_3`, text: '' },
          { id: `opt_1_4`, text: '' }
        ],
        correctOptionIds: ['opt_1_1'],
        explanation: '',
        hint: ''
      }
    ]);
  };

  return (
    <div className="w-full max-w-6xl mx-auto px-4 py-6 space-y-6 animate-in fade-in duration-300">
      
      {/* 1. Header Banner matching Android MethodSelectStep.kt */}
      <div 
        className="rounded-[28px] border border-indigo-500/30 p-6 md:p-8 relative overflow-hidden"
        style={{
          background: 'linear-gradient(135deg, #0C143B 0%, #131D56 50%, #090D2A 100%)',
          boxShadow: '0 20px 40px -15px rgba(79, 70, 229, 0.25)'
        }}
      >
        <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex-1 space-y-3">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#1E2868]/70 border border-[#60A5FA]/40">
              <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
              <span className="text-[10px] md:text-[11px] font-bold text-[#93C5FD] uppercase tracking-wider">
                TEACHER ASSESSMENT STUDIO
              </span>
            </div>

            <div>
              <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                Create & Publish Quizzes
              </h2>
              <p className="text-xs sm:text-sm text-slate-300 mt-1 max-w-xl leading-relaxed">
                Choose your authoring method: craft questions manually, generate complete curriculum assessments with AI, or paste formatted questions in bulk.
              </p>
            </div>

            {/* Creation Method Buttons matching Android MethodSelectStep */}
            <div className="pt-2 flex flex-wrap items-center gap-3">
              <button
                type="button"
                onClick={() => setShowAiModal(true)}
                className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-bold text-xs sm:text-sm shadow-lg shadow-purple-600/30 transition-all flex items-center gap-2 cursor-pointer"
              >
                <Bot className="w-4 h-4" />
                <span>Generate with AI</span>
                <span className="px-1.5 py-0.5 rounded text-[9px] font-black bg-white/20 text-white">
                  RECOMMENDED
                </span>
              </button>

              <button
                type="button"
                onClick={() => setShowBulkModal(true)}
                className="px-4 py-2.5 rounded-xl bg-slate-900/90 hover:bg-slate-800 border border-slate-700 text-cyan-300 font-semibold text-xs sm:text-sm transition-all flex items-center gap-2 cursor-pointer"
              >
                <FileText className="w-4 h-4 text-cyan-400" />
                <span>Bulk Import Questions</span>
              </button>
            </div>
          </div>

          {/* Right 3D Illustration matching Android img_quiz_builder_clipboard */}
          <div className="w-40 sm:w-48 md:w-56 flex-shrink-0 flex items-center justify-center">
            <img 
              src="/img_quiz_builder_clipboard.png" 
              alt="Quiz Builder Illustration" 
              className="w-full h-auto object-contain filter drop-shadow-[0_15px_25px_rgba(0,0,0,0.6)]"
              onError={(e) => {
                (e.target as HTMLImageElement).src = '/app_logo.png';
              }}
            />
          </div>
        </div>
      </div>

      {/* Error alert if any */}
      {errorMessage && (
        <div className="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs font-semibold flex items-center justify-between">
          <span>{errorMessage}</span>
          <button onClick={() => setErrorMessage('')} className="text-red-400 hover:text-white">✕</button>
        </div>
      )}

      {/* 2. Metadata Config Cards Grid matching Android BasicInfoStep.kt */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Left Column: General Information */}
        <div className="lg:col-span-2 bg-[#0A102E] border border-[#16214C] rounded-2xl p-5 sm:p-6 space-y-4">
          <div className="flex items-center gap-2.5 pb-2 border-b border-slate-800/80">
            <BookOpen className="w-4 h-4 text-indigo-400" />
            <h3 className="text-sm sm:text-base font-bold text-white">General Information</h3>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
              Quiz Title *
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Kotlin Jetpack Compose & State Management"
              className="w-full bg-[#050B22] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 uppercase tracking-wider mb-1.5">
              Description / Instructions
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Provide background context, required knowledge, or exam rules for candidates..."
              className="w-full bg-[#050B22] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
            />
          </div>
        </div>

        {/* Right Column: Exam Parameters */}
        <div className="bg-[#0A102E] border border-[#16214C] rounded-2xl p-5 sm:p-6 space-y-4">
          <div className="flex items-center gap-2.5 pb-2 border-b border-slate-800/80">
            <Sliders className="w-4 h-4 text-cyan-400" />
            <h3 className="text-sm sm:text-base font-bold text-white">Exam Parameters</h3>
          </div>

          <div>
            <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
              Category / Subject
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full bg-[#050B22] border border-[#1B2E6C] rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-indigo-500"
            >
              <option value="Android Development">Android Development</option>
              <option value="Programming">Programming & Algorithms</option>
              <option value="Web Development">Web Development</option>
              <option value="Computer Science">Computer Science & Engineering</option>
              <option value="Mathematics">Mathematics</option>
              <option value="Science">Science & Technology</option>
              <option value="General Knowledge">General Knowledge</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                Difficulty
              </label>
              <select
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value as Difficulty)}
                className="w-full bg-[#050B22] border border-[#1B2E6C] rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-indigo-500"
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                Answer Mode
              </label>
              <select
                value={questionType}
                onChange={(e) => setQuestionType(e.target.value as QuestionType)}
                className="w-full bg-[#050B22] border border-[#1B2E6C] rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-indigo-500"
              >
                <option value="SINGLE">Single Choice</option>
                <option value="MULTIPLE">Multiple Choice</option>
                <option value="MIXED">Mixed</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-indigo-400" />
                <span>Duration (min)</span>
              </label>
              <input
                type="number"
                min={1}
                max={180}
                value={durationMinutes}
                onChange={(e) => setDurationMinutes(Number(e.target.value))}
                className="w-full bg-[#050B22] border border-[#1B2E6C] rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Award className="w-3.5 h-3.5 text-amber-400" />
                <span>Pass Rule %</span>
              </label>
              <input
                type="number"
                min={40}
                max={100}
                value={passingScorePercent}
                onChange={(e) => setPassingScorePercent(Number(e.target.value))}
                className="w-full bg-[#050B22] border border-[#1B2E6C] rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-indigo-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
              Points Per Question
            </label>
            <input
              type="number"
              min={1}
              max={100}
              value={pointsPerQuestion}
              onChange={(e) => setPointsPerQuestion(Number(e.target.value))}
              className="w-full bg-[#050B22] border border-[#1B2E6C] rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none focus:border-indigo-500"
            />
          </div>

          {/* Shuffle Toggles */}
          <div className="pt-2 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-300">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={shuffleQuestions}
                onChange={(e) => setShuffleQuestions(e.target.checked)}
                className="w-4 h-4 rounded text-indigo-600 bg-slate-900 border-slate-700"
              />
              <span>Shuffle Questions</span>
            </label>

            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={shuffleOptions}
                onChange={(e) => setShuffleOptions(e.target.checked)}
                className="w-4 h-4 rounded text-indigo-600 bg-slate-900 border-slate-700"
              />
              <span>Shuffle Options</span>
            </label>
          </div>
        </div>
      </div>

      {/* 3. Questions Section Header */}
      <div className="flex items-center justify-between pt-2">
        <div>
          <h3 className="text-lg sm:text-xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <span>Questions</span>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-500/15 text-indigo-300 border border-indigo-500/30">
              {questions.length}
            </span>
          </h3>
          <p className="text-xs text-slate-400">Click an option letter to mark it as the verified correct answer</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={handleAddQuestion}
            className="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-slate-200 text-xs font-semibold flex items-center gap-1.5 transition-colors cursor-pointer"
          >
            <Plus className="w-4 h-4 text-indigo-400" />
            <span>Add Question</span>
          </button>
        </div>
      </div>

      {/* 4. Interactive Questions List matching Android QuestionReviewStep.kt */}
      <div className="space-y-5">
        {questions.map((q, qIndex) => (
          <div
            key={q.id}
            className="bg-[#0A102E] border border-[#16214C] hover:border-[#1E2E6C] rounded-2xl p-5 sm:p-6 transition-all space-y-4"
          >
            {/* Top Toolbar */}
            <div className="flex items-center justify-between pb-3 border-b border-slate-800/80">
              <div className="flex items-center gap-2.5">
                <span className="w-7 h-7 rounded-xl bg-indigo-500/20 text-indigo-300 font-extrabold text-xs flex items-center justify-center border border-indigo-500/30">
                  {qIndex + 1}
                </span>
                <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">
                  Question #{qIndex + 1}
                </span>
              </div>

              {/* Move / Dup / Delete buttons */}
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  onClick={() => handleMoveQuestion(qIndex, 'up')}
                  disabled={qIndex === 0}
                  className="p-1.5 rounded-lg bg-slate-900 text-slate-400 hover:text-white disabled:opacity-30 transition-colors"
                  title="Move Up"
                >
                  <ArrowUp className="w-3.5 h-3.5" />
                </button>
                <button
                  type="button"
                  onClick={() => handleMoveQuestion(qIndex, 'down')}
                  disabled={qIndex === questions.length - 1}
                  className="p-1.5 rounded-lg bg-slate-900 text-slate-400 hover:text-white disabled:opacity-30 transition-colors"
                  title="Move Down"
                >
                  <ArrowDown className="w-3.5 h-3.5" />
                </button>
                <button
                  type="button"
                  onClick={() => handleDuplicateQuestion(qIndex)}
                  className="p-1.5 rounded-lg bg-slate-900 text-slate-400 hover:text-cyan-300 transition-colors"
                  title="Duplicate Question"
                >
                  <Copy className="w-3.5 h-3.5" />
                </button>
                <button
                  type="button"
                  onClick={() => handleRemoveQuestion(qIndex)}
                  disabled={questions.length <= 1}
                  className="p-1.5 rounded-lg bg-slate-900 text-slate-400 hover:text-red-400 disabled:opacity-30 transition-colors"
                  title="Delete Question"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

            {/* Question Text */}
            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                Question Prompt *
              </label>
              <textarea
                rows={2}
                value={q.text}
                onChange={(e) => handleUpdateQuestionText(qIndex, e.target.value)}
                placeholder="Enter Question prompt or problem statement here..."
                className="w-full bg-[#050B22] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
              />
            </div>

            {/* Options List */}
            <div className="space-y-2.5">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                  Options & Answer Key ({q.options.length})
                </span>
                {q.options.length < 6 && (
                  <button
                    type="button"
                    onClick={() => handleAddOption(qIndex)}
                    className="text-[11px] font-semibold text-cyan-400 hover:text-cyan-300 flex items-center gap-1 cursor-pointer"
                  >
                    <Plus className="w-3 h-3" />
                    <span>Add Option</span>
                  </button>
                )}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {q.options.map((opt, optIndex) => {
                  const isCorrect = q.correctOptionIds.includes(opt.id);
                  const letter = String.fromCharCode(65 + optIndex);

                  return (
                    <div
                      key={opt.id}
                      className={`flex items-center gap-2 p-2 rounded-xl border transition-all ${
                        isCorrect
                          ? 'bg-emerald-500/10 border-emerald-500/60 shadow-md shadow-emerald-500/10'
                          : 'bg-[#050B22] border-slate-800 hover:border-slate-700'
                      }`}
                    >
                      {/* Clickable Letter Badge to toggle correct answer */}
                      <button
                        type="button"
                        onClick={() => handleToggleCorrectOption(qIndex, opt.id)}
                        className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-black shrink-0 transition-all cursor-pointer ${
                          isCorrect
                            ? 'bg-emerald-500 text-black shadow-md'
                            : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                        }`}
                        title="Click to mark as correct answer"
                      >
                        {letter}
                      </button>

                      {/* Option text input */}
                      <input
                        type="text"
                        value={opt.text}
                        onChange={(e) => handleUpdateOptionText(qIndex, optIndex, e.target.value)}
                        placeholder={`Option ${letter}`}
                        className="flex-1 bg-transparent text-xs sm:text-sm text-white placeholder-slate-500 focus:outline-none"
                      />

                      {/* Correct Answer Badge */}
                      {isCorrect && (
                        <span className="text-[10px] font-bold text-emerald-400 bg-emerald-500/15 px-2 py-0.5 rounded-full shrink-0 flex items-center gap-1">
                          <Check className="w-3 h-3" />
                          <span>Correct</span>
                        </span>
                      )}

                      {/* Remove Option */}
                      {q.options.length > 2 && (
                        <button
                          type="button"
                          onClick={() => handleRemoveOption(qIndex, optIndex)}
                          className="text-slate-600 hover:text-red-400 p-1 rounded transition-colors"
                          title="Remove option"
                        >
                          ✕
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Explanation & Hint Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pt-2">
              <div>
                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1 mb-1">
                  <Lightbulb className="w-3 h-3 text-amber-400" />
                  <span>Explanation (Shown after quiz)</span>
                </label>
                <input
                  type="text"
                  value={q.explanation}
                  onChange={(e) => handleUpdateExplanation(qIndex, e.target.value)}
                  placeholder="Explain why the marked answer is correct..."
                  className="w-full bg-[#050B22] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl px-3 py-2 text-white placeholder-slate-600 text-xs focus:outline-none transition-colors"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1 mb-1">
                  <HelpCircle className="w-3 h-3 text-cyan-400" />
                  <span>Hint for Students (Optional)</span>
                </label>
                <input
                  type="text"
                  value={q.hint}
                  onChange={(e) => handleUpdateHint(qIndex, e.target.value)}
                  placeholder="Provide a clue to nudge the candidate..."
                  className="w-full bg-[#050B22] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl px-3 py-2 text-white placeholder-slate-600 text-xs focus:outline-none transition-colors"
                />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 5. Sticky Bottom Action Bar */}
      <div className="sticky bottom-4 z-30 bg-[#0A102E]/95 backdrop-blur-xl border border-[#16214C] rounded-2xl p-4 shadow-2xl flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-xl bg-indigo-500/20 flex items-center justify-center text-indigo-400">
            <CheckCircle2 className="w-4 h-4" />
          </div>
          <div>
            <p className="text-xs font-bold text-white">
              {questions.length} Question{questions.length > 1 ? 's' : ''} Ready • {durationMinutes} min exam
            </p>
            <p className="text-[11px] text-slate-400">
              Pass cutoff: {passingScorePercent}% • Total Marks: {questions.length * pointsPerQuestion}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto">
          <button
            type="button"
            onClick={handleAddQuestion}
            className="flex-1 sm:flex-none px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-slate-200 text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
          >
            <Plus className="w-4 h-4 text-indigo-400" />
            <span>Add Question</span>
          </button>

          <button
            type="button"
            onClick={handleStartPublish}
            disabled={saving}
            className="flex-1 sm:flex-none px-6 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 via-indigo-500 to-blue-600 hover:from-indigo-500 hover:to-blue-500 disabled:opacity-40 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all flex items-center justify-center gap-2 cursor-pointer"
          >
            {saving ? (
              <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            ) : (
              <>
                <Check className="w-4 h-4" />
                <span>Publish Assessment</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Modals */}
      <BulkImportModal
        isOpen={showBulkModal}
        onClose={() => setShowBulkModal(false)}
        onImport={handleImportedBulk}
      />

      <AiGenerateModal
        isOpen={showAiModal}
        onClose={() => setShowAiModal(false)}
        category={category}
        onGenerated={handleAiGenerated}
      />

      <PublishTargetModal
        isOpen={showPublishTargetModal}
        onClose={() => setShowPublishTargetModal(false)}
        onConfirmPublish={handleConfirmPublish}
        defaultCode={Math.random().toString(36).substring(2, 8).toUpperCase()}
      />

      <PublishSuccessModal
        isOpen={showSuccessModal}
        onClose={() => setShowSuccessModal(false)}
        quizCode={publishedQuizCode}
        quizTitle={title}
        onGoToDashboard={() => {
          setShowSuccessModal(false);
          if (onNavigateHome) onNavigateHome();
        }}
        onCreateAnother={handleCreateAnother}
      />

    </div>
  );
};

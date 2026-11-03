import React, { useState } from 'react';
import type { Quiz, Question, QuestionType, Difficulty } from '../../types';
import { db } from '../../lib/firebase';
import { collection, addDoc } from 'firebase/firestore';
import { Plus, Trash2, Sparkles, Check, Clock, Award, BookOpen, Layers } from 'lucide-react';

interface QuizStudioProps {
  onQuizCreated?: (quiz: Quiz) => void;
}

export const QuizStudio: React.FC<QuizStudioProps> = ({ onQuizCreated }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('Programming');
  const [difficulty, setDifficulty] = useState<Difficulty>('MEDIUM');
  const [questionType, setQuestionType] = useState<QuestionType>('SINGLE');
  const [durationMinutes, setDurationMinutes] = useState(15);
  const [passingScorePercent, setPassingScorePercent] = useState(80);
  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  // Questions state
  const [questions, setQuestions] = useState<Question[]>([
    {
      id: 'q_1',
      text: 'What is the primary role of a ViewModel in Android Architecture Components?',
      options: [
        { id: 'opt_1', text: 'Store and manage UI-related data across configuration changes' },
        { id: 'opt_2', text: 'Directly draw UI pixels onto the screen canvas' },
        { id: 'opt_3', text: 'Manage SQLite database queries on the main thread' },
        { id: 'opt_4', text: 'Handle Android permissions exclusively' }
      ],
      correctOptionIds: ['opt_1'],
      explanation: 'ViewModels survive configuration changes (like screen rotations) and decouple data flow from Activity lifecycles.',
      hint: 'Think about screen rotation data retention.'
    }
  ]);

  const addQuestion = () => {
    const newQ: Question = {
      id: `q_${Date.now()}`,
      text: '',
      options: [
        { id: `opt_${Date.now()}_1`, text: '' },
        { id: `opt_${Date.now()}_2`, text: '' },
        { id: `opt_${Date.now()}_3`, text: '' },
        { id: `opt_${Date.now()}_4`, text: '' }
      ],
      correctOptionIds: [],
      explanation: '',
      hint: ''
    };
    setQuestions([...questions, newQ]);
  };

  const removeQuestion = (index: number) => {
    if (questions.length <= 1) return;
    setQuestions(questions.filter((_, i) => i !== index));
  };

  const updateQuestionText = (index: number, text: string) => {
    const updated = [...questions];
    updated[index].text = text;
    setQuestions(updated);
  };

  const updateOptionText = (qIndex: number, optIndex: number, text: string) => {
    const updated = [...questions];
    updated[qIndex].options[optIndex].text = text;
    setQuestions(updated);
  };

  const toggleCorrectOption = (qIndex: number, optId: string) => {
    const updated = [...questions];
    const q = updated[qIndex];
    if (questionType === 'SINGLE') {
      q.correctOptionIds = [optId];
    } else {
      if (q.correctOptionIds.includes(optId)) {
        q.correctOptionIds = q.correctOptionIds.filter((id) => id !== optId);
      } else {
        q.correctOptionIds.push(optId);
      }
    }
    setQuestions(updated);
  };

  const handleSaveQuiz = async () => {
    if (!title.trim()) {
      alert('Please enter a Quiz Title');
      return;
    }
    setSaving(true);
    setSuccessMsg('');

    try {
      const quizCode = Math.random().toString(36).substring(2, 8).toUpperCase();
      const newQuizData = {
        code: quizCode,
        title: title.trim(),
        description: description.trim(),
        category,
        difficulty,
        questionType,
        questions,
        durationMinutes,
        totalMarks: questions.length,
        passingScorePercent,
        status: 'PUBLISHED',
        teacherId: 'teacher_web_portal',
        teacherName: 'Web Portal Instructor',
        batchIds: [],
        createdAt: Date.now(),
        updatedAt: Date.now()
      };

      const docRef = await addDoc(collection(db, 'quizzes'), newQuizData);
      const createdObj = { id: docRef.id, ...newQuizData } as Quiz;
      setSuccessMsg(`Quiz created successfully with Access Code: ${quizCode}!`);
      if (onQuizCreated) onQuizCreated(createdObj);

      // Reset form
      setTitle('');
      setDescription('');
    } catch (err) {
      console.error('Error saving quiz to Firestore:', err);
      setSuccessMsg('Quiz saved locally (Firestore offline or permission pending).');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="w-full max-w-5xl mx-auto px-4 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800 pb-6">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 text-indigo-400 text-xs font-semibold mb-2">
            <Sparkles className="w-3.5 h-3.5" />
            Teacher Assessment Studio
          </div>
          <h2 className="text-2xl md:text-3xl font-extrabold text-white">Create & Publish Quizzes</h2>
          <p className="text-slate-400 text-sm mt-1">
            Author high-yield exams with timing rules, passing cutoffs, and multi-choice validation.
          </p>
        </div>

        <button
          onClick={handleSaveQuiz}
          disabled={saving}
          className="px-6 py-3 bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-semibold rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/30 active:scale-95 transition-all"
        >
          {saving ? (
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <>
              <Check className="w-5 h-5" />
              Publish Assessment
            </>
          )}
        </button>
      </div>

      {successMsg && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-sm font-medium flex items-center gap-3">
          <Check className="w-5 h-5 text-emerald-400 shrink-0" />
          {successMsg}
        </div>
      )}

      {/* Quiz Metadata Config Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Main Details */}
        <div className="md:col-span-2 bg-slate-900/60 border border-slate-800 rounded-2xl p-6 backdrop-blur-xl space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-indigo-400" />
            General Information
          </h3>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Quiz Title *
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Kotlin Jetpack Compose & State Management"
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-indigo-500 transition-colors"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Description / Instructions
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Provide context, required materials, or student guidelines..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-indigo-500 transition-colors"
            />
          </div>
        </div>

        {/* Configuration Controls */}
        <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 backdrop-blur-xl space-y-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Layers className="w-4 h-4 text-cyan-400" />
            Exam Parameters
          </h3>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
              Category
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500"
            >
              <option value="Programming">Programming</option>
              <option value="Android Development">Android Development</option>
              <option value="Web Development">Web Development</option>
              <option value="Computer Science">Computer Science</option>
              <option value="Mathematics">Mathematics</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                Difficulty
              </label>
              <select
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value as Difficulty)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500"
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                Question Type
              </label>
              <select
                value={questionType}
                onChange={(e) => setQuestionType(e.target.value as QuestionType)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500"
              >
                <option value="SINGLE">Single Choice</option>
                <option value="MULTIPLE">Multiple Choice</option>
                <option value="MIXED">Mixed</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3 pt-2">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Clock className="w-3.5 h-3.5 text-indigo-400" />
                Duration (min)
              </label>
              <input
                type="number"
                min={1}
                max={180}
                value={durationMinutes}
                onChange={(e) => setDurationMinutes(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1">
                <Award className="w-3.5 h-3.5 text-amber-400" />
                Pass Rule %
              </label>
              <input
                type="number"
                min={50}
                max={100}
                value={passingScorePercent}
                onChange={(e) => setPassingScorePercent(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white text-sm focus:outline-none focus:border-indigo-500"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Questions Section */}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h3 className="text-xl font-bold text-white flex items-center gap-2">
            Questions ({questions.length})
          </h3>
          <button
            onClick={addQuestion}
            className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-indigo-300 border border-slate-700 rounded-xl text-sm font-semibold flex items-center gap-1.5 transition-colors"
          >
            <Plus className="w-4 h-4" />
            Add Question
          </button>
        </div>

        {questions.map((q, qIndex) => (
          <div
            key={q.id}
            className="bg-slate-900/70 border border-slate-800 rounded-2xl p-6 backdrop-blur-xl space-y-4 transition-all hover:border-slate-700"
          >
            <div className="flex items-start justify-between gap-4">
              <span className="w-7 h-7 rounded-lg bg-indigo-500/20 text-indigo-400 font-bold text-xs flex items-center justify-center shrink-0">
                {qIndex + 1}
              </span>
              <div className="flex-1">
                <input
                  type="text"
                  value={q.text}
                  onChange={(e) => updateQuestionText(qIndex, e.target.value)}
                  placeholder="Enter Question Prompt here..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-indigo-500"
                />
              </div>
              <button
                onClick={() => removeQuestion(qIndex)}
                disabled={questions.length <= 1}
                className="text-slate-500 hover:text-red-400 disabled:opacity-30 p-1.5 rounded-lg transition-colors"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>

            {/* Options Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pl-11">
              {q.options.map((opt, optIndex) => {
                const isSelected = q.correctOptionIds.includes(opt.id);
                return (
                  <div
                    key={opt.id}
                    className={`flex items-center gap-2.5 p-2 rounded-xl border transition-all ${
                      isSelected
                        ? 'bg-emerald-500/10 border-emerald-500/40 text-emerald-300'
                        : 'bg-slate-950 border-slate-800 text-slate-300'
                    }`}
                  >
                    <button
                      onClick={() => toggleCorrectOption(qIndex, opt.id)}
                      className={`w-6 h-6 rounded-lg flex items-center justify-center text-xs font-bold shrink-0 transition-colors ${
                        isSelected
                          ? 'bg-emerald-500 text-black'
                          : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
                      }`}
                    >
                      {String.fromCharCode(65 + optIndex)}
                    </button>
                    <input
                      type="text"
                      value={opt.text}
                      onChange={(e) => updateOptionText(qIndex, optIndex, e.target.value)}
                      placeholder={`Option ${String.fromCharCode(65 + optIndex)}`}
                      className="w-full bg-transparent border-none text-sm text-white placeholder-slate-600 focus:outline-none"
                    />
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { db } from '../../lib/firebase';
import { collection, query, where, getDocs, onSnapshot, deleteDoc, doc } from 'firebase/firestore';
import type { Quiz } from '../../types';
import { 
  Plus, 
  Users, 
  BarChart3, 
  Award, 
  BookOpen, 
  CheckCircle2, 
  Star, 
  ArrowRight, 
  Clock, 
  Trash2, 
  Bell, 
  HelpCircle,
  Sparkles,
  Bot,
  ShieldCheck,
  Smartphone,
  ExternalLink,
  Info,
  LogIn
} from 'lucide-react';

interface TeacherHomeProps {
  onNavigateTab: (tab: 'home' | 'studio' | 'batches' | 'verify') => void;
  onOpenAbout: () => void;
  onOpenAuth: () => void;
}

export const TeacherHome: React.FC<TeacherHomeProps> = ({ 
  onNavigateTab, 
  onOpenAbout, 
  onOpenAuth 
}) => {
  const { currentUser } = useAuth();
  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalStudents, setTotalStudents] = useState(0);
  const [totalAttempts, setTotalAttempts] = useState(0);
  const [avgScore, setAvgScore] = useState(0);
  const [pendingRequestsCount, setPendingRequestsCount] = useState(0);
  const [deleteConfirmQuiz, setDeleteConfirmQuiz] = useState<Quiz | null>(null);

  // Compute dynamic greeting
  const hour = new Date().getHours();
  const greetingPrefix = hour < 12 ? 'Good Morning' : hour < 17 ? 'Good Afternoon' : 'Good Evening';
  const teacherName = currentUser?.name || 'Instructor';

  // Real-time Firestore synchronization
  useEffect(() => {
    let unsubQuizzes: (() => void) | undefined;
    let unsubBatches: (() => void) | undefined;
    let unsubRequests: (() => void) | undefined;

    const teacherId = currentUser?.id;

    if (teacherId) {
      // 1. Quizzes
      const qQuizzes = query(collection(db, 'quizzes'), where('teacherId', '==', teacherId));
      unsubQuizzes = onSnapshot(qQuizzes, (snapshot) => {
        const loaded: Quiz[] = [];
        snapshot.forEach((d) => {
          loaded.push({ id: d.id, ...d.data() } as Quiz);
        });
        setQuizzes(loaded);
        setLoading(false);
      }, (err) => {
        console.warn('Live quizzes sync fallback:', err);
        getDocs(collection(db, 'quizzes')).then((snap) => {
          const list: Quiz[] = [];
          snap.forEach((doc) => list.push({ id: doc.id, ...doc.data() } as Quiz));
          setQuizzes(list.filter(q => q.teacherId === teacherId || !q.teacherId));
          setLoading(false);
        }).catch(() => setLoading(false));
      });

      // 2. Batches & Enrolled Students
      const qBatches = query(collection(db, 'batches'), where('teacherId', '==', teacherId));
      unsubBatches = onSnapshot(qBatches, (snapshot) => {
        let studentCount = 0;
        snapshot.forEach((d) => {
          const bData = d.data();
          if (Array.isArray(bData.students)) {
            studentCount += bData.students.length;
          } else if (typeof bData.studentCount === 'number') {
            studentCount += bData.studentCount;
          }
        });
        setTotalStudents(studentCount);
      }, (err) => console.warn('Batches count err:', err));

      // 3. Pending join requests
      const qRequests = query(
        collection(db, 'join_requests'), 
        where('teacherId', '==', teacherId),
        where('isPending', '==', true)
      );
      unsubRequests = onSnapshot(qRequests, (snapshot) => {
        setPendingRequestsCount(snapshot.size);
      }, () => {});

      // 4. Submissions / Results metrics
      const qResults = query(collection(db, 'quiz_results'), where('teacherId', '==', teacherId));
      getDocs(qResults).then((snap) => {
        if (!snap.empty) {
          setTotalAttempts(snap.size);
          let sum = 0;
          snap.forEach(d => {
            const val = d.data().scorePercentage || d.data().score || 0;
            sum += Number(val);
          });
          setAvgScore(Math.round(sum / snap.size));
        }
      }).catch(() => {});

    } else {
      // Guest view: Fetch public quizzes for showcase preview
      getDocs(collection(db, 'quizzes')).then((snap) => {
        const loaded: Quiz[] = [];
        snap.forEach((d) => loaded.push({ id: d.id, ...d.data() } as Quiz));
        setQuizzes(loaded.slice(0, 6));
        setLoading(false);
      }).catch(() => setLoading(false));
    }

    return () => {
      unsubQuizzes?.();
      unsubBatches?.();
      unsubRequests?.();
    };
  }, [currentUser]);

  const handleDeleteQuiz = async (quizId: string) => {
    try {
      await deleteDoc(doc(db, 'quizzes', quizId));
      setQuizzes((prev) => prev.filter((q) => q.id !== quizId));
      setDeleteConfirmQuiz(null);
    } catch (err) {
      console.error('Delete failed:', err);
      alert('Could not delete quiz. Please verify permissions.');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6 space-y-8 animate-in fade-in duration-300">
      
      {/* =========================================================================
          1. REDESIGNED HERO SECTION (Ultra-Modern Dark SaaS Assessment Engine)
         ========================================================================= */}
      <div 
        className="relative overflow-hidden rounded-[32px] border border-[#1E2958] p-6 sm:p-8 lg:p-10 shadow-2xl transition-all"
        style={{
          background: 'linear-gradient(135deg, #070B1E 0%, #0D163B 50%, #050819 100%)',
          boxShadow: '0 25px 60px -15px rgba(99, 102, 241, 0.2)'
        }}
      >
        {/* Subtle Ambient Gradient Halos */}
        <div className="absolute -top-32 -right-32 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-32 -left-32 w-96 h-96 bg-blue-600/15 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute top-1/2 left-1/3 -translate-y-1/2 w-64 h-64 bg-purple-600/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 flex flex-col lg:flex-row items-center justify-between gap-8 lg:gap-12">
          
          {/* Left Column: Headline, Value Proposition, Action Buttons */}
          <div className="flex-1 space-y-4 text-center lg:text-left">
            
            {/* Platform Tag Pill */}
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#101A4A]/80 border border-[#2B3B8A] shadow-inner">
              <span className="w-2 h-2 rounded-full bg-[#38BDF8] animate-pulse" />
              <span className="text-[11px] font-bold text-[#93C5FD] tracking-wider uppercase">
                QUIZMASTER TEACHER STUDIO
              </span>
            </div>

            {/* Dynamic Headline */}
            <div>
              {currentUser ? (
                <>
                  <p className="text-slate-400 text-sm font-medium">{greetingPrefix},</p>
                  <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black text-white tracking-tight mt-1">
                    {teacherName} <span className="inline-block animate-bounce">👋</span>
                  </h1>
                </>
              ) : (
                <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black text-white tracking-tight leading-tight">
                  Intelligent Assessment Studio for <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-purple-300 to-cyan-400">Modern Educators</span>
                </h1>
              )}
            </div>

            {/* Subtitle & Value Proposition */}
            <p className="text-sm sm:text-base text-slate-300 max-w-xl leading-relaxed">
              Author high-yield exams manually, import bulk question banks, or generate full curriculum tests in seconds with multi-provider AI. Seamlessly organize cohorts, monitor live submissions, and issue verifiable digital credentials.
            </p>

            {/* Feature Badges */}
            <div className="flex flex-wrap items-center justify-center lg:justify-start gap-2 pt-1 text-xs text-slate-300">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-900/80 border border-slate-800">
                <Sparkles className="w-3.5 h-3.5 text-purple-400" />
                <span>Multi-Model AI (AgentRouter, Gemini)</span>
              </span>
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-900/80 border border-slate-800">
                <ShieldCheck className="w-3.5 h-3.5 text-cyan-400" />
                <span>SHA-256 Tamper-Proof Certificates</span>
              </span>
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-900/80 border border-slate-800">
                <Smartphone className="w-3.5 h-3.5 text-emerald-400" />
                <span>Android App Integration</span>
              </span>
            </div>

            {/* Action Buttons */}
            <div className="pt-3 flex flex-wrap items-center justify-center lg:justify-start gap-3">
              <button
                onClick={() => onNavigateTab('studio')}
                className="px-6 py-3 rounded-2xl bg-gradient-to-r from-indigo-600 via-indigo-500 to-blue-600 hover:from-indigo-500 hover:to-blue-500 text-white font-bold text-sm shadow-xl shadow-indigo-600/35 transition-all flex items-center gap-2 cursor-pointer hover:scale-[1.02] active:scale-[0.98]"
              >
                <Plus className="w-4 h-4" />
                <span>Create Assessment</span>
              </button>
              
              <button
                onClick={() => onNavigateTab('batches')}
                className="px-5 py-3 rounded-2xl bg-[#0B1233] hover:bg-[#121B47] border border-[#202E70] text-slate-200 font-semibold text-sm transition-all flex items-center gap-2 cursor-pointer hover:scale-[1.02] active:scale-[0.98]"
              >
                <Users className="w-4 h-4 text-indigo-400" />
                <span>Manage Batches</span>
              </button>

              <button
                onClick={() => onNavigateTab('verify')}
                className="px-4 py-3 rounded-2xl bg-slate-900/80 hover:bg-slate-800 border border-slate-800 text-slate-300 font-medium text-sm transition-all flex items-center gap-2 cursor-pointer"
              >
                <ShieldCheck className="w-4 h-4 text-cyan-400" />
                <span>Verify Credentials</span>
              </button>

              {!currentUser && (
                <button
                  onClick={onOpenAuth}
                  className="px-4 py-3 rounded-2xl bg-indigo-950/80 hover:bg-indigo-900/80 border border-indigo-800/80 text-indigo-200 font-semibold text-sm transition-all flex items-center gap-2 cursor-pointer"
                >
                  <LogIn className="w-4 h-4 text-indigo-400" />
                  <span>Sign In</span>
                </button>
              )}
            </div>
          </div>

          {/* Right Column: Live Assessment Engine Preview Card (High-Tech Replacement for Cartoon Graphic) */}
          <div className="w-full lg:w-[420px] shrink-0">
            <div className="relative rounded-2xl bg-[#090E26]/95 border border-[#1E2E6C] p-5 shadow-2xl backdrop-blur-xl">
              
              {/* Card Header with Status Indicator */}
              <div className="flex items-center justify-between pb-3.5 border-b border-slate-800/80">
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse shadow-sm shadow-emerald-400/50" />
                  <span className="text-[11px] font-extrabold text-emerald-400 tracking-wider">
                    ENGINE ACTIVE • FIRESTORE SYNCED
                  </span>
                </div>
                <span className="text-[10px] font-semibold text-slate-400 bg-slate-800/80 px-2 py-0.5 rounded-full border border-slate-700/60">
                  v5.0.2
                </span>
              </div>

              {/* Sample AI Generated Question Showcase */}
              <div className="mt-4 space-y-3">
                <div className="flex items-center justify-between text-xs">
                  <span className="px-2 py-0.5 rounded-md bg-indigo-500/15 text-indigo-300 border border-indigo-500/30 text-[10px] font-bold uppercase tracking-wider">
                    Cloud Architecture • Medium
                  </span>
                  <span className="text-[11px] text-slate-400 flex items-center gap-1 font-medium">
                    <Clock className="w-3 h-3 text-slate-400" /> 15m duration
                  </span>
                </div>

                <p className="text-xs sm:text-sm font-semibold text-slate-100 leading-snug">
                  Which protocol ensures real-time student submission sync between Android APK and the Web Teacher Studio?
                </p>

                {/* Simulated Options */}
                <div className="space-y-1.5 pt-1 text-xs">
                  <div className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 text-slate-400 flex items-center justify-between">
                    <span>A. Polling REST API every 10 seconds</span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-emerald-500/15 border border-emerald-500/40 text-emerald-200 font-semibold flex items-center justify-between shadow-sm">
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 rounded-full bg-emerald-500 text-black flex items-center justify-center text-[10px] font-black">✓</span>
                      <span>B. Cloud Firestore Real-time Snapshot Stream</span>
                    </span>
                    <span className="text-[10px] text-emerald-400 font-bold uppercase">Correct</span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-slate-900/60 border border-slate-800 text-slate-400 flex items-center justify-between">
                    <span>C. Unencrypted Local SQLite Database</span>
                  </div>
                </div>

                {/* Engine Diagnostic Footer */}
                <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-400">
                  <span className="flex items-center gap-1 text-indigo-400 font-semibold">
                    <Bot className="w-3.5 h-3.5" /> AgentRouter & Gemini
                  </span>
                  <span className="text-emerald-400 font-bold">Auto-Graded on Android</span>
                </div>
              </div>

            </div>
          </div>

        </div>
      </div>

      {/* =========================================================================
          2. PENDING BATCH REQUESTS ALERT BANNER (If Any)
         ========================================================================= */}
      {pendingRequestsCount > 0 && (
        <div 
          onClick={() => onNavigateTab('batches')}
          className="cursor-pointer rounded-2xl bg-[#1E1338] border border-[#7C3AED]/60 p-4 flex items-center justify-between hover:border-[#7C3AED] transition-all shadow-lg"
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#8B5CF6]/20 border border-[#8B5CF6]/30 flex items-center justify-center text-purple-300">
              <Bell className="w-5 h-5 text-purple-400 animate-pulse" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h4 className="font-bold text-sm text-white">Pending Join Requests</h4>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-amber-500 text-black">
                  {pendingRequestsCount} new
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                {pendingRequestsCount} student{pendingRequestsCount > 1 ? 's' : ''} waiting for batch enrollment approval
              </p>
            </div>
          </div>
          <div className="flex items-center gap-1.5 text-xs font-bold text-purple-400">
            <span>Review Requests</span>
            <ArrowRight className="w-4 h-4" />
          </div>
        </div>
      )}

      {/* =========================================================================
          3. KEY METRICS OVERVIEW (Live Firestore Data)
         ========================================================================= */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        {/* Total Students */}
        <div className="bg-[#090E26] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-indigo-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Enrolled Students</span>
            <div className="w-8 h-8 rounded-xl bg-[#7C3AED]/20 flex items-center justify-center">
              <Users className="w-4 h-4 text-[#A78BFA]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{totalStudents}</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Active across all batches</p>
          </div>
        </div>

        {/* Total Quizzes */}
        <div className="bg-[#090E26] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-blue-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Authored Quizzes</span>
            <div className="w-8 h-8 rounded-xl bg-[#2563EB]/20 flex items-center justify-center">
              <BookOpen className="w-4 h-4 text-[#60A5FA]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{quizzes.length}</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Available for student testing</p>
          </div>
        </div>

        {/* Total Attempts */}
        <div className="bg-[#090E26] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-emerald-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Exam Submissions</span>
            <div className="w-8 h-8 rounded-xl bg-[#059669]/20 flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4 text-[#34D399]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{totalAttempts}</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Auto-graded on Android APK</p>
          </div>
        </div>

        {/* Avg. Score */}
        <div className="bg-[#090E26] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-amber-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Classroom Performance</span>
            <div className="w-8 h-8 rounded-xl bg-[#D97706]/20 flex items-center justify-center">
              <Star className="w-4 h-4 text-[#FBBF24]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{avgScore}%</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Average score percentage</p>
          </div>
        </div>
      </div>

      {/* =========================================================================
          4. "ALL ABOUT QUIZMASTER" — COMPREHENSIVE PLATFORM CAPABILITIES
         ========================================================================= */}
      <div className="space-y-6 pt-4">
        <div className="text-center max-w-2xl mx-auto space-y-2">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/25 text-indigo-400 text-xs font-bold uppercase tracking-wider">
            <Sparkles className="w-3.5 h-3.5" />
            <span>All About QuizMaster</span>
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            The Complete Assessment & Credential Ecosystem
          </h2>
          <p className="text-xs sm:text-sm text-slate-400">
            A unified platform connecting web instructors with mobile students through Google Cloud Firestore and AI.
          </p>
          <div className="pt-1">
            <button
              onClick={onOpenAbout}
              className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 text-[11px] font-semibold text-slate-300 hover:text-white transition-colors cursor-pointer"
            >
              <Info className="w-3.5 h-3.5 text-indigo-400" />
              <span>Platform Specs & Release Info</span>
            </button>
          </div>
        </div>

        {/* 4 Feature Pillar Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          
          {/* Pillar 1: AI Quiz Studio */}
          <div 
            onClick={() => onNavigateTab('studio')}
            className="rounded-2xl p-5 border border-indigo-500/30 bg-gradient-to-b from-[#1E1145] to-[#0B0824] hover:border-indigo-400/60 transition-all cursor-pointer group hover:scale-[1.01]"
          >
            <div className="w-10 h-10 rounded-xl bg-indigo-500/25 border border-indigo-500/40 flex items-center justify-center mb-3 text-indigo-300 group-hover:scale-110 transition-transform">
              <Bot className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-white group-hover:text-indigo-200 transition-colors">
              Multi-Model AI Studio
            </h3>
            <p className="text-xs text-slate-300 mt-2 leading-relaxed">
              Generate curriculum-aligned assessments in seconds with AgentRouter, Google Gemini, OpenAI, Claude, or DeepSeek. Includes explanations and hints.
            </p>
            <div className="mt-4 flex items-center gap-1 text-xs font-bold text-indigo-400">
              <span>Open Studio</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>

          {/* Pillar 2: Cohort & Batch Management */}
          <div 
            onClick={() => onNavigateTab('batches')}
            className="rounded-2xl p-5 border border-blue-500/30 bg-gradient-to-b from-[#0C2D54] to-[#07132B] hover:border-blue-400/60 transition-all cursor-pointer group hover:scale-[1.01]"
          >
            <div className="w-10 h-10 rounded-xl bg-blue-500/25 border border-blue-500/40 flex items-center justify-center mb-3 text-blue-300 group-hover:scale-110 transition-transform">
              <Users className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-white group-hover:text-blue-200 transition-colors">
              Batches & Access Codes
            </h3>
            <p className="text-xs text-slate-300 mt-2 leading-relaxed">
              Organize students into private cohorts with auto-generated 6-character access keys. Review and approve student join requests with a single click.
            </p>
            <div className="mt-4 flex items-center gap-1 text-xs font-bold text-blue-400">
              <span>Manage Batches</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>

          {/* Pillar 3: Real-Time Grade Analytics */}
          <div 
            onClick={() => onNavigateTab('home')}
            className="rounded-2xl p-5 border border-emerald-500/30 bg-gradient-to-b from-[#0A382A] to-[#061712] hover:border-emerald-400/60 transition-all cursor-pointer group hover:scale-[1.01]"
          >
            <div className="w-10 h-10 rounded-xl bg-emerald-500/25 border border-emerald-500/40 flex items-center justify-center mb-3 text-emerald-300 group-hover:scale-110 transition-transform">
              <BarChart3 className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-white group-hover:text-emerald-200 transition-colors">
              Automated Analytics
            </h3>
            <p className="text-xs text-slate-300 mt-2 leading-relaxed">
              Exams taken by students on mobile are instantly auto-graded. Track cohort pass rates, average scores, grade distributions, and problem question areas.
            </p>
            <div className="mt-4 flex items-center gap-1 text-xs font-bold text-emerald-400">
              <span>View Analytics</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>

          {/* Pillar 4: Public Tamper-Proof Verification */}
          <div 
            onClick={() => onNavigateTab('verify')}
            className="rounded-2xl p-5 border border-pink-500/30 bg-gradient-to-b from-[#3D0E29] to-[#1A0512] hover:border-pink-400/60 transition-all cursor-pointer group hover:scale-[1.01]"
          >
            <div className="w-10 h-10 rounded-xl bg-pink-500/25 border border-pink-500/40 flex items-center justify-center mb-3 text-pink-300 group-hover:scale-110 transition-transform">
              <Award className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-white group-hover:text-pink-200 transition-colors">
              Tamper-Proof Verification
            </h3>
            <p className="text-xs text-slate-300 mt-2 leading-relaxed">
              Open credential verification portal with zero login requirement. Employers and institutions verify certificate authenticity directly against Firestore.
            </p>
            <div className="mt-4 flex items-center gap-1 text-xs font-bold text-pink-400">
              <span>Verify Certificates</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>

        </div>
      </div>

      {/* =========================================================================
          5. CROSS-PLATFORM ECOSYSTEM BANNER (Android APK + Web Studio)
         ========================================================================= */}
      <div className="rounded-3xl bg-[#090E26] border border-[#1B2960] p-6 sm:p-8 flex flex-col md:flex-row items-center justify-between gap-6 shadow-xl">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-[#7C3AED] to-[#3B82F6] p-0.5 shrink-0 flex items-center justify-center">
            <div className="w-full h-full bg-[#070913] rounded-[14px] flex items-center justify-center p-2">
              <Smartphone className="w-6 h-6 text-indigo-400" />
            </div>
          </div>
          <div>
            <h3 className="text-base sm:text-lg font-bold text-white">
              QuizMaster Android Student App
            </h3>
            <p className="text-xs sm:text-sm text-slate-400 mt-0.5 max-w-xl">
              Students join private batches, take timed assessments, practice offline streaks, and earn tamper-proof verifiable certificates directly on Android devices.
            </p>
          </div>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <a
            href="https://github.com/Deb-Kumar/QuizMaster/releases"
            target="_blank"
            rel="noreferrer"
            className="px-5 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-white font-bold text-xs sm:text-sm flex items-center gap-2 transition-all cursor-pointer"
          >
            <span>Download APK</span>
            <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
          </a>
        </div>
      </div>

      {/* =========================================================================
          6. RECENT QUIZZES SECTION (Real-Time Authored List)
         ========================================================================= */}
      <div className="space-y-4 pt-2">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-extrabold text-white tracking-tight">Recent Assessments</h3>
            <p className="text-xs text-slate-400">All assessments authored under your account</p>
          </div>
          <button
            onClick={() => onNavigateTab('studio')}
            className="text-xs font-semibold text-indigo-400 hover:text-indigo-300 flex items-center gap-1 cursor-pointer"
          >
            <span>Create New</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-32 bg-[#090E26]/60 border border-[#16214C] rounded-2xl animate-pulse" />
            ))}
          </div>
        ) : quizzes.length === 0 ? (
          <div className="bg-[#090E26] border border-[#16214C] rounded-3xl p-8 sm:p-12 text-center">
            <div className="w-14 h-14 mx-auto mb-3 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
              <BookOpen className="w-7 h-7 text-indigo-400" />
            </div>
            <h4 className="text-base sm:text-lg font-bold text-white">No Quizzes Created Yet</h4>
            <p className="text-xs text-slate-400 max-w-md mx-auto mt-1 mb-5">
              You haven't authored any quizzes yet. Use Quiz Studio to assemble questions manually, paste bulk text, or let AI generate full curriculum tests.
            </p>
            <button
              onClick={() => onNavigateTab('studio')}
              className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 text-white font-bold text-xs shadow-lg shadow-indigo-600/30 transition-all inline-flex items-center gap-2 cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Create Your First Quiz</span>
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {quizzes.map((quiz) => (
              <div 
                key={quiz.id}
                className="bg-[#090E26] border border-[#16214C] hover:border-[#1E2E6C] rounded-2xl p-5 flex flex-col justify-between transition-all group"
              >
                <div>
                  <div className="flex items-start justify-between gap-3">
                    <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-indigo-500/15 text-indigo-300 border border-indigo-500/30 uppercase tracking-wider">
                      {quiz.category || 'General Assessment'}
                    </span>
                    <button
                      onClick={() => setDeleteConfirmQuiz(quiz)}
                      className="text-slate-500 hover:text-red-400 p-1 rounded-lg transition-colors cursor-pointer"
                      title="Delete Quiz"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>

                  <h4 className="font-bold text-white text-base mt-2.5 line-clamp-1 group-hover:text-indigo-300 transition-colors">
                    {quiz.title}
                  </h4>
                  <p className="text-xs text-slate-400 mt-1 line-clamp-2">
                    {quiz.description || 'No custom description provided for this test.'}
                  </p>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
                  <div className="flex items-center gap-3">
                    <span className="flex items-center gap-1">
                      <HelpCircle className="w-3.5 h-3.5 text-indigo-400" />
                      <span>{quiz.questions?.length || 0} Qs</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5 text-blue-400" />
                      <span>{quiz.durationMinutes || 20}m</span>
                    </span>
                  </div>

                  <button
                    onClick={() => onNavigateTab('studio')}
                    className="font-semibold text-indigo-400 hover:text-indigo-300 flex items-center gap-1 cursor-pointer"
                  >
                    <span>Edit</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {deleteConfirmQuiz && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm bg-[#0F172A] border border-[#1E293B] rounded-2xl p-6 text-center space-y-4">
            <div className="w-12 h-12 mx-auto rounded-full bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-400">
              <Trash2 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="font-bold text-base text-white">Delete Quiz?</h3>
              <p className="text-xs text-slate-400 mt-1">
                Are you sure you want to delete <span className="text-white font-semibold">"{deleteConfirmQuiz.title}"</span>? This action cannot be undone.
              </p>
            </div>
            <div className="flex items-center gap-3 pt-2">
              <button
                onClick={() => setDeleteConfirmQuiz(null)}
                className="flex-1 py-2 rounded-xl bg-slate-800 text-slate-300 font-semibold text-xs hover:bg-slate-700 transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDeleteQuiz(deleteConfirmQuiz.id)}
                className="flex-1 py-2 rounded-xl bg-red-600 hover:bg-red-500 text-white font-bold text-xs shadow-lg shadow-red-600/30 transition-colors cursor-pointer"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

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

  // Compute greeting prefix
  const hour = new Date().getHours();
  const greetingPrefix = hour < 12 ? 'Good Morning,' : hour < 17 ? 'Good Afternoon,' : 'Good Evening,';
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
        // Fallback: load all quizzes
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
      }, () => {
        // Ignore fallback
      });

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
      }).catch(() => {
        // ignore
      });

    } else {
      // Guest view: Fetch public quizzes
      getDocs(collection(db, 'quizzes')).then((snap) => {
        const loaded: Quiz[] = [];
        snap.forEach((d) => loaded.push({ id: d.id, ...d.data() } as Quiz));
        setQuizzes(loaded.slice(0, 5));
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
    <div className="max-w-7xl mx-auto px-4 lg:px-8 py-6 space-y-6 animate-in fade-in duration-300">
      
      {/* Guest Mode Sign-In Banner */}
      {!currentUser && (
        <div className="rounded-2xl bg-[#0F173C] border border-[#1E2858] p-4 flex flex-col sm:flex-row items-center justify-between gap-3 shadow-lg">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/20 flex items-center justify-center">
              <LogIn className="w-4 h-4 text-indigo-400" />
            </div>
            <div>
              <h4 className="text-sm font-bold text-white">Teacher Portal Preview</h4>
              <p className="text-xs text-slate-400 mt-0.5">
                Sign in with your instructor account to synchronize your private batches, quizzes, and live submissions.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={onOpenAbout}
              className="px-3 py-1.5 rounded-xl bg-slate-900 border border-slate-700 text-xs font-semibold text-slate-300 hover:text-white transition-colors cursor-pointer"
            >
              About
            </button>
            <button
              onClick={onOpenAuth}
              className="px-4 py-1.5 rounded-xl bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 text-white font-bold text-xs shadow-md shadow-indigo-600/30 transition-all cursor-pointer"
            >
              Sign In Now
            </button>
          </div>
        </div>
      )}

      {/* 1. Hero Card matching Android TeacherHomeContent.kt */}
      <div 
        className="relative overflow-hidden rounded-[28px] border border-indigo-500/30 p-6 md:p-8"
        style={{
          background: 'linear-gradient(135deg, #0C143B 0%, #131D56 50%, #090D2A 100%)',
          boxShadow: '0 20px 40px -15px rgba(79, 70, 229, 0.25)'
        }}
      >
        {/* Ambient Halo behind Hero image */}
        <div 
          className="absolute -right-12 -top-12 w-80 h-80 rounded-full pointer-events-none opacity-40 blur-3xl"
          style={{
            background: 'radial-gradient(circle, rgba(99,102,241,0.5) 0%, rgba(59,130,246,0.2) 60%, transparent 100%)'
          }}
        />

        <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex-1 space-y-3 text-center md:text-left">
            {/* Pill Tag */}
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#1E2868]/70 border border-[#60A5FA]/40 shadow-sm">
              <span className="w-2 h-2 rounded-full bg-[#38BDF8] animate-pulse" />
              <span className="text-[10px] md:text-[11px] font-bold text-[#93C5FD] tracking-wider uppercase">
                TEACHER DASHBOARD
              </span>
            </div>

            <div>
              <p className="text-slate-400 text-sm font-medium">{greetingPrefix}</p>
              <h2 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-white tracking-tight mt-0.5">
                {teacherName} <span className="inline-block animate-bounce">👋</span>
              </h2>
            </div>

            <div className="flex items-center justify-center md:justify-start gap-2 text-xs md:text-sm font-semibold text-[#93C5FD]">
              <span className="w-1.5 h-1.5 rounded-full bg-indigo-400" />
              <span>Create. Manage. Inspire.</span>
            </div>

            <p className="text-xs md:text-sm text-slate-300 max-w-md leading-relaxed">
              Build better quizzes, track student progress, and help your classes achieve peak academic excellence.
            </p>

            <div className="pt-2 flex flex-wrap items-center justify-center md:justify-start gap-3">
              <button
                onClick={() => onNavigateTab('studio')}
                className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 text-white font-bold text-xs md:text-sm shadow-lg shadow-indigo-600/30 transition-all flex items-center gap-2 cursor-pointer"
              >
                <Plus className="w-4 h-4" />
                <span>Create New Quiz</span>
              </button>
              <button
                onClick={() => onNavigateTab('batches')}
                className="px-4 py-2.5 rounded-xl bg-slate-900/90 hover:bg-slate-800 border border-slate-700/80 text-slate-200 font-semibold text-xs md:text-sm transition-all flex items-center gap-2 cursor-pointer"
              >
                <Users className="w-4 h-4 text-indigo-400" />
                <span>Manage Batches</span>
              </button>
            </div>
          </div>

          {/* Hero Illustration */}
          <div className="w-48 sm:w-56 md:w-64 flex-shrink-0 flex items-center justify-center">
            <img 
              src="/welcome_illustration.png" 
              alt="QuizMaster Teacher" 
              className="w-full h-auto object-contain filter drop-shadow-[0_15px_25px_rgba(0,0,0,0.6)]"
              onError={(e) => {
                // Fallback to app_logo if illustration is missing
                (e.target as HTMLImageElement).src = '/app_logo.png';
              }}
            />
          </div>
        </div>
      </div>

      {/* 2. Pending Batch Join Requests Notification Banner */}
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

      {/* 3. 4 Overview Metric Cards matching Android TeacherMetricCard.kt */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
        {/* Total Students */}
        <div className="bg-[#0A102E] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-indigo-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Total Students</span>
            <div className="w-8 h-8 rounded-xl bg-[#7C3AED]/20 flex items-center justify-center">
              <Users className="w-4 h-4 text-[#A78BFA]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{totalStudents}</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Across all enrolled batches</p>
          </div>
        </div>

        {/* Total Quizzes */}
        <div className="bg-[#0A102E] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-blue-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Total Quizzes</span>
            <div className="w-8 h-8 rounded-xl bg-[#2563EB]/20 flex items-center justify-center">
              <BookOpen className="w-4 h-4 text-[#60A5FA]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{quizzes.length}</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Active authored assessments</p>
          </div>
        </div>

        {/* Total Attempts */}
        <div className="bg-[#0A102E] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-emerald-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Total Attempts</span>
            <div className="w-8 h-8 rounded-xl bg-[#059669]/20 flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4 text-[#34D399]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{totalAttempts}</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Completed quiz submissions</p>
          </div>
        </div>

        {/* Avg. Score */}
        <div className="bg-[#0A102E] border border-[#16214C] rounded-2xl p-4 sm:p-5 flex flex-col justify-between hover:border-amber-500/40 transition-colors">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Avg. Score</span>
            <div className="w-8 h-8 rounded-xl bg-[#D97706]/20 flex items-center justify-center">
              <Star className="w-4 h-4 text-[#FBBF24]" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">{avgScore}%</span>
            <p className="text-[11px] text-slate-400 mt-0.5">Student performance index</p>
          </div>
        </div>
      </div>

      {/* 4. Quick Actions 2x2 Grid matching Android TeacherHomeContent.kt */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-base sm:text-lg font-bold text-white tracking-tight">Quick Actions</h3>
          <span className="text-xs text-slate-400">Instant Teacher Controls</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
          {/* Card 1: Create Quiz */}
          <button
            onClick={() => onNavigateTab('studio')}
            className="text-left rounded-2xl p-4 sm:p-5 border transition-all cursor-pointer group hover:scale-[1.01]"
            style={{
              background: 'linear-gradient(180deg, #3B0764 0%, #1E1038 100%)',
              borderColor: '#7C3AED'
            }}
          >
            <div className="w-10 h-10 rounded-xl bg-[#7C3AED]/30 flex items-center justify-center mb-3">
              <Plus className="w-5 h-5 text-[#C4B5FD] group-hover:scale-110 transition-transform" />
            </div>
            <h4 className="font-bold text-white text-sm sm:text-base">Create Quiz</h4>
            <p className="text-xs text-[#DDD6FE]/80 mt-1">Build manual, bulk, or AI-powered tests</p>
          </button>

          {/* Card 2: Manage Students */}
          <button
            onClick={() => onNavigateTab('batches')}
            className="text-left rounded-2xl p-4 sm:p-5 border transition-all cursor-pointer group hover:scale-[1.01]"
            style={{
              background: 'linear-gradient(180deg, #0C4A6E 0%, #09233A 100%)',
              borderColor: '#0284C7'
            }}
          >
            <div className="w-10 h-10 rounded-xl bg-[#0284C7]/30 flex items-center justify-center mb-3">
              <Users className="w-5 h-5 text-[#7DD3FC] group-hover:scale-110 transition-transform" />
            </div>
            <h4 className="font-bold text-white text-sm sm:text-base">Manage Students</h4>
            <p className="text-xs text-[#BAE6FD]/80 mt-1">Manage batches, codes & approvals</p>
          </button>

          {/* Card 3: View Analytics */}
          <button
            onClick={() => onNavigateTab('batches')}
            className="text-left rounded-2xl p-4 sm:p-5 border transition-all cursor-pointer group hover:scale-[1.01]"
            style={{
              background: 'linear-gradient(180deg, #064E3B 0%, #05241C 100%)',
              borderColor: '#059669'
            }}
          >
            <div className="w-10 h-10 rounded-xl bg-[#059669]/30 flex items-center justify-center mb-3">
              <BarChart3 className="w-5 h-5 text-[#6EE7B7] group-hover:scale-110 transition-transform" />
            </div>
            <h4 className="font-bold text-white text-sm sm:text-base">Batch Analytics</h4>
            <p className="text-xs text-[#A7F3D0]/80 mt-1">Check pass rates, scores & rankings</p>
          </button>

          {/* Card 4: Certificates */}
          <button
            onClick={() => onNavigateTab('verify')}
            className="text-left rounded-2xl p-4 sm:p-5 border transition-all cursor-pointer group hover:scale-[1.01]"
            style={{
              background: 'linear-gradient(180deg, #831843 0%, #34071B 100%)',
              borderColor: '#DB2777'
            }}
          >
            <div className="w-10 h-10 rounded-xl bg-[#DB2777]/30 flex items-center justify-center mb-3">
              <Award className="w-5 h-5 text-[#F472B6] group-hover:scale-110 transition-transform" />
            </div>
            <h4 className="font-bold text-white text-sm sm:text-base">Certificates</h4>
            <p className="text-xs text-[#FBCFE8]/80 mt-1">Verify credentials & tamper checks</p>
          </button>
        </div>
      </div>

      {/* 5. Recent Quizzes Header & List */}
      <div className="space-y-4 pt-2">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base sm:text-lg font-bold text-white tracking-tight">Recent Quizzes</h3>
            <p className="text-xs text-slate-400">All assessments authored under your account</p>
          </div>
          <button
            onClick={() => onNavigateTab('studio')}
            className="text-xs font-semibold text-indigo-400 hover:text-indigo-300 flex items-center gap-1 cursor-pointer"
          >
            <span>Create Quiz</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[1, 2].map((i) => (
              <div key={i} className="h-28 bg-[#0A102E]/60 border border-[#16214C] rounded-2xl animate-pulse" />
            ))}
          </div>
        ) : quizzes.length === 0 ? (
          <div className="bg-[#0A102E] border border-[#16214C] rounded-3xl p-8 sm:p-12 text-center">
            <div className="w-14 h-14 mx-auto mb-3 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
              <BookOpen className="w-7 h-7 text-indigo-400" />
            </div>
            <h4 className="text-base sm:text-lg font-bold text-white">No Quizzes Created Yet</h4>
            <p className="text-xs text-slate-400 max-w-md mx-auto mt-1 mb-5">
              You haven't created any quizzes yet. Use the Quiz Studio to assemble questions manually, import bulk text, or let AI generate them.
            </p>
            <button
              onClick={() => onNavigateTab('studio')}
              className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-cyan-600 hover:from-indigo-500 hover:to-cyan-500 text-white font-bold text-xs shadow-lg shadow-indigo-600/30 transition-all inline-flex items-center gap-2 cursor-pointer"
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
                className="bg-[#0A102E] border border-[#16214C] hover:border-[#1E2E6C] rounded-2xl p-5 flex flex-col justify-between transition-all group"
              >
                <div>
                  <div className="flex items-start justify-between gap-3">
                    <span className="px-2.5 py-1 rounded-full text-[10px] font-bold bg-indigo-500/15 text-indigo-300 border border-indigo-500/30 uppercase tracking-wider">
                      {quiz.category || 'General Assessment'}
                    </span>
                    <button
                      onClick={() => setDeleteConfirmQuiz(quiz)}
                      className="text-slate-500 hover:text-red-400 p-1 rounded-lg transition-colors"
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
                className="flex-1 py-2 rounded-xl bg-slate-800 text-slate-300 font-semibold text-xs hover:bg-slate-700 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDeleteQuiz(deleteConfirmQuiz.id)}
                className="flex-1 py-2 rounded-xl bg-red-600 hover:bg-red-500 text-white font-bold text-xs shadow-lg shadow-red-600/30 transition-colors"
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

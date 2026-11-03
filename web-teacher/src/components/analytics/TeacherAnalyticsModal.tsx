import React, { useState, useEffect } from 'react';
import { db } from '../../lib/firebase';
import { collection, query, where, getDocs, orderBy, limit } from 'firebase/firestore';
import { useAuth } from '../../context/AuthContext';
import { 
  BarChart3, 
  X, 
  FileSpreadsheet
} from 'lucide-react';

interface TeacherAnalyticsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface SubmissionRecord {
  id: string;
  studentName: string;
  studentEmail: string;
  quizTitle: string;
  score: number;
  totalMarks: number;
  scorePercentage: number;
  passed: boolean;
  submittedAt: number;
}

export const TeacherAnalyticsModal: React.FC<TeacherAnalyticsModalProps> = ({ isOpen, onClose }) => {
  const { currentUser } = useAuth();
  const [submissions, setSubmissions] = useState<SubmissionRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isOpen) {
      fetchAnalytics();
    }
  }, [isOpen, currentUser]);

  const fetchAnalytics = async () => {
    setLoading(true);
    try {
      const teacherId = currentUser?.id;
      let q = query(
        collection(db, 'quiz_results'),
        orderBy('submittedAt', 'desc'),
        limit(50)
      );

      if (teacherId) {
        q = query(
          collection(db, 'quiz_results'),
          where('teacherId', '==', teacherId),
          orderBy('submittedAt', 'desc'),
          limit(50)
        );
      }

      const snap = await getDocs(q);
      const list: SubmissionRecord[] = [];
      snap.forEach((d) => {
        const data = d.data();
        list.push({
          id: d.id,
          studentName: data.studentName || data.userName || 'Student',
          studentEmail: data.studentEmail || data.userEmail || '',
          quizTitle: data.quizTitle || 'Assessment',
          score: data.score || 0,
          totalMarks: data.totalMarks || 10,
          scorePercentage: data.scorePercentage || Math.round(((data.score || 0) / (data.totalMarks || 10)) * 100),
          passed: data.passed ?? ((data.scorePercentage || 0) >= 60),
          submittedAt: data.submittedAt || data.timestamp || Date.now()
        });
      });
      setSubmissions(list);
    } catch (err) {
      console.warn('Analytics fetch error:', err);
      // Fallback: query without teacherId filter
      try {
        const snap = await getDocs(collection(db, 'quiz_results'));
        const list: SubmissionRecord[] = [];
        snap.forEach((d) => {
          const data = d.data();
          list.push({
            id: d.id,
            studentName: data.studentName || 'Student',
            studentEmail: data.studentEmail || '',
            quizTitle: data.quizTitle || 'Assessment',
            score: data.score || 0,
            totalMarks: data.totalMarks || 10,
            scorePercentage: data.scorePercentage || 0,
            passed: data.passed ?? true,
            submittedAt: data.submittedAt || Date.now()
          });
        });
        setSubmissions(list);
      } catch {
        setSubmissions([]);
      }
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const totalAttempts = submissions.length;
  const avgScore = totalAttempts > 0 
    ? Math.round(submissions.reduce((acc, s) => acc + s.scorePercentage, 0) / totalAttempts) 
    : 0;
  const passedCount = submissions.filter((s) => s.passed).length;
  const passRate = totalAttempts > 0 ? Math.round((passedCount / totalAttempts) * 100) : 0;

  // Grade distributions
  const gradeA = submissions.filter((s) => s.scorePercentage >= 90).length;
  const gradeB = submissions.filter((s) => s.scorePercentage >= 75 && s.scorePercentage < 90).length;
  const gradeC = submissions.filter((s) => s.scorePercentage >= 50 && s.scorePercentage < 75).length;
  const gradeF = submissions.filter((s) => s.scorePercentage < 50).length;

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-4xl bg-[#090E26] border border-[#16214C] rounded-[28px] p-6 sm:p-8 shadow-2xl relative max-h-[92vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -15px rgba(5, 150, 105, 0.25)'
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
              <BarChart3 className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">Classroom & Quiz Analytics</h3>
              <p className="text-xs text-slate-400">Comprehensive real-time student performance breakdown</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-900 text-slate-400 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* 4 Summary Stat Cards matching Android TeacherAnalyticsScreen */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 my-5">
          <div className="bg-[#05091F] border border-slate-800 rounded-2xl p-4">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">Total Submissions</span>
            <span className="text-2xl sm:text-3xl font-extrabold text-white mt-1 block">{totalAttempts}</span>
            <span className="text-[10px] text-slate-400">Completed test sessions</span>
          </div>

          <div className="bg-[#05091F] border border-slate-800 rounded-2xl p-4">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">Average Score</span>
            <span className="text-2xl sm:text-3xl font-extrabold text-amber-400 mt-1 block">{avgScore}%</span>
            <span className="text-[10px] text-slate-400">Batch performance mean</span>
          </div>

          <div className="bg-[#05091F] border border-slate-800 rounded-2xl p-4">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">Pass Rate</span>
            <span className="text-2xl sm:text-3xl font-extrabold text-emerald-400 mt-1 block">{passRate}%</span>
            <span className="text-[10px] text-slate-400">{passedCount} of {totalAttempts} passed</span>
          </div>

          <div className="bg-[#05091F] border border-slate-800 rounded-2xl p-4">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">High Scorers (90%+)</span>
            <span className="text-2xl sm:text-3xl font-extrabold text-indigo-400 mt-1 block">{gradeA}</span>
            <span className="text-[10px] text-slate-400">Honor roll candidates</span>
          </div>
        </div>

        {/* Grade Distribution Bar */}
        <div className="bg-[#05091F] border border-slate-800 rounded-2xl p-4 mb-5 space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="font-bold text-white">Score Tier Breakdown</span>
            <span className="text-slate-400">Based on live submissions</span>
          </div>

          <div className="h-3 w-full bg-slate-900 rounded-full overflow-hidden flex">
            <div style={{ width: `${totalAttempts > 0 ? (gradeA / totalAttempts) * 100 : 25}%` }} className="bg-emerald-500 h-full" title={`Grade A (90%+): ${gradeA}`} />
            <div style={{ width: `${totalAttempts > 0 ? (gradeB / totalAttempts) * 100 : 25}%` }} className="bg-blue-500 h-full" title={`Grade B (75-89%): ${gradeB}`} />
            <div style={{ width: `${totalAttempts > 0 ? (gradeC / totalAttempts) * 100 : 25}%` }} className="bg-amber-500 h-full" title={`Grade C (50-74%): ${gradeC}`} />
            <div style={{ width: `${totalAttempts > 0 ? (gradeF / totalAttempts) * 100 : 25}%` }} className="bg-red-500 h-full" title={`Needs Review (<50%): ${gradeF}`} />
          </div>

          <div className="grid grid-cols-4 gap-2 text-[10px] sm:text-xs pt-1 text-slate-400">
            <div className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-emerald-500" /><span>A (90%+): {gradeA}</span></div>
            <div className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-blue-500" /><span>B (75-89%): {gradeB}</span></div>
            <div className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-amber-500" /><span>C (50-74%): {gradeC}</span></div>
            <div className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-red-500" /><span>F (&lt;50%): {gradeF}</span></div>
          </div>
        </div>

        {/* Recent Submissions List */}
        <div className="flex-1 overflow-y-auto space-y-2.5 pr-1">
          <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">
            Recent Student Submissions ({submissions.length})
          </h4>

          {loading ? (
            <p className="text-xs text-slate-400 py-6 text-center">Loading submission records...</p>
          ) : submissions.length === 0 ? (
            <div className="p-8 rounded-2xl bg-[#05091F] border border-slate-800 text-center">
              <FileSpreadsheet className="w-8 h-8 text-slate-600 mx-auto mb-2" />
              <p className="text-sm font-bold text-white">No Submissions Recorded Yet</p>
              <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
                Student test submissions completed on the QuizMaster Android APK or Web will automatically appear here with full grade metrics.
              </p>
            </div>
          ) : (
            <div className="space-y-2">
              {submissions.map((sub) => (
                <div 
                  key={sub.id}
                  className="p-3.5 rounded-xl bg-[#05091F] border border-slate-800/80 flex items-center justify-between gap-3 text-xs"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-xl bg-slate-800 flex items-center justify-center font-bold text-slate-300">
                      {sub.studentName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <p className="font-bold text-white">{sub.studentName}</p>
                      <p className="text-[11px] text-slate-400 line-clamp-1">{sub.quizTitle}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <span className="font-bold text-white block">{sub.scorePercentage}%</span>
                      <span className="text-[10px] text-slate-400">
                        {new Date(sub.submittedAt).toLocaleDateString()}
                      </span>
                    </div>

                    <span className={`px-2.5 py-1 rounded-full text-[10px] font-bold ${
                      sub.passed 
                        ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30' 
                        : 'bg-red-500/15 text-red-300 border border-red-500/30'
                    }`}>
                      {sub.passed ? 'PASSED' : 'RETRY'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="pt-4 mt-3 border-t border-slate-800 flex items-center justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-semibold transition-colors cursor-pointer"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

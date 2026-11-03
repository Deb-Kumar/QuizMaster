import React, { useState, useEffect } from 'react';
import { db } from '../../lib/firebase';
import { collection, query, where, getDocs, orderBy, limit } from 'firebase/firestore';
import { useAuth } from '../../context/AuthContext';
import { Bell, UserPlus, Award, Sparkles, X, CheckCircle2 } from 'lucide-react';

interface TeacherNotificationsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onNavigateBatches: () => void;
}

interface NotificationItem {
  id: string;
  title: string;
  message: string;
  type: 'JOIN_REQUEST' | 'SUBMISSION' | 'SYSTEM' | 'QUIZ';
  timestamp: number;
  read: boolean;
}

export const TeacherNotificationsModal: React.FC<TeacherNotificationsModalProps> = ({ 
  isOpen, 
  onClose,
  onNavigateBatches
}) => {
  const { currentUser } = useAuth();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen, currentUser]);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const items: NotificationItem[] = [];

      // 1. Fetch pending join requests
      if (currentUser?.id) {
        const qReq = query(
          collection(db, 'join_requests'),
          where('teacherId', '==', currentUser.id)
        );
        const reqSnap = await getDocs(qReq);
        reqSnap.forEach((d) => {
          const data = d.data();
          items.push({
            id: `req_${d.id}`,
            title: 'New Student Join Request',
            message: `${data.studentName || 'A student'} requested to join batch "${data.batchName || 'Classroom'}"`,
            type: 'JOIN_REQUEST',
            timestamp: data.requestedAt || data.timestamp || Date.now(),
            read: !data.isPending
          });
        });
      }

      // 2. Fetch recent quiz results
      const qRes = query(
        collection(db, 'quiz_results'),
        orderBy('submittedAt', 'desc'),
        limit(10)
      );
      const resSnap = await getDocs(qRes).catch(() => null);
      if (resSnap) {
        resSnap.forEach((d) => {
          const data = d.data();
          items.push({
            id: `res_${d.id}`,
            title: 'Quiz Submission Received',
            message: `${data.studentName || 'Student'} scored ${data.scorePercentage || data.score || 0}% on "${data.quizTitle || 'Assessment'}"`,
            type: 'SUBMISSION',
            timestamp: data.submittedAt || Date.now(),
            read: false
          });
        });
      }

      // 3. System Welcome Notification
      items.push({
        id: 'sys_welcome',
        title: 'QuizMaster v5.0.2 Active',
        message: 'Your unified teacher workspace is connected to Cloud Firestore & Firebase Auth.',
        type: 'SYSTEM',
        timestamp: Date.now() - 3600000,
        read: true
      });

      // Sort by timestamp desc
      items.sort((a, b) => b.timestamp - a.timestamp);
      setNotifications(items);
    } catch {
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const handleMarkAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-lg bg-[#090E26] border border-[#16214C] rounded-[28px] p-6 sm:p-7 shadow-2xl relative max-h-[90vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -15px rgba(124, 58, 237, 0.25)'
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-indigo-500/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
              <Bell className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">Notifications</h3>
              <p className="text-xs text-slate-400">Classroom activity, submissions & requests</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-900 text-slate-400 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Action bar */}
        <div className="flex items-center justify-between py-3 text-xs">
          <span className="font-semibold text-slate-400">
            {notifications.filter((n) => !n.read).length} Unread Updates
          </span>
          <button
            onClick={handleMarkAllRead}
            className="text-indigo-400 hover:text-indigo-300 font-bold flex items-center gap-1 cursor-pointer"
          >
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Mark all read</span>
          </button>
        </div>

        {/* Notifications List */}
        <div className="flex-1 overflow-y-auto space-y-2.5 pr-1">
          {loading ? (
            <p className="text-xs text-slate-400 py-8 text-center">Loading updates...</p>
          ) : notifications.length === 0 ? (
            <div className="p-8 rounded-2xl bg-[#05091F] border border-slate-800 text-center">
              <Bell className="w-8 h-8 text-slate-600 mx-auto mb-2" />
              <p className="text-sm font-bold text-white">No New Notifications</p>
              <p className="text-xs text-slate-400 mt-1">
                You're completely up to date. Student requests and test completions will notify you here.
              </p>
            </div>
          ) : (
            notifications.map((n) => (
              <div
                key={n.id}
                className={`p-3.5 rounded-2xl border transition-all ${
                  !n.read 
                    ? 'bg-[#0F173C] border-[#1E2858]' 
                    : 'bg-[#05091F] border-slate-800/80 opacity-75'
                }`}
              >
                <div className="flex items-start gap-3">
                  <div className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 mt-0.5 ${
                    n.type === 'JOIN_REQUEST'
                      ? 'bg-amber-500/20 text-amber-400'
                      : n.type === 'SUBMISSION'
                      ? 'bg-emerald-500/20 text-emerald-400'
                      : 'bg-indigo-500/20 text-indigo-400'
                  }`}>
                    {n.type === 'JOIN_REQUEST' && <UserPlus className="w-4 h-4" />}
                    {n.type === 'SUBMISSION' && <Award className="w-4 h-4" />}
                    {n.type === 'SYSTEM' && <Sparkles className="w-4 h-4" />}
                  </div>

                  <div className="flex-1">
                    <div className="flex items-center justify-between">
                      <h4 className="text-xs font-bold text-white">{n.title}</h4>
                      <span className="text-[10px] text-slate-500">
                        {new Date(n.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <p className="text-[11px] text-slate-300 mt-0.5 leading-relaxed">{n.message}</p>

                    {n.type === 'JOIN_REQUEST' && (
                      <button
                        onClick={() => {
                          onClose();
                          onNavigateBatches();
                        }}
                        className="mt-2 text-[10px] font-bold text-amber-400 hover:text-amber-300 underline cursor-pointer"
                      >
                        Review in Batches →
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))
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

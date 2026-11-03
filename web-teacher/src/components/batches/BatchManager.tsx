import React, { useState, useEffect } from 'react';
import type { Batch } from '../../types';
import { db } from '../../lib/firebase';
import { collection, getDocs, addDoc, query, where, doc, updateDoc, arrayUnion } from 'firebase/firestore';
import { useAuth } from '../../context/AuthContext';
import { 
  Users, 
  Plus, 
  Key, 
  UserCheck, 
  Check, 
  Copy
} from 'lucide-react';
import { CardSkeleton } from '../ui/Skeleton';

interface JoinRequest {
  id: string;
  studentId: string;
  studentName: string;
  studentEmail: string;
  batchId: string;
  batchName: string;
  isPending: boolean;
  requestedAt: number;
}

export const BatchManager: React.FC = () => {
  const { currentUser } = useAuth();
  const [batches, setBatches] = useState<Batch[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [creating, setCreating] = useState(false);
  const [copiedCode, setCopiedCode] = useState<string | null>(null);

  // Join requests state
  const [pendingRequests, setPendingRequests] = useState<JoinRequest[]>([]);
  const [selectedBatchForDetails, setSelectedBatchForDetails] = useState<Batch | null>(null);
  const [enrolledStudents, setEnrolledStudents] = useState<{ id: string; name: string; email: string }[]>([]);

  useEffect(() => {
    fetchBatches();
    fetchJoinRequests();
  }, [currentUser]);

  const fetchBatches = async () => {
    setLoading(true);
    try {
      const teacherId = currentUser?.id;
      let snap;
      if (teacherId) {
        const q = query(collection(db, 'batches'), where('teacherId', '==', teacherId));
        snap = await getDocs(q);
      } else {
        snap = await getDocs(collection(db, 'batches'));
      }

      const list: Batch[] = snap.docs.map((d) => ({
        id: d.id,
        ...d.data()
      })) as Batch[];

      setBatches(list);
    } catch (err) {
      console.error('Error fetching batches:', err);
      // Fallback: try fetching all
      try {
        const snap = await getDocs(collection(db, 'batches'));
        const list: Batch[] = snap.docs.map((d) => ({ id: d.id, ...d.data() })) as Batch[];
        setBatches(list);
      } catch {
        setBatches([]);
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchJoinRequests = async () => {
    if (!currentUser?.id) return;
    try {
      const q = query(
        collection(db, 'join_requests'),
        where('teacherId', '==', currentUser.id),
        where('isPending', '==', true)
      );
      const snap = await getDocs(q);
      const list: JoinRequest[] = [];
      snap.forEach((d) => {
        list.push({ id: d.id, ...d.data() } as JoinRequest);
      });
      setPendingRequests(list);
    } catch {
      // ignore
    }
  };

  const handleCreateBatch = async () => {
    if (!name.trim()) return;
    setCreating(true);

    const chars = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ';
    let code = '';
    for (let i = 0; i < 6; i++) {
      code += chars.charAt(Math.floor(Math.random() * chars.length));
    }

    const newBatchData = {
      code,
      name: name.trim(),
      description: description.trim(),
      teacherId: currentUser?.id || 'teacher_web',
      teacherName: currentUser?.name || 'Authorized Instructor',
      studentIds: [],
      pendingRequests: [],
      createdAt: Date.now()
    };

    try {
      const ref = await addDoc(collection(db, 'batches'), newBatchData);
      setBatches([{ id: ref.id, ...newBatchData }, ...batches]);
      setName('');
      setDescription('');
      setShowModal(false);
    } catch (err) {
      console.error('Error creating batch:', err);
      setBatches([{ id: `b_${Date.now()}`, ...newBatchData }, ...batches]);
      setName('');
      setDescription('');
      setShowModal(false);
    } finally {
      setCreating(false);
    }
  };

  const copyCode = (code: string) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2000);
  };

  const handleApproveRequest = async (req: JoinRequest) => {
    try {
      // 1. Mark request as approved
      await updateDoc(doc(db, 'join_requests', req.id), {
        isPending: false,
        status: 'APPROVED',
        approvedAt: Date.now()
      });

      // 2. Add student to batch
      await updateDoc(doc(db, 'batches', req.batchId), {
        studentIds: arrayUnion(req.studentId)
      });

      // 3. Update local state
      setPendingRequests((prev) => prev.filter((r) => r.id !== req.id));
      setBatches((prev) =>
        prev.map((b) =>
          b.id === req.batchId
            ? { ...b, studentIds: [...(b.studentIds || []), req.studentId] }
            : b
        )
      );
    } catch (e) {
      console.error('Approve failed:', e);
      alert('Could not approve request.');
    }
  };

  const handleRejectRequest = async (req: JoinRequest) => {
    try {
      await updateDoc(doc(db, 'join_requests', req.id), {
        isPending: false,
        status: 'REJECTED',
        rejectedAt: Date.now()
      });
      setPendingRequests((prev) => prev.filter((r) => r.id !== req.id));
    } catch (e) {
      console.error('Reject failed:', e);
    }
  };

  const handleViewBatchDetails = async (batch: Batch) => {
    setSelectedBatchForDetails(batch);
    // Fetch enrolled students
    if (batch.studentIds && batch.studentIds.length > 0) {
      try {
        const studentDocs = await Promise.all(
          batch.studentIds.slice(0, 20).map(async (sid) => {
            const snap = await getDocs(query(collection(db, 'users'), where('__name__', '==', sid)));
            if (!snap.empty) {
              const d = snap.docs[0].data();
              return { id: sid, name: d.name || 'Student', email: d.email || '' };
            }
            return { id: sid, name: `Student (${sid.substring(0, 5)})`, email: '' };
          })
        );
        setEnrolledStudents(studentDocs);
      } catch {
        setEnrolledStudents(batch.studentIds.map((sid) => ({ id: sid, name: 'Enrolled Student', email: '' })));
      }
    } else {
      setEnrolledStudents([]);
    }
  };

  return (
    <div className="w-full max-w-5xl mx-auto px-4 py-8 space-y-8 animate-in fade-in duration-300">
      
      {/* Top Banner matching Android TeacherHomeContent */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-6">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 text-cyan-400 text-xs font-semibold mb-2">
            <Users className="w-3.5 h-3.5" />
            <span>Classroom & Batches</span>
          </div>
          <h2 className="text-2xl md:text-3xl font-extrabold text-white">Manage Batches & Students</h2>
          <p className="text-slate-400 text-sm mt-1">
            Generate 6-character access codes for student enrollment and assign batch quizzes.
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="px-5 py-2.5 bg-gradient-to-r from-cyan-600 to-indigo-600 hover:from-cyan-500 hover:to-indigo-500 text-white font-bold rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-cyan-600/25 active:scale-95 transition-all text-xs sm:text-sm cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Create New Batch</span>
        </button>
      </div>

      {/* Pending Join Requests Section */}
      {pendingRequests.length > 0 && (
        <div className="p-5 rounded-2xl bg-[#1E1338] border border-[#7C3AED]/60 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-amber-400 animate-pulse" />
              <h3 className="text-sm font-bold text-white">Pending Student Join Requests</h3>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-extrabold bg-amber-500 text-black">
                {pendingRequests.length} pending
              </span>
            </div>
            <span className="text-xs text-purple-300">Requires teacher approval</span>
          </div>

          <div className="space-y-2">
            {pendingRequests.map((req) => (
              <div 
                key={req.id}
                className="p-3 rounded-xl bg-[#0F0B26] border border-purple-500/20 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs"
              >
                <div>
                  <p className="font-bold text-white">{req.studentName} ({req.studentEmail || 'Student'})</p>
                  <p className="text-[11px] text-purple-300">
                    Requested entry to: <span className="font-semibold text-white">{req.batchName}</span>
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleRejectRequest(req)}
                    className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold cursor-pointer"
                  >
                    Decline
                  </button>
                  <button
                    onClick={() => handleApproveRequest(req)}
                    className="px-4 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold flex items-center gap-1 cursor-pointer"
                  >
                    <Check className="w-3.5 h-3.5" />
                    <span>Approve</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Batches Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <CardSkeleton />
          <CardSkeleton />
        </div>
      ) : batches.length === 0 ? (
        <div className="bg-[#0A102E] border border-[#16214C] rounded-3xl p-12 text-center">
          <Users className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-white mb-1">No Batches Created Yet</h3>
          <p className="text-slate-400 text-xs sm:text-sm max-w-sm mx-auto mb-6">
            Create your first batch to start inviting students with 6-character access codes and assign private assessments.
          </p>
          <button
            onClick={() => setShowModal(true)}
            className="px-5 py-2.5 bg-gradient-to-r from-indigo-600 to-cyan-600 hover:from-indigo-500 hover:to-cyan-500 text-white text-xs font-bold rounded-xl shadow-lg shadow-indigo-600/30 transition-all cursor-pointer"
          >
            Create Batch Now
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {batches.map((batch) => (
            <div
              key={batch.id}
              className="bg-[#0A102E] border border-[#16214C] hover:border-cyan-500/40 rounded-2xl p-6 space-y-4 transition-all group"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="text-lg font-bold text-white group-hover:text-cyan-300 transition-colors">
                    {batch.name}
                  </h3>
                  <p className="text-xs text-slate-400 mt-1 line-clamp-2">
                    {batch.description || 'General classroom batch for assigned tests.'}
                  </p>
                </div>

                {/* 6-char Batch Code Badge */}
                <button
                  onClick={() => copyCode(batch.code)}
                  title="Click to copy Batch Code"
                  className="px-3 py-1.5 rounded-xl bg-[#050B22] border border-slate-800 hover:border-cyan-500/50 flex items-center gap-2 font-mono text-xs font-bold text-cyan-400 transition-colors cursor-pointer"
                >
                  <Key className="w-3.5 h-3.5" />
                  <span>{batch.code}</span>
                  {copiedCode === batch.code ? (
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                  ) : (
                    <Copy className="w-3.5 h-3.5 text-slate-500" />
                  )}
                </button>
              </div>

              {/* Stats Bar */}
              <div className="flex items-center justify-between pt-3 border-t border-slate-800/80 text-xs">
                <div className="flex items-center gap-2 text-slate-400">
                  <UserCheck className="w-4 h-4 text-emerald-400" />
                  <span className="font-semibold text-white">{batch.studentIds?.length || 0}</span>
                  <span>Enrolled Students</span>
                </div>

                <button
                  onClick={() => handleViewBatchDetails(batch)}
                  className="font-semibold text-cyan-400 hover:text-cyan-300 text-xs cursor-pointer"
                >
                  View Classroom →
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal: Create Batch */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
          <div className="bg-[#0A102E] border border-[#16214C] rounded-3xl p-6 md:p-8 max-w-md w-full shadow-2xl space-y-5">
            <div className="flex items-center justify-between pb-2 border-b border-slate-800">
              <h3 className="text-lg font-bold text-white">Create New Classroom Batch</h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-white">✕</button>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                  Batch Name *
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Kotlin Jetpack Compose - Batch A"
                  className="w-full bg-[#050B22] border border-[#1B2E6C] focus:border-cyan-500 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
                />
              </div>

              <div>
                <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1">
                  Description / Subject
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={3}
                  placeholder="Schedule details, meeting hours, or subject syllabus..."
                  className="w-full bg-[#050B22] border border-[#1B2E6C] focus:border-cyan-500 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                onClick={() => setShowModal(false)}
                className="px-4 py-2 text-slate-400 hover:text-white text-xs font-semibold transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handleCreateBatch}
                disabled={creating || !name.trim()}
                className="px-5 py-2.5 bg-gradient-to-r from-cyan-600 to-indigo-600 hover:from-cyan-500 hover:to-indigo-500 disabled:opacity-50 text-white text-xs font-bold rounded-xl shadow-lg shadow-cyan-600/30 transition-all cursor-pointer"
              >
                {creating ? 'Creating...' : 'Create Batch'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Batch Classroom Details */}
      {selectedBatchForDetails && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200">
          <div className="bg-[#0A102E] border border-[#16214C] rounded-[28px] p-6 max-w-lg w-full shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div>
                <h3 className="text-lg font-bold text-white">{selectedBatchForDetails.name}</h3>
                <span className="text-xs text-cyan-400 font-mono font-bold">Code: {selectedBatchForDetails.code}</span>
              </div>
              <button onClick={() => setSelectedBatchForDetails(null)} className="text-slate-400 hover:text-white">✕</button>
            </div>

            <div>
              <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">
                Enrolled Students ({enrolledStudents.length})
              </h4>

              {enrolledStudents.length === 0 ? (
                <div className="p-6 rounded-2xl bg-[#050B22] border border-slate-800 text-center text-xs text-slate-400">
                  No students currently enrolled in this batch. Share code <span className="font-mono text-cyan-400 font-bold">{selectedBatchForDetails.code}</span> with candidates.
                </div>
              ) : (
                <div className="max-h-48 overflow-y-auto space-y-2 pr-1">
                  {enrolledStudents.map((st) => (
                    <div key={st.id} className="p-3 rounded-xl bg-[#050B22] border border-slate-800 flex items-center justify-between text-xs">
                      <span className="font-bold text-white">{st.name}</span>
                      <span className="text-[11px] text-slate-400">{st.email}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="pt-3 border-t border-slate-800 flex justify-end">
              <button
                onClick={() => setSelectedBatchForDetails(null)}
                className="px-4 py-2 rounded-xl bg-slate-900 text-white text-xs font-semibold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

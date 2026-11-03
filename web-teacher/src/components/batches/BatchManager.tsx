import React, { useState, useEffect } from 'react';
import type { Batch } from '../../types';
import { db } from '../../lib/firebase';
import { collection, getDocs, addDoc, query, orderBy } from 'firebase/firestore';
import { Users, Plus, Key, UserCheck, Check, Copy } from 'lucide-react';
import { CardSkeleton } from '../ui/Skeleton';

export const BatchManager: React.FC = () => {
  const [batches, setBatches] = useState<Batch[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [creating, setCreating] = useState(false);
  const [copiedCode, setCopiedCode] = useState<string | null>(null);

  useEffect(() => {
    fetchBatches();
  }, []);

  const fetchBatches = async () => {
    setLoading(true);
    try {
      const q = query(collection(db, 'batches'), orderBy('createdAt', 'desc'));
      const snap = await getDocs(q);
      const list: Batch[] = snap.docs.map((doc) => ({
        id: doc.id,
        ...doc.data()
      })) as Batch[];
      setBatches(list);
    } catch (err) {
      console.warn('Could not fetch batches from firestore, using sample local list:', err);
      setBatches([
        {
          id: 'b_sample_1',
          code: 'KT3921',
          name: 'Android Kotlin Masterclass 2026',
          description: 'Jetpack Compose, Clean Architecture, and Coroutines Batch',
          teacherId: 'teacher_web',
          teacherName: 'Lead Instructor',
          studentIds: ['s1', 's2', 's3', 's4'],
          pendingRequests: ['s5'],
          createdAt: Date.now() - 86400000 * 4
        },
        {
          id: 'b_sample_2',
          code: 'WEB882',
          name: 'Fullstack Web & Cloud Engineering',
          description: 'React, Node.js, and Cloud Infrastructure cohorts',
          teacherId: 'teacher_web',
          teacherName: 'Lead Instructor',
          studentIds: ['s10', 's11'],
          pendingRequests: [],
          createdAt: Date.now() - 86400000 * 10
        }
      ]);
    } finally {
      setLoading(false);
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
      teacherId: 'teacher_web',
      teacherName: 'Web Portal Instructor',
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

  return (
    <div className="w-full max-w-5xl mx-auto px-4 py-8 space-y-8">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-6">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/10 text-cyan-400 text-xs font-semibold mb-2">
            <Users className="w-3.5 h-3.5" />
            Classroom & Batches
          </div>
          <h2 className="text-2xl md:text-3xl font-extrabold text-white">Manage Batches & Students</h2>
          <p className="text-slate-400 text-sm mt-1">
            Generate 6-character access codes for student enrollment and assign batch quizzes.
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="px-5 py-2.5 bg-gradient-to-r from-cyan-600 to-indigo-600 hover:from-cyan-500 hover:to-indigo-500 text-white font-semibold rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-cyan-600/20 active:scale-95 transition-all text-sm"
        >
          <Plus className="w-4 h-4" />
          Create New Batch
        </button>
      </div>

      {/* Batches Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <CardSkeleton />
          <CardSkeleton />
        </div>
      ) : batches.length === 0 ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-12 text-center backdrop-blur-xl">
          <Users className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-white mb-1">No Batches Yet</h3>
          <p className="text-slate-400 text-sm max-w-sm mx-auto mb-6">
            Create your first batch to start inviting students and assigning private assessments.
          </p>
          <button
            onClick={() => setShowModal(true)}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl transition-colors"
          >
            Create Batch Now
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {batches.map((batch) => (
            <div
              key={batch.id}
              className="bg-slate-900/60 border border-slate-800/80 rounded-2xl p-6 backdrop-blur-xl space-y-4 hover:border-slate-700 transition-all group"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="text-lg font-bold text-white group-hover:text-indigo-300 transition-colors">
                    {batch.name}
                  </h3>
                  <p className="text-xs text-slate-400 mt-1 line-clamp-2">{batch.description || 'No description provided'}</p>
                </div>
                {/* 6-char Batch Code Badge */}
                <button
                  onClick={() => copyCode(batch.code)}
                  title="Click to copy Batch Code"
                  className="px-3 py-1.5 rounded-xl bg-slate-950 border border-slate-800 hover:border-cyan-500/50 flex items-center gap-2 font-mono text-xs font-bold text-cyan-400 transition-colors"
                >
                  <Key className="w-3.5 h-3.5" />
                  {batch.code}
                  {copiedCode === batch.code ? (
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                  ) : (
                    <Copy className="w-3.5 h-3.5 text-slate-500" />
                  )}
                </button>
              </div>

              {/* Stats Bar */}
              <div className="flex items-center gap-4 pt-3 border-t border-slate-800/80 text-xs text-slate-400">
                <div className="flex items-center gap-1.5">
                  <UserCheck className="w-4 h-4 text-emerald-400" />
                  <span>{batch.studentIds?.length || 0} Enrolled</span>
                </div>
                {batch.pendingRequests && batch.pendingRequests.length > 0 && (
                  <div className="px-2 py-0.5 rounded-md bg-amber-500/10 text-amber-300 font-semibold border border-amber-500/30">
                    {batch.pendingRequests.length} Pending
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal: Create Batch */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 md:p-8 max-w-md w-full shadow-2xl space-y-6">
            <h3 className="text-xl font-bold text-white">Create New Classroom Batch</h3>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                  Batch Name *
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Kotlin Jetpack Compose - Batch A"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-cyan-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-1">
                  Description
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={3}
                  placeholder="Overview or schedule details for your students..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-cyan-500"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                onClick={() => setShowModal(false)}
                className="px-4 py-2 text-slate-400 hover:text-white text-sm font-semibold transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleCreateBatch}
                disabled={creating || !name.trim()}
                className="px-5 py-2.5 bg-gradient-to-r from-cyan-600 to-indigo-600 hover:from-cyan-500 hover:to-indigo-500 disabled:opacity-50 text-white text-sm font-semibold rounded-xl shadow-lg shadow-cyan-600/30 transition-all"
              >
                {creating ? 'Creating...' : 'Create Batch'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

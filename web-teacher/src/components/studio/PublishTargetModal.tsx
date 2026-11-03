import React, { useState, useEffect } from 'react';
import type { Batch } from '../../types';
import { db } from '../../lib/firebase';
import { collection, query, where, getDocs } from 'firebase/firestore';
import { useAuth } from '../../context/AuthContext';
import { Globe, Users, Key, X, Check, ShieldCheck, ArrowRight } from 'lucide-react';

interface PublishTargetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirmPublish: (target: {
    isPublic: boolean;
    batchIds: string[];
    accessCode: string;
  }) => void;
  defaultCode: string;
}

export const PublishTargetModal: React.FC<PublishTargetModalProps> = ({
  isOpen,
  onClose,
  onConfirmPublish,
  defaultCode
}) => {
  const { currentUser } = useAuth();
  const [targetType, setTargetType] = useState<'public' | 'batches' | 'code'>('public');
  const [batches, setBatches] = useState<Batch[]>([]);
  const [selectedBatchIds, setSelectedBatchIds] = useState<string[]>([]);
  const [accessCode, setAccessCode] = useState(defaultCode);
  const [loadingBatches, setLoadingBatches] = useState(false);

  useEffect(() => {
    if (isOpen && currentUser?.id) {
      setLoadingBatches(true);
      const q = query(collection(db, 'batches'), where('teacherId', '==', currentUser.id));
      getDocs(q)
        .then((snap) => {
          const loaded: Batch[] = [];
          snap.forEach((d) => loaded.push({ id: d.id, ...d.data() } as Batch));
          setBatches(loaded);
        })
        .catch(() => {})
        .finally(() => setLoadingBatches(false));
    }
  }, [isOpen, currentUser]);

  if (!isOpen) return null;

  const toggleBatch = (batchId: string) => {
    if (selectedBatchIds.includes(batchId)) {
      setSelectedBatchIds(selectedBatchIds.filter((id) => id !== batchId));
    } else {
      setSelectedBatchIds([...selectedBatchIds, batchId]);
    }
  };

  const handlePublish = () => {
    onConfirmPublish({
      isPublic: targetType === 'public',
      batchIds: targetType === 'batches' ? selectedBatchIds : [],
      accessCode: accessCode.trim().toUpperCase() || defaultCode
    });
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-lg bg-[#0A102E] border border-[#16214C] rounded-[28px] p-6 sm:p-7 shadow-2xl relative"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -15px rgba(79, 70, 229, 0.3)'
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-indigo-500/15 border border-indigo-500/30 flex items-center justify-center text-indigo-400">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">Select Publish Target</h3>
              <p className="text-xs text-slate-400">Configure who can take this assessment</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-900 text-slate-400 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Target Options */}
        <div className="mt-5 space-y-3">
          {/* Option 1: Public */}
          <div
            onClick={() => setTargetType('public')}
            className={`p-4 rounded-2xl border cursor-pointer transition-all ${
              targetType === 'public'
                ? 'bg-indigo-600/15 border-indigo-500 shadow-md'
                : 'bg-[#050B22] border-slate-800 hover:border-slate-700'
            }`}
          >
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-xl bg-indigo-500/20 flex items-center justify-center text-indigo-400 mt-0.5">
                <Globe className="w-5 h-5" />
              </div>
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <h4 className="text-sm font-bold text-white">Public Assessment</h4>
                  <span className="text-[10px] font-bold text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded-full">
                    Recommended
                  </span>
                </div>
                <p className="text-xs text-slate-400 mt-0.5">
                  Available in the global catalog for all enrolled QuizMaster students.
                </p>
              </div>
            </div>
          </div>

          {/* Option 2: Batches */}
          <div
            onClick={() => setTargetType('batches')}
            className={`p-4 rounded-2xl border cursor-pointer transition-all ${
              targetType === 'batches'
                ? 'bg-indigo-600/15 border-indigo-500 shadow-md'
                : 'bg-[#050B22] border-slate-800 hover:border-slate-700'
            }`}
          >
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-xl bg-cyan-500/20 flex items-center justify-center text-cyan-400 mt-0.5">
                <Users className="w-5 h-5" />
              </div>
              <div className="flex-1">
                <h4 className="text-sm font-bold text-white">Specific Batches Only</h4>
                <p className="text-xs text-slate-400 mt-0.5">
                  Restrict strictly to students enrolled in your selected classrooms.
                </p>
              </div>
            </div>

            {targetType === 'batches' && (
              <div className="mt-3 pt-3 border-t border-slate-800/80 space-y-2">
                <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">
                  Select Batches:
                </span>
                {loadingBatches ? (
                  <p className="text-xs text-slate-400">Loading your batches...</p>
                ) : batches.length === 0 ? (
                  <p className="text-xs text-amber-300">
                    No batches found. Create a batch in the "Batches" tab first.
                  </p>
                ) : (
                  <div className="max-h-36 overflow-y-auto space-y-1.5 pr-1">
                    {batches.map((b) => {
                      const isChecked = selectedBatchIds.includes(b.id);
                      return (
                        <div
                          key={b.id}
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleBatch(b.id);
                          }}
                          className={`flex items-center justify-between p-2 rounded-xl text-xs cursor-pointer border ${
                            isChecked
                              ? 'bg-cyan-500/20 border-cyan-500 text-white font-semibold'
                              : 'bg-slate-900 border-slate-800 text-slate-300'
                          }`}
                        >
                          <span>{b.name} ({b.code})</span>
                          {isChecked && <Check className="w-4 h-4 text-cyan-400" />}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Option 3: Private Access Code */}
          <div
            onClick={() => setTargetType('code')}
            className={`p-4 rounded-2xl border cursor-pointer transition-all ${
              targetType === 'code'
                ? 'bg-indigo-600/15 border-indigo-500 shadow-md'
                : 'bg-[#050B22] border-slate-800 hover:border-slate-700'
            }`}
          >
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-xl bg-purple-500/20 flex items-center justify-center text-purple-400 mt-0.5">
                <Key className="w-5 h-5" />
              </div>
              <div className="flex-1">
                <h4 className="text-sm font-bold text-white">Private Access Code</h4>
                <p className="text-xs text-slate-400 mt-0.5">
                  Only students with the 6-character security code can enter the exam.
                </p>
              </div>
            </div>

            {targetType === 'code' && (
              <div className="mt-3 pt-3 border-t border-slate-800/80">
                <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
                  Access Code
                </label>
                <input
                  type="text"
                  maxLength={8}
                  value={accessCode}
                  onChange={(e) => setAccessCode(e.target.value.toUpperCase())}
                  placeholder="e.g. QZ-8891"
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-white font-mono font-bold tracking-widest text-sm focus:outline-none focus:border-purple-500"
                />
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="mt-6 pt-4 border-t border-slate-800 flex items-center justify-end gap-3">
          <button
            onClick={onClose}
            className="px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 text-xs font-semibold transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={handlePublish}
            disabled={targetType === 'batches' && selectedBatchIds.length === 0}
            className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 disabled:opacity-40 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all flex items-center gap-2 cursor-pointer"
          >
            <span>Confirm & Publish</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};

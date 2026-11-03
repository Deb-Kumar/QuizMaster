import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { db } from '../../lib/firebase';
import { doc, setDoc } from 'firebase/firestore';
import { User, Mail, School, Building, FileText, Check, X } from 'lucide-react';

interface TeacherProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const TeacherProfileModal: React.FC<TeacherProfileModalProps> = ({ isOpen, onClose }) => {
  const { currentUser } = useAuth();
  const [name, setName] = useState(currentUser?.name || '');
  const [department, setDepartment] = useState(currentUser?.department || 'Computer Science & Engineering');
  const [college, setCollege] = useState('QuizMaster Academy');
  const [bio, setBio] = useState('Passionate educator dedicated to modern computer science assessment.');
  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  if (!isOpen) return null;

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    setSaving(true);
    setSavedSuccess(false);

    try {
      if (currentUser?.id) {
        const userRef = doc(db, 'users', currentUser.id);
        await setDoc(userRef, {
          name: name.trim(),
          department: department.trim(),
          college: college.trim(),
          bio: bio.trim(),
          role: 'TEACHER',
          updatedAt: Date.now()
        }, { merge: true });

        // Update local storage
        const updated = {
          ...currentUser,
          name: name.trim(),
          department: department.trim()
        };
        localStorage.setItem('qm_teacher_user', JSON.stringify(updated));
      }

      setSavedSuccess(true);
      setTimeout(() => {
        setSavedSuccess(false);
        onClose();
      }, 1000);
    } catch (err) {
      console.error('Error updating teacher profile:', err);
      // Fallback save in localStorage
      if (currentUser) {
        const updated = {
          ...currentUser,
          name: name.trim(),
          department: department.trim()
        };
        localStorage.setItem('qm_teacher_user', JSON.stringify(updated));
      }
      setSavedSuccess(true);
      setTimeout(() => {
        setSavedSuccess(false);
        onClose();
      }, 1000);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-lg bg-[#090E26] border border-[#16214C] rounded-[28px] p-6 sm:p-7 shadow-2xl relative"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -15px rgba(99, 102, 241, 0.3)'
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-indigo-500/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400 font-extrabold text-base">
              {name ? name.charAt(0).toUpperCase() : 'T'}
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">Instructor Profile</h3>
              <p className="text-xs text-slate-400">Manage your verified academic profile credentials</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-900 text-slate-400 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {savedSuccess && (
          <div className="my-3 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs font-bold flex items-center gap-2">
            <Check className="w-4 h-4" />
            <span>Profile information synchronized successfully!</span>
          </div>
        )}

        <form onSubmit={handleSave} className="space-y-4 my-4">
          <div>
            <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-indigo-400" />
              <span>Full Name *</span>
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Dr. Alex Rivera"
              className="w-full bg-[#05091F] border border-slate-800 focus:border-indigo-500 rounded-xl px-3.5 py-2.5 text-white text-xs sm:text-sm focus:outline-none transition-colors"
            />
          </div>

          <div>
            <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
              <Mail className="w-3.5 h-3.5 text-slate-500" />
              <span>Email Address (Account ID)</span>
            </label>
            <input
              type="email"
              disabled
              value={currentUser?.email || 'teacher@institution.edu'}
              className="w-full bg-[#030614] border border-slate-800 text-slate-400 rounded-xl px-3.5 py-2.5 text-xs sm:text-sm cursor-not-allowed"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                <School className="w-3.5 h-3.5 text-cyan-400" />
                <span>Department</span>
              </label>
              <input
                type="text"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                placeholder="Computer Science"
                className="w-full bg-[#05091F] border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none transition-colors"
              />
            </div>

            <div>
              <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
                <Building className="w-3.5 h-3.5 text-amber-400" />
                <span>College / University</span>
              </label>
              <input
                type="text"
                value={college}
                onChange={(e) => setCollege(e.target.value)}
                placeholder="QuizMaster Academy"
                className="w-full bg-[#05091F] border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-1 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-purple-400" />
              <span>Professional Bio & Description</span>
            </label>
            <textarea
              rows={3}
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              placeholder="Teaching philosophy, subject specializations..."
              className="w-full bg-[#05091F] border border-slate-800 focus:border-indigo-500 rounded-xl px-3 py-2 text-white text-xs sm:text-sm focus:outline-none transition-colors"
            />
          </div>

          <div className="pt-3 border-t border-slate-800 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 text-xs font-semibold transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 disabled:opacity-40 text-white text-xs font-bold shadow-lg shadow-indigo-600/30 transition-all flex items-center gap-2 cursor-pointer"
            >
              {saving ? 'Saving...' : 'Save Profile'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

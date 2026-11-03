import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Lock, Mail, Eye, EyeOff, ArrowRight, X, ExternalLink, User, GraduationCap } from 'lucide-react';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultMode?: 'login' | 'register';
}

export const AuthModal: React.FC<AuthModalProps> = ({ isOpen, onClose, defaultMode = 'login' }) => {
  const [isLogin, setIsLogin] = useState(defaultMode === 'login');
  const [selectedRole, setSelectedRole] = useState<'teacher' | 'student'>('teacher');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [department, setDepartment] = useState('Computer Science');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login, register, loginWithGoogle } = useAuth();

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (selectedRole === 'student') {
      setError('Student accounts must use the QuizMaster Android app to access quizzes and streak tracking.');
      return;
    }

    setLoading(true);

    if (isLogin) {
      const res = await login(email, password);
      if (res.success) {
        onClose();
      } else {
        setError(res.error || 'Invalid email or password. Please try again.');
      }
    } else {
      if (!name.trim()) {
        setError('Please enter your full name');
        setLoading(false);
        return;
      }
      const res = await register(name, email, password, department);
      if (res.success) {
        onClose();
      } else {
        setError(res.error || 'Failed to create teacher account');
      }
    }
    setLoading(false);
  };

  const handleGoogle = async () => {
    setError('');
    setLoading(true);
    const res = await loginWithGoogle();
    if (res.success) {
      onClose();
    } else {
      setError(res.error || 'Google Sign-in failed. Please verify popup permissions.');
    }
    setLoading(false);
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-md bg-[#080E24] border border-[#16214C] rounded-[30px] p-6 sm:p-8 shadow-2xl relative overflow-hidden"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -12px rgba(124, 58, 237, 0.35)'
        }}
      >
        {/* Android Ambient Glow Halo */}
        <div className="absolute -top-24 -right-24 w-56 h-56 bg-indigo-600/25 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-24 -left-24 w-56 h-56 bg-purple-600/25 rounded-full blur-3xl pointer-events-none" />

        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 text-slate-400 hover:text-white p-2 rounded-full hover:bg-slate-800 transition-colors"
          title="Close dialog"
        >
          <X className="w-4 h-4" />
        </button>

        {/* Floating Glowing Badge matching Android AuthHeaderBadge */}
        <div className="flex justify-center mb-4">
          <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-[#0F173C] border border-[#1E2858] shadow-inner">
            <span className="text-base">👨‍🏫</span>
            <span className="text-xs font-bold text-white tracking-wider">
              {isLogin ? 'TEACHER AUTHENTICATION' : 'NEW INSTRUCTOR PORTAL'}
            </span>
          </div>
        </div>

        {/* Welcome Multi-Colored Title matching Android LoginScreen.kt */}
        <div className="text-center mb-5">
          <h3 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            <span className="text-white">{isLogin ? 'Welcome ' : 'Teacher '}</span>
            <span className="text-[#8B5CF6]">{isLogin ? 'Back' : 'Registration'}</span>
          </h3>
          <p className="text-xs text-slate-400 mt-1.5 max-w-xs mx-auto">
            {isLogin 
              ? 'Sign in with Teacher credentials to manage quizzes & students' 
              : 'Create an authorized instructor account to publish quizzes & batches'}
          </p>
        </div>

        {/* Role Selector Tab matching Android AuthRoleSelector */}
        <div className="grid grid-cols-2 p-1 bg-[#05091C] border border-[#16214C] rounded-2xl mb-5">
          <button
            type="button"
            onClick={() => setSelectedRole('teacher')}
            className={`flex items-center justify-center gap-2 py-2 rounded-xl text-xs font-bold transition-all ${
              selectedRole === 'teacher'
                ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <span>👨‍🏫 Teacher</span>
          </button>
          <button
            type="button"
            onClick={() => setSelectedRole('student')}
            className={`flex items-center justify-center gap-2 py-2 rounded-xl text-xs font-bold transition-all ${
              selectedRole === 'student'
                ? 'bg-slate-800 text-slate-200 border border-slate-700'
                : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            <GraduationCap className="w-3.5 h-3.5" />
            <span>Student</span>
          </button>
        </div>

        {/* Notice if Student is selected */}
        {selectedRole === 'student' && (
          <div className="mb-5 p-3.5 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs">
            <p className="font-semibold">Looking for Student Quizzes?</p>
            <p className="text-[11px] text-amber-200/80 mt-1">
              Student test taking, leaderboards, and streak tracking are managed directly within the QuizMaster Android APK.
            </p>
            <a
              href="https://github.com/Deb-Kumar/QuizMaster/releases"
              target="_blank"
              rel="noreferrer"
              className="mt-2 inline-flex items-center gap-1.5 text-xs font-bold text-amber-400 hover:underline"
            >
              <span>Download Android APK</span>
              <ExternalLink className="w-3 h-3" />
            </a>
          </div>
        )}

        {/* Error message */}
        {error && (
          <div className="mb-4 p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-300 text-xs font-medium">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3.5">
          {!isLogin && (
            <div>
              <label className="block text-[11px] font-bold text-slate-300 uppercase tracking-wider mb-1">
                Full Name *
              </label>
              <div className="relative flex items-center">
                <User className="w-4 h-4 text-slate-400 absolute left-3 pointer-events-none" />
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Prof. Alex Rivera"
                  className="w-full bg-[#05091C] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl pl-9 pr-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-[11px] font-bold text-slate-300 uppercase tracking-wider mb-1">
              Email Address *
            </label>
            <div className="relative flex items-center">
              <Mail className="w-4 h-4 text-slate-400 absolute left-3 pointer-events-none" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="teacher@institution.edu"
                className="w-full bg-[#05091C] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl pl-9 pr-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-bold text-slate-300 uppercase tracking-wider mb-1">
              Password *
            </label>
            <div className="relative flex items-center">
              <Lock className="w-4 h-4 text-slate-400 absolute left-3 pointer-events-none" />
              <input
                type={showPassword ? 'text' : 'password'}
                required
                maxLength={16}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-[#05091C] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl pl-9 pr-10 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 text-slate-400 hover:text-white"
                title={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {!isLogin && (
            <div>
              <label className="block text-[11px] font-bold text-slate-300 uppercase tracking-wider mb-1">
                Department / Subject
              </label>
              <input
                type="text"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                placeholder="Computer Science, Mathematics, Physics"
                className="w-full bg-[#05091C] border border-[#1B2E6C] focus:border-indigo-500 rounded-xl px-4 py-2.5 text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none transition-colors"
              />
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-gradient-to-r from-[#7C3AED] to-[#3B82F6] hover:from-[#6D28D9] hover:to-[#2563EB] disabled:opacity-50 text-white font-bold rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/30 active:scale-[0.98] transition-all text-xs sm:text-sm mt-3 cursor-pointer"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            ) : (
              <>
                <span>{isLogin ? 'Sign In as Teacher' : 'Register as Teacher'}</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* Divider */}
        <div className="relative my-4 text-center">
          <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-slate-800" /></div>
          <span className="relative px-3 bg-[#080E24] text-slate-500 text-[10px] uppercase tracking-wider font-bold">
            Or continue with
          </span>
        </div>

        {/* Google Sign In */}
        <button
          type="button"
          onClick={handleGoogle}
          disabled={loading}
          className="w-full py-2.5 bg-slate-900/90 hover:bg-slate-800 border border-slate-700/80 rounded-xl text-white text-xs font-semibold flex items-center justify-center gap-2.5 transition-colors active:scale-[0.98] cursor-pointer"
        >
          <svg className="w-4 h-4" viewBox="0 0 24 24">
            <path fill="#EA4335" d="M12 5c1.6 0 3 .6 4.1 1.6l3.1-3.1C17.3 1.7 14.8 1 12 1 7.5 1 3.7 3.6 1.9 7.3l3.7 2.9C6.5 7.3 9 5 12 5z" />
            <path fill="#4285F4" d="M23.5 12.3c0-.8-.1-1.6-.2-2.3H12v4.5h6.5c-.3 1.5-1.1 2.8-2.4 3.7l3.7 2.9c2.2-2 3.7-5 3.7-8.8z" />
            <path fill="#FBBC05" d="M5.6 14.8c-.2-.7-.4-1.5-.4-2.3s.2-1.6.4-2.3L1.9 7.3C.7 9.7 0 12.3 0 15s.7 5.3 1.9 7.7l3.7-2.9z" />
            <path fill="#34A853" d="M12 23c3.2 0 6-1.1 8-3l-3.7-2.9c-1.1.7-2.5 1.2-4.3 1.2-3 0-5.5-2-6.4-4.8L1.9 16.4C3.7 20.4 7.5 23 12 23z" />
          </svg>
          <span>Continue with Google Account</span>
        </button>

        {/* Toggle between Login and Register */}
        <div className="mt-5 text-center text-xs text-slate-400">
          {isLogin ? (
            <>
              Don't have a teacher account?{' '}
              <button
                type="button"
                onClick={() => { setIsLogin(false); setError(''); }}
                className="text-indigo-400 hover:text-indigo-300 font-bold underline cursor-pointer"
              >
                Register here
              </button>
            </>
          ) : (
            <>
              Already have a teacher account?{' '}
              <button
                type="button"
                onClick={() => { setIsLogin(true); setError(''); }}
                className="text-indigo-400 hover:text-indigo-300 font-bold underline cursor-pointer"
              >
                Sign In
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

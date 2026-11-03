import React from 'react';
import { Info, X, Check, Smartphone, ShieldCheck, UserCheck, ExternalLink } from 'lucide-react';

interface AboutModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AboutModal: React.FC<AboutModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-lg bg-[#0F172A] border border-[#1E293B] rounded-3xl p-6 sm:p-7 shadow-2xl relative overflow-hidden max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 50px -12px rgba(99, 102, 241, 0.25)'
        }}
      >
        {/* Android Ambient Accent Halo */}
        <div className="absolute -top-24 -right-24 w-48 h-48 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-24 -left-24 w-48 h-48 bg-purple-600/20 rounded-full blur-3xl pointer-events-none" />

        {/* Header Row */}
        <div className="flex items-center justify-between pb-4 border-b border-[#1E293B]">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-full bg-gradient-to-tr from-[#6366F1] to-[#4F46E5] flex items-center justify-center shadow-lg shadow-indigo-500/25">
              <Info className="w-5 h-5 text-white" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">About Us</h3>
              <p className="text-xs text-slate-400">QuizMaster • Version 5.0.2</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-[#1E293B] hover:bg-[#334155] text-slate-400 hover:text-white flex items-center justify-center transition-colors"
            title="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="mt-5 space-y-5">
          {/* Section 1: Mission */}
          <div>
            <span className="text-[11px] font-bold text-indigo-400 tracking-wider uppercase">
              Our Mission
            </span>
            <div className="mt-2 bg-[#1E293B]/65 border border-[#334155]/50 rounded-2xl p-4 text-xs sm:text-sm text-slate-200 leading-relaxed">
              QuizMaster empowers students and educators with an intelligent, engaging platform for testing knowledge, building streaks, and tracking academic progress in real-time.
            </div>
          </div>

          {/* Section 2: Key Features */}
          <div>
            <span className="text-[11px] font-bold text-indigo-400 tracking-wider uppercase">
              Key Features
            </span>
            <div className="mt-2 bg-[#1E293B]/65 border border-[#334155]/50 rounded-2xl divide-y divide-[#334155]/40 overflow-hidden">
              <div className="p-3.5 flex items-start gap-3">
                <span className="text-base">🎯</span>
                <div>
                  <h4 className="text-xs font-semibold text-white">Multi-Department Quizzes</h4>
                  <p className="text-[11px] text-slate-400 mt-0.5">Hundreds of verified subjects across engineering, sciences, arts & medicine</p>
                </div>
              </div>

              <div className="p-3.5 flex items-start gap-3">
                <span className="text-base">⚡</span>
                <div>
                  <h4 className="text-xs font-semibold text-white">Auto-Save & Resume</h4>
                  <p className="text-[11px] text-slate-400 mt-0.5">Never lose progress if the app is closed during an active practice test</p>
                </div>
              </div>

              <div className="p-3.5 flex items-start gap-3">
                <span className="text-base">🏆</span>
                <div>
                  <h4 className="text-xs font-semibold text-white">Streaks & Leaderboards</h4>
                  <p className="text-[11px] text-slate-400 mt-0.5">Compete with peers and celebrate achievements every day</p>
                </div>
              </div>

              <div className="p-3.5 flex items-start gap-3">
                <span className="text-base">🌓</span>
                <div>
                  <h4 className="text-xs font-semibold text-white">Adaptive Appearance</h4>
                  <p className="text-[11px] text-slate-400 mt-0.5">Custom-crafted light and cyberpunk dark modes for comfortable study</p>
                </div>
              </div>
            </div>
          </div>

          {/* Section 3: App Specifications */}
          <div>
            <span className="text-[11px] font-bold text-indigo-400 tracking-wider uppercase">
              App Specifications
            </span>
            <div className="mt-2 bg-[#1E293B]/65 border border-[#334155]/50 rounded-2xl divide-y divide-[#334155]/40 overflow-hidden text-xs">
              <div className="p-3 flex items-center justify-between">
                <div className="flex items-center gap-2.5 text-slate-400">
                  <Check className="w-4 h-4 text-indigo-400" />
                  <span>Version</span>
                </div>
                <span className="font-semibold text-white">5.0.2 (Release Build)</span>
              </div>

              <div className="p-3 flex items-center justify-between">
                <div className="flex items-center gap-2.5 text-slate-400">
                  <Smartphone className="w-4 h-4 text-emerald-400" />
                  <span>Platform</span>
                </div>
                <span className="font-semibold text-white">Android Jetpack Compose & Web Teacher Portal</span>
              </div>

              <div className="p-3 flex items-center justify-between">
                <div className="flex items-center gap-2.5 text-slate-400">
                  <ShieldCheck className="w-4 h-4 text-blue-400" />
                  <span>Security</span>
                </div>
                <span className="font-semibold text-white">Firebase & Encrypted Local Session</span>
              </div>

              <div className="p-3 flex items-center justify-between">
                <div className="flex items-center gap-2.5 text-slate-400">
                  <UserCheck className="w-4 h-4 text-purple-400" />
                  <span>Developer & Lead Architect</span>
                </div>
                <a 
                  href="https://github.com/Deb-Kumar" 
                  target="_blank" 
                  rel="noreferrer"
                  className="font-semibold text-indigo-300 hover:text-indigo-200 flex items-center gap-1"
                >
                  <span>Deb Kumar</span>
                  <ExternalLink className="w-3 h-3" />
                </a>
              </div>
            </div>
          </div>
        </div>

        {/* Action Button */}
        <div className="mt-6 pt-2">
          <button
            onClick={onClose}
            className="w-full py-3 rounded-2xl bg-[#6366F1] hover:bg-[#4F46E5] text-white font-bold text-sm shadow-lg shadow-indigo-600/30 transition-all cursor-pointer"
          >
            Got It
          </button>
        </div>
      </div>
    </div>
  );
};

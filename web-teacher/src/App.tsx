import React, { useState } from 'react';
import { CertificateVerifier } from './components/verification/CertificateVerifier';
import { QuizStudio } from './components/studio/QuizStudio';
import { BatchManager } from './components/batches/BatchManager';
import { ShieldCheck, Users, Award, BookOpen, ExternalLink } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'verify' | 'studio' | 'batches'>('verify');

  return (
    <div className="min-h-screen bg-[#070913] text-slate-100 flex flex-col selection:bg-indigo-500/30 selection:text-indigo-200">
      {/* Top Android Material 3 Navigation Bar */}
      <header className="sticky top-0 z-40 bg-[#070913]/80 backdrop-blur-xl border-b border-slate-800/80 px-4 lg:px-8 py-3.5 transition-all">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          {/* Logo & Brand */}
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 p-0.5 shadow-lg shadow-indigo-500/20 flex items-center justify-center">
              <div className="w-full h-full bg-[#070913] rounded-[14px] flex items-center justify-center">
                <Award className="w-5 h-5 text-indigo-400" />
              </div>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-base md:text-lg tracking-tight text-white">QuizMaster</span>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-indigo-500/15 text-indigo-300 border border-indigo-500/30 uppercase tracking-widest">
                  Teacher & Web
                </span>
              </div>
              <p className="text-[11px] text-slate-400 hidden sm:block">Unified Assessment & Credential Engine</p>
            </div>
          </div>

          {/* Navigation Tabs (Android Segmented Pill Bar) */}
          <nav className="flex items-center bg-slate-900/90 border border-slate-800 p-1 rounded-2xl shadow-inner">
            <button
              onClick={() => setActiveTab('verify')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs md:text-sm font-semibold transition-all duration-200 ${
                activeTab === 'verify'
                  ? 'bg-gradient-to-r from-indigo-600 to-cyan-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <ShieldCheck className="w-4 h-4" />
              <span>Verify Certificate</span>
            </button>

            <button
              onClick={() => setActiveTab('studio')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs md:text-sm font-semibold transition-all duration-200 ${
                activeTab === 'studio'
                  ? 'bg-gradient-to-r from-indigo-600 to-cyan-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <BookOpen className="w-4 h-4" />
              <span>Quiz Studio</span>
            </button>

            <button
              onClick={() => setActiveTab('batches')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs md:text-sm font-semibold transition-all duration-200 ${
                activeTab === 'batches'
                  ? 'bg-gradient-to-r from-indigo-600 to-cyan-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <Users className="w-4 h-4" />
              <span>Batches</span>
            </button>
          </nav>

          {/* Right Action: Mobile APK / Live Link */}
          <div className="hidden lg:flex items-center gap-3">
            <a
              href="https://github.com/Deb-Kumar/QuizMaster/releases"
              target="_blank"
              rel="noreferrer"
              className="px-3.5 py-1.5 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 text-xs font-semibold flex items-center gap-1.5 transition-colors"
            >
              <span>Download Android APK</span>
              <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
            </a>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1">
        {activeTab === 'verify' && <CertificateVerifier />}
        {activeTab === 'studio' && <QuizStudio />}
        {activeTab === 'batches' && <BatchManager />}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-900 py-6 px-4 text-center text-xs text-slate-500">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
          <p>© 2026 QuizMaster Academy • Enterprise Educational Platform</p>
          <div className="flex items-center gap-4 text-slate-400">
            <a href="https://quizmaster-dd2f8.web.app" target="_blank" rel="noreferrer" className="hover:text-indigo-400">
              Live Firebase Portal
            </a>
            <span>•</span>
            <a href="https://github.com/Deb-Kumar/QuizMaster" target="_blank" rel="noreferrer" className="hover:text-indigo-400">
              GitHub Repository
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default App;

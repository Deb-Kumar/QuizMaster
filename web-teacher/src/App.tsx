import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { TeacherHome } from './components/home/TeacherHome';
import { QuizStudio } from './components/studio/QuizStudio';
import { BatchManager } from './components/batches/BatchManager';
import { CertificateVerifier } from './components/verification/CertificateVerifier';
import { AuthModal } from './components/auth/AuthModal';
import { AboutModal } from './components/about/AboutModal';
import { TeacherSettingsModal } from './components/profile/TeacherSettingsModal';
import { TeacherProfileModal } from './components/profile/TeacherProfileModal';
import { TeacherAnalyticsModal } from './components/analytics/TeacherAnalyticsModal';
import { TeacherNotificationsModal } from './components/notifications/TeacherNotificationsModal';
import { 
  ShieldCheck, 
  Users, 
  BookOpen, 
  LayoutDashboard, 
  Info, 
  Bell, 
  LogOut, 
  ExternalLink,
  ChevronDown,
  Key,
  BarChart3,
  User
} from 'lucide-react';

const TeacherAppContent: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'home' | 'studio' | 'batches' | 'verify'>('home');
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [showAboutModal, setShowAboutModal] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [showAnalyticsModal, setShowAnalyticsModal] = useState(false);
  const [showNotificationsModal, setShowNotificationsModal] = useState(false);
  const [showProfileDropdown, setShowProfileDropdown] = useState(false);
  const [authDefaultMode, setAuthDefaultMode] = useState<'login' | 'register'>('login');

  const { currentUser, logout } = useAuth();

  const handleOpenAuth = (mode: 'login' | 'register' = 'login') => {
    setAuthDefaultMode(mode);
    setShowAuthModal(true);
  };

  return (
    <div className="min-h-screen bg-[#070913] text-slate-100 flex flex-col selection:bg-indigo-500/30 selection:text-indigo-200">
      
      {/* Top Android Material 3 Navigation Bar matching TeacherHomeContent.kt */}
      <header className="sticky top-0 z-40 bg-[#070913]/90 backdrop-blur-xl border-b border-slate-800/80 px-3 sm:px-6 lg:px-8 py-2.5 sm:py-3 transition-all">
        <div className="max-w-7xl mx-auto flex items-center justify-between gap-2 sm:gap-4">
          
          {/* Logo & Brand using official app_logo.png */}
          <div 
            onClick={() => setActiveTab('home')}
            className="flex items-center gap-2.5 sm:gap-3 cursor-pointer select-none"
          >
            <div className="w-9 h-9 sm:w-10 sm:h-10 rounded-2xl bg-gradient-to-tr from-[#7C3AED] via-indigo-500 to-[#3B82F6] p-0.5 shadow-lg shadow-indigo-500/25 flex items-center justify-center shrink-0">
              <div className="w-full h-full bg-[#070913] rounded-[14px] flex items-center justify-center p-1">
                <img 
                  src="/app_logo.png" 
                  alt="QuizMaster Logo" 
                  className="w-full h-full object-contain filter drop-shadow" 
                />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <span className="font-black text-lg sm:text-xl tracking-tight text-white">QuizMaster</span>
              {/* Teacher Badge matching Android TeacherTheme.kt */}
              <span className="px-2 sm:px-2.5 py-0.5 rounded-full text-[10px] sm:text-[11px] font-bold bg-[#0F173C] text-white border border-[#1E2858] shadow-sm shrink-0">
                👨‍🏫 Teacher
              </span>
            </div>
          </div>

          {/* Navigation Tabs (Android Segmented Pill Bar - Desktop) */}
          <nav className="hidden md:flex items-center bg-slate-900/90 border border-slate-800/90 p-1 rounded-2xl shadow-inner">
            <button
              onClick={() => setActiveTab('home')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all duration-200 cursor-pointer ${
                activeTab === 'home'
                  ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Dashboard</span>
            </button>

            <button
              onClick={() => setActiveTab('studio')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all duration-200 cursor-pointer ${
                activeTab === 'studio'
                  ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <BookOpen className="w-4 h-4" />
              <span>Quiz Studio</span>
            </button>

            <button
              onClick={() => setActiveTab('batches')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all duration-200 cursor-pointer ${
                activeTab === 'batches'
                  ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <Users className="w-4 h-4" />
              <span>Batches</span>
            </button>

            <button
              onClick={() => setActiveTab('verify')}
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all duration-200 cursor-pointer ${
                activeTab === 'verify'
                  ? 'bg-gradient-to-r from-indigo-600 to-blue-600 text-white shadow-md shadow-indigo-600/30'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <ShieldCheck className="w-4 h-4" />
              <span>Verify Certificate</span>
            </button>
          </nav>

          {/* Right Header Actions */}
          <div className="flex items-center gap-2 sm:gap-3">
            
            {/* Android APK Link (Desktop) */}
            <a
              href="https://github.com/Deb-Kumar/QuizMaster/releases"
              target="_blank"
              rel="noreferrer"
              className="hidden lg:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-900/90 border border-slate-800 hover:border-slate-700 text-slate-300 text-xs font-semibold transition-colors"
              title="Download Quiz-Master.apk"
            >
              <span>Android APK</span>
              <ExternalLink className="w-3 h-3 text-slate-400" />
            </a>

            {/* If logged in: Notification Bell + Teacher Profile Dropdown */}
            {currentUser ? (
              <div className="flex items-center gap-2">
                {/* Notification Bell */}
                <button
                  onClick={() => setShowNotificationsModal(true)}
                  className="relative p-2 rounded-xl bg-slate-900/90 border border-slate-800 hover:border-slate-700 text-slate-400 hover:text-white transition-colors cursor-pointer shrink-0"
                  title="Notifications"
                >
                  <Bell className="w-4 h-4" />
                  <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 animate-pulse" />
                </button>

                {/* Profile Pill & Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setShowProfileDropdown(!showProfileDropdown)}
                    className="flex items-center gap-2 pl-1.5 sm:pl-2 pr-2.5 py-1.5 rounded-xl bg-slate-900/90 border border-slate-800 hover:border-slate-700 text-slate-200 transition-colors cursor-pointer"
                  >
                    <div className="w-6 h-6 rounded-lg bg-gradient-to-tr from-indigo-600 to-blue-600 flex items-center justify-center text-xs font-bold text-white shrink-0">
                      {currentUser.name ? currentUser.name.charAt(0).toUpperCase() : 'T'}
                    </div>
                    <span className="text-xs font-semibold max-w-[90px] sm:max-w-[120px] truncate hidden sm:inline">
                      {currentUser.name || 'Instructor'}
                    </span>
                    <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                  </button>

                  {/* Dropdown Menu */}
                  {showProfileDropdown && (
                    <div 
                      className="absolute right-0 mt-2 w-60 bg-[#0F172A] border border-[#1E293B] rounded-2xl p-2 shadow-2xl z-50 animate-in fade-in zoom-in-95 duration-150"
                      onClick={() => setShowProfileDropdown(false)}
                    >
                      <div className="px-3 py-2.5 border-b border-slate-800">
                        <p className="text-xs font-bold text-white truncate">{currentUser.name}</p>
                        <p className="text-[11px] text-slate-400 truncate">{currentUser.email}</p>
                        <span className="mt-1.5 inline-block text-[10px] font-semibold text-indigo-400 bg-indigo-500/15 border border-indigo-500/25 px-2 py-0.5 rounded-full">
                          {currentUser.department || 'Computer Science'}
                        </span>
                      </div>

                      <div className="py-1">
                        <button
                          onClick={() => { setActiveTab('home'); }}
                          className="w-full text-left px-3 py-2 text-xs text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-xl flex items-center gap-2 transition-colors cursor-pointer"
                        >
                          <LayoutDashboard className="w-4 h-4 text-indigo-400" />
                          <span>Teacher Dashboard</span>
                        </button>

                        <button
                          onClick={() => setShowProfileModal(true)}
                          className="w-full text-left px-3 py-2 text-xs text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-xl flex items-center gap-2 transition-colors cursor-pointer"
                        >
                          <User className="w-4 h-4 text-cyan-400" />
                          <span>Instructor Profile</span>
                        </button>

                        <button
                          onClick={() => setShowAnalyticsModal(true)}
                          className="w-full text-left px-3 py-2 text-xs text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-xl flex items-center gap-2 transition-colors cursor-pointer"
                        >
                          <BarChart3 className="w-4 h-4 text-emerald-400" />
                          <span>Classroom Analytics</span>
                        </button>

                        <button
                          onClick={() => setShowSettingsModal(true)}
                          className="w-full text-left px-3 py-2 text-xs text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-xl flex items-center gap-2 transition-colors cursor-pointer"
                        >
                          <Key className="w-4 h-4 text-purple-400" />
                          <span>AI API Keys & Models</span>
                        </button>

                        <button
                          onClick={() => setShowAboutModal(true)}
                          className="w-full text-left px-3 py-2 text-xs text-slate-300 hover:text-white hover:bg-slate-800/60 rounded-xl flex items-center gap-2 transition-colors cursor-pointer"
                        >
                          <Info className="w-4 h-4 text-blue-400" />
                          <span>About QuizMaster</span>
                        </button>
                      </div>

                      <div className="pt-1 border-t border-slate-800">
                        <button
                          onClick={() => logout()}
                          className="w-full text-left px-3 py-2 text-xs text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded-xl flex items-center gap-2 transition-colors cursor-pointer"
                        >
                          <LogOut className="w-4 h-4" />
                          <span>Sign Out</span>
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ) : (
              /* If not logged in: Clean Sign In & Register */
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleOpenAuth('login')}
                  className="px-3.5 py-1.5 rounded-xl text-xs font-semibold text-slate-300 hover:text-white hover:bg-slate-800/60 transition-colors cursor-pointer"
                >
                  Sign In
                </button>
                <button
                  onClick={() => handleOpenAuth('register')}
                  className="px-4 py-1.5 rounded-xl bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 text-white font-bold text-xs shadow-md shadow-indigo-600/30 transition-all cursor-pointer whitespace-nowrap"
                >
                  Register
                </button>
              </div>
            )}

          </div>
        </div>
      </header>

      {/* Main Content Area with safe bottom padding for mobile navigation */}
      <main className="flex-1 pb-24 md:pb-8">
        {activeTab === 'home' && (
          <TeacherHome 
            onNavigateTab={setActiveTab}
            onOpenAbout={() => setShowAboutModal(true)}
            onOpenAuth={() => handleOpenAuth('login')}
          />
        )}
        {activeTab === 'studio' && (
          <QuizStudio 
            onNavigateHome={() => setActiveTab('home')}
          />
        )}
        {activeTab === 'batches' && <BatchManager />}
        {activeTab === 'verify' && <CertificateVerifier />}
      </main>

      {/* Android Native Material 3 Bottom Navigation Bar for Mobile matching TeacherBottomNavBar.kt */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#070913]/95 backdrop-blur-2xl border-t border-slate-800/90 py-2 px-3 flex md:hidden items-center justify-around shadow-2xl safe-area-pb">
        <button
          onClick={() => {
            setActiveTab('home');
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }}
          className={`flex flex-col items-center gap-1 py-1 px-3 rounded-xl transition-all cursor-pointer ${
            activeTab === 'home' ? 'text-indigo-400 font-bold scale-105' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <LayoutDashboard className="w-5 h-5" />
          <span className="text-[10px]">Dashboard</span>
        </button>

        <button
          onClick={() => {
            setActiveTab('studio');
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }}
          className={`flex flex-col items-center gap-1 py-1 px-3 rounded-xl transition-all cursor-pointer ${
            activeTab === 'studio' ? 'text-indigo-400 font-bold scale-105' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <BookOpen className="w-5 h-5" />
          <span className="text-[10px]">Studio</span>
        </button>

        <button
          onClick={() => {
            setActiveTab('batches');
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }}
          className={`flex flex-col items-center gap-1 py-1 px-3 rounded-xl transition-all cursor-pointer ${
            activeTab === 'batches' ? 'text-indigo-400 font-bold scale-105' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Users className="w-5 h-5" />
          <span className="text-[10px]">Batches</span>
        </button>

        <button
          onClick={() => {
            setActiveTab('verify');
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }}
          className={`flex flex-col items-center gap-1 py-1 px-3 rounded-xl transition-all cursor-pointer ${
            activeTab === 'verify' ? 'text-indigo-400 font-bold scale-105' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <ShieldCheck className="w-5 h-5" />
          <span className="text-[10px]">Verify</span>
        </button>
      </nav>

      {/* Footer matching Android About Specifications */}
      <footer className="border-t border-slate-900 py-6 px-4 text-center text-xs text-slate-500 mb-14 md:mb-0">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
          <p>© 2026 QuizMaster Academy • Enterprise Educational Platform</p>
          <div className="flex flex-wrap items-center justify-center gap-x-3 gap-y-1 text-slate-400 text-[11px] sm:text-xs">
            <button onClick={() => setShowAboutModal(true)} className="hover:text-indigo-400 cursor-pointer">
              About QuizMaster v5.0.2
            </button>
            <span>•</span>
            <button onClick={() => setShowSettingsModal(true)} className="hover:text-indigo-400 cursor-pointer">
              AI Keys
            </button>
            <span>•</span>
            <button onClick={() => setShowAnalyticsModal(true)} className="hover:text-indigo-400 cursor-pointer">
              Analytics
            </button>
            <span>•</span>
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

      {/* Modals */}
      <AuthModal 
        isOpen={showAuthModal} 
        onClose={() => setShowAuthModal(false)}
        defaultMode={authDefaultMode}
      />

      <AboutModal 
        isOpen={showAboutModal} 
        onClose={() => setShowAboutModal(false)} 
      />

      <TeacherSettingsModal
        isOpen={showSettingsModal}
        onClose={() => setShowSettingsModal(false)}
      />

      <TeacherProfileModal
        isOpen={showProfileModal}
        onClose={() => setShowProfileModal(false)}
      />

      <TeacherAnalyticsModal
        isOpen={showAnalyticsModal}
        onClose={() => setShowAnalyticsModal(false)}
      />

      <TeacherNotificationsModal
        isOpen={showNotificationsModal}
        onClose={() => setShowNotificationsModal(false)}
        onNavigateBatches={() => setActiveTab('batches')}
      />

    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <TeacherAppContent />
    </AuthProvider>
  );
};

export default App;

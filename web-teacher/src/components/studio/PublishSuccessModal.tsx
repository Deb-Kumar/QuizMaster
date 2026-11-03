import React, { useState, useEffect } from 'react';
import confetti from 'canvas-confetti';
import { Award, Copy, Check, ArrowRight, Sparkles } from 'lucide-react';

interface PublishSuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  quizCode: string;
  quizTitle: string;
  onGoToDashboard: () => void;
  onCreateAnother: () => void;
}

export const PublishSuccessModal: React.FC<PublishSuccessModalProps> = ({
  isOpen,
  onClose,
  quizCode,
  quizTitle,
  onGoToDashboard,
  onCreateAnother
}) => {
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (isOpen) {
      confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.6 }
      });
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleCopyCode = () => {
    navigator.clipboard.writeText(quizCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-md bg-[#0A102E] border border-emerald-500/40 rounded-[30px] p-6 sm:p-8 shadow-2xl relative text-center overflow-hidden"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -15px rgba(16, 185, 129, 0.3)'
        }}
      >
        {/* Glow ambient background */}
        <div className="absolute -top-20 -right-20 w-48 h-48 bg-emerald-500/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-20 -left-20 w-48 h-48 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />

        <div className="w-16 h-16 mx-auto mb-4 rounded-3xl bg-gradient-to-tr from-emerald-500 to-cyan-500 p-0.5 shadow-xl shadow-emerald-500/25 flex items-center justify-center">
          <div className="w-full h-full bg-[#0A102E] rounded-[22px] flex items-center justify-center">
            <Award className="w-8 h-8 text-emerald-400" />
          </div>
        </div>

        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs font-bold mb-2">
          <Sparkles className="w-3.5 h-3.5" />
          <span>QUIZ PUBLISHED LIVE</span>
        </div>

        <h3 className="text-xl sm:text-2xl font-extrabold text-white tracking-tight">
          Assessment is Ready!
        </h3>
        <p className="text-xs text-slate-300 mt-1 max-w-xs mx-auto line-clamp-1">
          "{quizTitle}"
        </p>

        {/* Big Code Card */}
        <div className="my-6 p-4 rounded-2xl bg-[#050B22] border border-slate-800">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">
            Student Access Code
          </span>
          <div className="text-3xl sm:text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 via-cyan-300 to-indigo-400 tracking-widest font-mono">
            {quizCode}
          </div>
          <p className="text-[11px] text-slate-400 mt-2">
            Share this 6-character code with your students to take the exam on Android or Web.
          </p>

          <button
            onClick={handleCopyCode}
            className="mt-3 w-full py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-white text-xs font-bold flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            {copied ? (
              <>
                <Check className="w-4 h-4 text-emerald-400" />
                <span className="text-emerald-300">Access Code Copied!</span>
              </>
            ) : (
              <>
                <Copy className="w-4 h-4 text-slate-400" />
                <span>Copy Access Code</span>
              </>
            )}
          </button>
        </div>

        {/* Buttons */}
        <div className="space-y-2.5">
          <button
            onClick={onGoToDashboard}
            className="w-full py-3 rounded-2xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold text-xs sm:text-sm shadow-lg shadow-emerald-600/30 transition-all flex items-center justify-center gap-2 cursor-pointer"
          >
            <span>View in Teacher Dashboard</span>
            <ArrowRight className="w-4 h-4" />
          </button>

          <button
            onClick={onCreateAnother}
            className="w-full py-2.5 rounded-xl bg-slate-900/90 hover:bg-slate-800 text-slate-300 text-xs font-semibold transition-colors cursor-pointer"
          >
            Create Another Quiz
          </button>
        </div>
      </div>
    </div>
  );
};

import React, { useState } from 'react';
import type { Question } from '../../types';
import { parseBulkQuizText } from '../../utils/bulkQuestionParser';
import { FileText, CheckCircle2, AlertCircle, X, Sparkles, Copy } from 'lucide-react';

interface BulkImportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onImport: (questions: Question[]) => void;
}

const SAMPLE_QUIZ_FORMAT = `1. What is the primary purpose of Jetpack Compose in Android?
A) Declarative modern UI toolkit
B) Relational database management system
C) Network proxy server
D) File compression algorithm
Answer: A
Explanation: Jetpack Compose is Google's recommended modern toolkit for building native Android UI declaratively.
Hint: Think about user interface rendering.

2. Which hook in React manages stateful values between re-renders?
A) useEffect
B) useState
C) useContext
D) useMemo
Answer: B
Explanation: useState allows functional React components to persist and trigger reactive state updates.
Hint: Think state initialization.`;

export const BulkImportModal: React.FC<BulkImportModalProps> = ({ isOpen, onClose, onImport }) => {
  const [text, setText] = useState('');
  const [previewList, setPreviewList] = useState<Question[]>([]);

  if (!isOpen) return null;

  const handleTextChange = (val: string) => {
    setText(val);
    const parsed = parseBulkQuizText(val);
    setPreviewList(parsed);
  };

  const handleLoadSample = () => {
    handleTextChange(SAMPLE_QUIZ_FORMAT);
  };

  const handleConfirmImport = () => {
    if (previewList.length === 0) return;
    onImport(previewList);
    onClose();
  };

  return (
    <div 
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/85 backdrop-blur-md animate-in fade-in duration-200"
      onClick={onClose}
    >
      <div 
        className="w-full max-w-3xl bg-[#0A102E] border border-[#16214C] rounded-3xl p-6 sm:p-8 shadow-2xl relative max-h-[90vh] flex flex-col"
        onClick={(e) => e.stopPropagation()}
        style={{
          boxShadow: '0 25px 60px -15px rgba(56, 189, 248, 0.2)'
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white tracking-tight">Bulk Import Questions</h3>
              <p className="text-xs text-slate-400">Paste questions from Word, Google Docs, CSV, or plain text</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-900 text-slate-400 hover:text-white flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Action bar */}
        <div className="flex items-center justify-between pt-4 pb-2">
          <span className="text-xs font-semibold text-slate-300">
            Paste Formatted Text below:
          </span>
          <button
            type="button"
            onClick={handleLoadSample}
            className="text-xs font-semibold text-cyan-400 hover:text-cyan-300 flex items-center gap-1 cursor-pointer"
          >
            <Copy className="w-3.5 h-3.5" />
            <span>Load Sample Questions</span>
          </button>
        </div>

        {/* Text Area */}
        <textarea
          rows={9}
          value={text}
          onChange={(e) => handleTextChange(e.target.value)}
          placeholder={`Paste your questions here...\nFormat example:\n1. Question Title\nA) Option 1\nB) Option 2\nC) Option 3\nAnswer: A\nExplanation: ...`}
          className="w-full bg-[#050B22] border border-[#16214C] rounded-2xl p-4 text-xs sm:text-sm text-slate-200 font-mono placeholder-slate-600 focus:outline-none focus:border-cyan-500 transition-colors"
        />

        {/* Live Parsing Status */}
        <div className="mt-4 p-3 rounded-xl bg-slate-900/80 border border-slate-800 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2">
            {previewList.length > 0 ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            ) : (
              <AlertCircle className="w-4 h-4 text-amber-400" />
            )}
            <span className={previewList.length > 0 ? 'text-emerald-300 font-semibold' : 'text-slate-400'}>
              {previewList.length > 0 
                ? `Detected ${previewList.length} structured question${previewList.length > 1 ? 's' : ''}` 
                : 'Waiting for formatted text input...'}
            </span>
          </div>

          {previewList.length > 0 && (
            <span className="text-[11px] text-slate-400">
              All options and answer keys verified
            </span>
          )}
        </div>

        {/* Footer buttons */}
        <div className="pt-5 mt-auto flex items-center justify-end gap-3 border-t border-slate-800">
          <button
            onClick={onClose}
            className="px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 text-xs font-semibold transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={handleConfirmImport}
            disabled={previewList.length === 0}
            className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 disabled:opacity-40 text-white text-xs font-bold shadow-lg shadow-cyan-600/25 transition-all flex items-center gap-1.5 cursor-pointer"
          >
            <Sparkles className="w-4 h-4" />
            <span>Import {previewList.length} Questions</span>
          </button>
        </div>
      </div>
    </div>
  );
};

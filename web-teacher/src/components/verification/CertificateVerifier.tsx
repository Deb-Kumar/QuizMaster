import React, { useState, useEffect } from 'react';
import { db } from '../../lib/firebase';
import { collection, query, where, getDocs, doc, getDoc } from 'firebase/firestore';
import type { CertificateRecord } from '../../types';
import { Search, Award, CheckCircle, ShieldCheck, AlertCircle, Copy, Check, ExternalLink, Calendar, User, FileText, ArrowRight } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';
import confetti from 'canvas-confetti';
import { Skeleton } from '../ui/Skeleton';

export const CertificateVerifier: React.FC = () => {
  const [certInput, setCertInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [certificate, setCertificate] = useState<CertificateRecord | null>(null);
  const [copied, setCopied] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Check URL query param or path on mount
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const urlCert = urlParams.get('certId') || urlParams.get('id');
    if (urlCert) {
      setCertInput(urlCert);
      verifyCertificate(urlCert);
    }
  }, []);

  const triggerCelebration = () => {
    try {
      confetti({
        particleCount: 70,
        spread: 60,
        origin: { y: 0.6 },
        colors: ['#6366f1', '#06b6d4', '#10b981', '#f59e0b']
      });
    } catch {
      // safe fallback
    }
  };

  const verifyCertificate = async (idToVerify?: string) => {
    const targetId = (idToVerify || certInput).trim();
    if (!targetId) return;

    setLoading(true);
    setErrorMsg('');
    setSearched(true);
    setCertificate(null);

    try {
      // 1. First try direct document lookup by ID
      const directRef = doc(db, 'certificates', targetId);
      const directSnap = await getDoc(directRef);

      if (directSnap.exists()) {
        const data = directSnap.data();
        const certObj: CertificateRecord = {
          id: directSnap.id,
          certificateId: data.certificateId || data.id || directSnap.id,
          studentId: data.studentId || '',
          studentName: data.studentName || 'Student Recipient',
          quizId: data.quizId || '',
          quizTitle: data.quizTitle || 'Quiz Assessment',
          score: data.score || 0,
          totalQuestions: data.totalQuestions || 0,
          percentage: data.percentage || data.scorePercent || 0,
          issuedAt: data.issuedAt || data.timestamp || Date.now(),
          grade: data.grade || 'A',
          verificationUrl: data.verificationUrl || `https://quizmaster-dd2f8.web.app/verify/${targetId}`
        };
        setCertificate(certObj);
        triggerCelebration();
        setLoading(false);
        return;
      }

      // 2. Query by certificateId field
      const q = query(collection(db, 'certificates'), where('certificateId', '==', targetId));
      const querySnap = await getDocs(q);

      if (!querySnap.empty) {
        const docData = querySnap.docs[0].data();
        const certObj: CertificateRecord = {
          id: querySnap.docs[0].id,
          certificateId: docData.certificateId || querySnap.docs[0].id,
          studentId: docData.studentId || '',
          studentName: docData.studentName || 'Student Recipient',
          quizId: docData.quizId || '',
          quizTitle: docData.quizTitle || 'Quiz Assessment',
          score: docData.score || 0,
          totalQuestions: docData.totalQuestions || 0,
          percentage: docData.percentage || docData.scorePercent || 0,
          issuedAt: docData.issuedAt || docData.timestamp || Date.now(),
          grade: docData.grade || 'A',
          verificationUrl: docData.verificationUrl || `https://quizmaster-dd2f8.web.app/verify/${targetId}`
        };
        setCertificate(certObj);
        triggerCelebration();
      } else {
        setErrorMsg(`No certificate record found with Identifier: "${targetId}". Please verify the code.`);
      }
    } catch (err: unknown) {
      console.error("Firestore lookup error", err);
      setErrorMsg('Error querying credential database. Check connection or verify identifier format.');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyLink = () => {
    if (!certificate) return;
    const url = `https://quizmaster-dd2f8.web.app/verify/${certificate.certificateId}`;
    navigator.clipboard.writeText(url);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const formatDate = (val: number | string) => {
    if (typeof val === 'string') return val;
    return new Date(val).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <div className="w-full max-w-4xl mx-auto px-4 py-8">
      {/* Header Banner - Android / Material 3 style */}
      <div className="text-center space-y-3 mb-8">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold tracking-wide uppercase">
          <ShieldCheck className="w-4 h-4 text-indigo-400" />
          Cryptographic Certificate Verification
        </div>
        <h1 className="text-3xl md:text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-white via-slate-100 to-indigo-200">
          Verify Academic Credentials
        </h1>
        <p className="text-slate-400 max-w-xl mx-auto text-sm md:text-base">
          Instant public verification portal. No login or registration required. Paste any Certificate ID or scan a QR code to audit authenticity.
        </p>
      </div>

      {/* Input Box with Android Elevation Vibes */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-3 shadow-2xl backdrop-blur-xl mb-10 flex flex-col sm:flex-row gap-2 transition-all hover:border-indigo-500/40 focus-within:border-indigo-500/60 focus-within:ring-2 focus-within:ring-indigo-500/20">
        <div className="relative flex-1 flex items-center pl-3">
          <Search className="w-5 h-5 text-slate-400 mr-2 shrink-0" />
          <input
            type="text"
            value={certInput}
            onChange={(e) => setCertInput(e.target.value.toUpperCase())}
            onKeyDown={(e) => e.key === 'Enter' && verifyCertificate()}
            placeholder="e.g. QM-JS92-20261125-003 or Certificate UUID"
            className="w-full bg-transparent border-none text-white placeholder-slate-500 focus:outline-none font-mono text-sm md:text-base tracking-wider uppercase"
          />
        </div>
        <button
          onClick={() => verifyCertificate()}
          disabled={loading || !certInput.trim()}
          className="px-6 py-3 bg-gradient-to-r from-indigo-600 to-cyan-600 hover:from-indigo-500 hover:to-cyan-500 disabled:opacity-50 text-white font-semibold rounded-xl flex items-center justify-center gap-2 transition-all duration-200 shadow-lg shadow-indigo-600/30 active:scale-95"
        >
          {loading ? (
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <>
              Verify Now
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </div>

      {/* Loading Skeleton */}
      {loading && (
        <div className="space-y-6">
          <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-8 backdrop-blur-xl space-y-5">
            <div className="flex justify-between items-center">
              <Skeleton className="h-7 w-48" />
              <Skeleton className="h-6 w-20 rounded-full" />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4">
              <Skeleton className="h-20 rounded-xl" />
              <Skeleton className="h-20 rounded-xl" />
              <Skeleton className="h-20 rounded-xl" />
              <Skeleton className="h-20 rounded-xl" />
            </div>
          </div>
        </div>
      )}

      {/* Verified Certificate Display (Dual Column Android Dev Style) */}
      {!loading && certificate && (
        <div className="animate-in fade-in zoom-in-95 duration-300 bg-gradient-to-b from-slate-900/90 to-slate-950/90 border border-emerald-500/30 rounded-3xl p-6 md:p-8 shadow-2xl backdrop-blur-2xl relative overflow-hidden">
          {/* Glowing Aura Effect */}
          <div className="absolute top-0 right-0 w-80 h-80 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute bottom-0 left-0 w-80 h-80 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

          {/* Verification Badge */}
          <div className="flex flex-wrap items-center justify-between gap-3 pb-6 border-b border-slate-800">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400 shadow-inner">
                <CheckCircle className="w-6 h-6" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-xl font-bold text-white">Authentic Credential Verified</h3>
                  <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/40">
                    Live Valid
                  </span>
                </div>
                <p className="text-xs text-slate-400 font-mono">ID: {certificate.certificateId}</p>
              </div>
            </div>

            <button
              onClick={handleCopyLink}
              className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-200 text-xs font-medium border border-slate-700 transition-all"
            >
              {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
              {copied ? 'Link Copied!' : 'Copy Verification Link'}
            </button>
          </div>

          {/* Certificate Detail Cards Grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 my-6">
            {/* Student Card */}
            <div className="bg-slate-900/60 border border-slate-800/80 p-4 rounded-2xl flex items-start gap-3">
              <User className="w-5 h-5 text-indigo-400 mt-0.5" />
              <div>
                <p className="text-xs uppercase tracking-wider text-slate-400">Recipient</p>
                <p className="text-base font-semibold text-white mt-0.5">{certificate.studentName}</p>
                <p className="text-xs text-slate-500">Student ID: {certificate.studentId || 'N/A'}</p>
              </div>
            </div>

            {/* Assessment Card */}
            <div className="bg-slate-900/60 border border-slate-800/80 p-4 rounded-2xl flex items-start gap-3">
              <FileText className="w-5 h-5 text-cyan-400 mt-0.5" />
              <div>
                <p className="text-xs uppercase tracking-wider text-slate-400">Exam Assessment</p>
                <p className="text-base font-semibold text-white mt-0.5 truncate max-w-[180px]">{certificate.quizTitle}</p>
                <p className="text-xs text-slate-500">Score: {certificate.score}/{certificate.totalQuestions} ({certificate.percentage}%)</p>
              </div>
            </div>

            {/* Date & Grade Card */}
            <div className="bg-slate-900/60 border border-slate-800/80 p-4 rounded-2xl flex items-start gap-3">
              <Calendar className="w-5 h-5 text-amber-400 mt-0.5" />
              <div>
                <p className="text-xs uppercase tracking-wider text-slate-400">Issued On</p>
                <p className="text-base font-semibold text-white mt-0.5">{formatDate(certificate.issuedAt)}</p>
                <p className="text-xs text-emerald-400 font-medium">Status: Official Passing Grade</p>
              </div>
            </div>
          </div>

          {/* Digital Credential Mockup + QR Code */}
          <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-6 flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="space-y-2 text-center md:text-left">
              <div className="flex items-center gap-2 justify-center md:justify-start">
                <Award className="w-5 h-5 text-amber-400" />
                <span className="text-sm font-bold text-amber-300 uppercase tracking-widest">QuizMaster Academy Seal</span>
              </div>
              <p className="text-xs text-slate-400 max-w-md">
                This digital diploma certifies that the recipient demonstrated mastery in this curriculum following the strict QuizMaster Rule 2 verification threshold (&gt; 80%).
              </p>
              <div className="pt-2 flex items-center justify-center md:justify-start gap-3">
                <a
                  href={`https://quizmaster-dd2f8.web.app/verify/${certificate.certificateId}`}
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-1.5 text-xs text-indigo-400 hover:text-indigo-300 underline"
                >
                  View on Official Portal
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </div>
            </div>

            {/* Machine Readable QR */}
            <div className="p-3 bg-white rounded-2xl shadow-xl shrink-0">
              <QRCodeSVG
                value={`https://quizmaster-dd2f8.web.app/verify/${certificate.certificateId}`}
                size={110}
                level="H"
                includeMargin={false}
              />
            </div>
          </div>
        </div>
      )}

      {/* Not Found State */}
      {!loading && searched && !certificate && (
        <div className="animate-in fade-in duration-300 bg-red-950/30 border border-red-500/30 rounded-2xl p-8 text-center backdrop-blur-xl">
          <div className="w-14 h-14 mx-auto rounded-full bg-red-500/10 border border-red-500/30 flex items-center justify-center text-red-400 mb-4">
            <AlertCircle className="w-7 h-7" />
          </div>
          <h3 className="text-lg font-bold text-white mb-1">No Credential Record Found</h3>
          <p className="text-sm text-slate-400 max-w-md mx-auto mb-4">
            {errorMsg || "The certificate ID provided does not match any authenticated record in our database."}
          </p>
          <div className="inline-flex items-center gap-2 text-xs text-slate-500">
            <span>Tip: Check for typos, extra spaces, or verify the exact code on the student PDF.</span>
          </div>
        </div>
      )}
    </div>
  );
};

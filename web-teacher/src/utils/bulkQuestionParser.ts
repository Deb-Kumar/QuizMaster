import type { Question, Option } from '../types';

/**
 * Bulk Question Parser ported from Android BulkQuestionParser.kt
 * Automatically parses pasted quiz text into structured Question objects.
 */
export const parseBulkQuizText = (rawText: string): Question[] => {
  if (!rawText.trim()) return [];

  const lines = rawText.split(/\r?\n/).map((l) => l.trim()).filter((l) => l.length > 0);
  const questions: Question[] = [];

  let currentQuestionText = '';
  let currentOptions: { key: string; text: string; isCorrect: boolean }[] = [];
  let currentExplanation = '';
  let currentHint = '';

  const finalizeCurrent = () => {
    if (currentQuestionText && currentOptions.length >= 2) {
      const qId = `q_${Date.now()}_${questions.length + 1}`;
      const mappedOptions: Option[] = currentOptions.map((opt, i) => ({
        id: `opt_${qId}_${i + 1}`,
        text: opt.text.trim()
      }));

      const correctOptionIds = currentOptions
        .map((opt, i) => (opt.isCorrect ? mappedOptions[i].id : null))
        .filter((id): id is string => id !== null);

      // If no correct option was explicitly marked, default to option A
      if (correctOptionIds.length === 0 && mappedOptions.length > 0) {
        correctOptionIds.push(mappedOptions[0].id);
      }

      questions.push({
        id: qId,
        text: currentQuestionText.trim(),
        options: mappedOptions,
        correctOptionIds,
        explanation: currentExplanation.trim(),
        hint: currentHint.trim()
      });
    }

    currentQuestionText = '';
    currentOptions = [];
    currentExplanation = '';
    currentHint = '';
  };

  const optionRegex = /^([A-Fa-f]|\b[IVX]+\b)[\)\.\:\-]\s*(.+)$/i;
  const questionNumRegex = /^(?:Q(?:uestion)?\s*)?(\d+)[\.\)\:\-]\s*(.+)$/i;
  const answerRegex = /^(?:Correct\s*)?Ans(?:wer)?(?:\s*Key)?\s*[:\-]\s*(.+)$/i;
  const explanationRegex = /^(?:Explanation|Solution|Note)\s*[:\-]\s*(.+)$/i;
  const hintRegex = /^Hint\s*[:\-]\s*(.+)$/i;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    // Check Question header (e.g. "1. What is Kotlin?" or "Q1: Explain React")
    const qMatch = line.match(questionNumRegex);
    if (qMatch) {
      finalizeCurrent();
      currentQuestionText = qMatch[2];
      continue;
    }

    // Check Option (e.g. "A) Python", "B. Java", "C) Kotlin (Correct Answer)")
    const optMatch = line.match(optionRegex);
    if (optMatch) {
      const key = optMatch[1].toUpperCase();
      let text = optMatch[2];
      let isCorrect = false;

      // Inline correct marker: "(Correct Answer)" or "[Answer]" or "✓"
      if (/\((?:correct(?:\s*answer)?|answer|right)\)|\[(?:correct|answer)\]|✓/i.test(text)) {
        isCorrect = true;
        text = text.replace(/\((?:correct(?:\s*answer)?|answer|right)\)|\[(?:correct|answer)\]|✓/gi, '').trim();
      }

      currentOptions.push({ key, text, isCorrect });
      continue;
    }

    // Check Answer Line (e.g. "Answer: C" or "Correct Answer: Kotlin(C)")
    const ansMatch = line.match(answerRegex);
    if (ansMatch) {
      const ansVal = ansMatch[1].toUpperCase();
      currentOptions.forEach((opt) => {
        if (ansVal.includes(opt.key) || ansVal.includes(opt.text.toUpperCase())) {
          opt.isCorrect = true;
        }
      });
      continue;
    }

    // Check Explanation
    const expMatch = line.match(explanationRegex);
    if (expMatch) {
      currentExplanation = expMatch[1];
      continue;
    }

    // Check Hint
    const hintMatch = line.match(hintRegex);
    if (hintMatch) {
      currentHint = hintMatch[1];
      continue;
    }

    // If we have no current question yet, treat this line as question prompt
    if (!currentQuestionText && currentOptions.length === 0) {
      currentQuestionText = line;
    } else if (currentOptions.length === 0) {
      // Append multi-line question text
      currentQuestionText += ' ' + line;
    }
  }

  finalizeCurrent();
  return questions;
};


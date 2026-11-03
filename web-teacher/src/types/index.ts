export interface Option {
  id: string;
  text: string;
}

export type QuestionType = 'SINGLE' | 'MULTIPLE' | 'MIXED';
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type QuizStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface Question {
  id: string;
  text: string;
  options: Option[];
  correctOptionIds: string[];
  explanation: string;
  hint: string;
}

export interface Quiz {
  id: string;
  code: string;
  title: string;
  description: string;
  category: string;
  difficulty: Difficulty;
  questionType: QuestionType;
  questions: Question[];
  durationMinutes: number;
  totalMarks: number;
  passingScorePercent: number;
  status: QuizStatus;
  teacherId: string;
  teacherName: string;
  batchIds: string[];
  createdAt: number;
  updatedAt: number;
}

export interface Batch {
  id: string;
  code: string;
  name: string;
  description: string;
  teacherId: string;
  teacherName: string;
  studentIds: string[];
  pendingRequests: string[];
  createdAt: number;
}

export interface CertificateRecord {
  id: string;
  certificateId: string;
  studentId: string;
  studentName: string;
  quizId: string;
  quizTitle: string;
  score: number;
  totalQuestions: number;
  percentage: number;
  issuedAt: number | string;
  grade?: string;
  verificationUrl?: string;
}

export interface TeacherUser {
  uid: string;
  email: string | null;
  displayName: string | null;
  role: 'TEACHER' | 'STUDENT';
}

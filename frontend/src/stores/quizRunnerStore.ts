import { create } from 'zustand';
import type { CorreccionResponseDTO } from '../types';

interface QuizRunnerState {
  answers: Map<number, string>;
  isSubmitted: boolean;
  score: number | null;
  examenId: number | null;
  
  setAnswer: (preguntaId: number, respuesta: string) => void;
  removeAnswer: (preguntaId: number) => void;
  submitExam: (result: CorreccionResponseDTO) => void;
  resetQuiz: () => void;
  setExamenId: (id: number) => void;
}

export const useQuizRunnerStore = create<QuizRunnerState>((set) => ({
  answers: new Map(),
  isSubmitted: false,
  score: null,
  examenId: null,
  
  setAnswer: (preguntaId: number, respuesta: string) => {
    set((state) => {
      const newAnswers = new Map(state.answers);
      newAnswers.set(preguntaId, respuesta);
      return { answers: newAnswers };
    });
  },
  
  removeAnswer: (preguntaId: number) => {
    set((state) => {
      const newAnswers = new Map(state.answers);
      newAnswers.delete(preguntaId);
      return { answers: newAnswers };
    });
  },
  
  submitExam: (result: CorreccionResponseDTO) => {
    set({
      isSubmitted: true,
      score: result.puntajeFinal,
    });
  },
  
  resetQuiz: () => {
    set({
      answers: new Map(),
      isSubmitted: false,
      score: null,
      examenId: null,
    });
  },
  
  setExamenId: (id: number) => {
    set({ examenId: id });
  },
}));

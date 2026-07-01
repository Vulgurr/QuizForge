import { create } from 'zustand';
import type { CorreccionResponseDTO } from '../types';

interface QuizRunnerState {
  // AQUÍ ES DONDE CAMBIAMOS EL TIPO DE DATOS
  answers: Record<number, string>;
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
  answers: {}, // Inicializamos como objeto vacío {}
  isSubmitted: false,
  score: null,
  examenId: null,

  setAnswer: (preguntaId: number, respuesta: string) => {
    set((state) => ({
      // Aquí está el cambio clave para que React detecte el cambio:
      // Usamos el spread operator para clonar el objeto y actualizar la propiedad
      answers: { ...state.answers, [preguntaId]: respuesta }
    }));
  },

  removeAnswer: (preguntaId: number) => {
    set((state) => {
      const newAnswers = { ...state.answers };
      delete newAnswers[preguntaId];
      return { answers: newAnswers };
    });
  },

  submitExam: (result: CorreccionResponseDTO) => {
    set((state) => ({
      ...state, // Preserva los otros valores del estado
      isSubmitted: true,
      score: result.puntajeFinal,
    }));
  },

  resetQuiz: () => {
      console.log("RESET");
    set({
      answers: {},
      isSubmitted: false,
      score: null,
      examenId: null,
    });
  },

  setExamenId: (id: number) => {
    set({ examenId: id });
  },
}));
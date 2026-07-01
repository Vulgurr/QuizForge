import { create } from 'zustand';
import type { PreguntaDraftDTO, ExamenDraft } from '../types';

interface ExamenBuilderState {
  examenId: number | null; // <-- NUEVO: Guardamos el ID real para el backend
  titulo: string;
  descripcion: string;
  categoriaId: number | null;
  preguntas: PreguntaDraftDTO[];
  errorImportacion: string | null;

  setExamenId: (id: number | null) => void; // <-- NUEVO
  setTitulo: (titulo: string) => void;
  setDescripcion: (descripcion: string) => void;
  setCategoriaId: (categoriaId: number | null) => void;
  setPreguntas: (preguntas: PreguntaDraftDTO[]) => void;
  addPregunta: (pregunta: PreguntaDraftDTO) => void;
  updatePregunta: (index: number, pregunta: PreguntaDraftDTO) => void;
  removePregunta: (index: number) => void;
  cargarDesdeJson: (jsonString: string) => void;
  limpiarBorrador: () => void;
  setErrorImportacion: (error: string | null) => void;
}

export const useExamenBuilderStore = create<ExamenBuilderState>((set) => ({
  examenId: null, // <-- NUEVO
  titulo: '',
  descripcion: '',
  categoriaId: null,
  preguntas: [],
  errorImportacion: null,

  setExamenId: (examenId: number | null) => { // <-- NUEVO
    set({ examenId });
  },
  
  setTitulo: (titulo: string) => {
    set({ titulo });
  },
  
  setDescripcion: (descripcion: string) => {
    set({ descripcion });
  },
  
  setCategoriaId: (categoriaId: number | null) => {
    set({ categoriaId });
  },
  
  setPreguntas: (preguntas: PreguntaDraftDTO[]) => {
    set({ preguntas });
  },
  
  addPregunta: (pregunta: PreguntaDraftDTO) => {
    set((state) => ({
      preguntas: [...state.preguntas, pregunta],
    }));
  },
  
  updatePregunta: (index: number, pregunta: PreguntaDraftDTO) => {
    set((state) => {
      const newPreguntas = [...state.preguntas];
      newPreguntas[index] = pregunta;
      return { preguntas: newPreguntas };
    });
  },
  
  removePregunta: (index: number) => {
    set((state) => {
      const newPreguntas = state.preguntas.filter((_, i) => i !== index);
      return { preguntas: newPreguntas };
    });
  },
  
  cargarDesdeJson: (jsonString: string) => {
    try {
      const parsed = JSON.parse(jsonString) as ExamenDraft;
      
      // Validación básica de estructura
      if (!parsed.titulo || !Array.isArray(parsed.preguntas)) {
        throw new Error('El JSON debe tener un título y un array de preguntas');
      }
      
      set({
        titulo: parsed.titulo,
        descripcion: parsed.descripcion || '',
        preguntas: parsed.preguntas,
        errorImportacion: null,
      });
    } catch (error) {
      set({
        errorImportacion: error instanceof Error ? error.message : 'Error al procesar el JSON',
      });
    }
  },
  
  limpiarBorrador: () => {
      set({
        examenId: null, // <-- NUEVO: Limpiamos el ID
        titulo: '',
        descripcion: '',
        categoriaId: null,
        preguntas: [],
        errorImportacion: null,
      });
    },
  
  setErrorImportacion: (error: string | null) => {
    set({ errorImportacion: error });
  },
}));

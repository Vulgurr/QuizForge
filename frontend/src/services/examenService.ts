import api from './api';
import type { 
  ExamenResponseDTO, 
  ExamenRequestDTO, 
  CorreccionRequestDTO, 
  CorreccionResponseDTO 
} from '../types';

export const examenService = {
  obtenerPorSlug: async (slug: string): Promise<ExamenResponseDTO> => {
    const response = await api.get<ExamenResponseDTO>(`/examenes/${slug}`);
    return response.data;
  },

  obtenerPorCategoria: async (categoriaSlug: string): Promise<ExamenResponseDTO[]> => {
    const response = await api.get<ExamenResponseDTO[]>(`/categorias/${categoriaSlug}/examenes`);
    return response.data;
  },

  crear: async (examen: ExamenRequestDTO): Promise<ExamenResponseDTO> => {
    const response = await api.post<ExamenResponseDTO>('/examenes', examen);
    return response.data;
  },

  corregir: async (examenId: number, respuestas: CorreccionRequestDTO): Promise<CorreccionResponseDTO> => {
    const response = await api.post<CorreccionResponseDTO>(`/examenes/${examenId}/corregir`, respuestas);
    return response.data;
  },

  eliminar: async (examenId: number): Promise<void> => {
    await api.delete(`/examenes/${examenId}`);
  },
  obtenerMisExamenes: async (): Promise<ExamenResponseDTO[]> => {
    const response = await api.get<ExamenResponseDTO[]>('/examenes/mis-examenes');
    return response.data;
  },
};

import api from './api';
import type { CategoriaResponseDTO } from '../types';

export const categoriaService = {
  buscarPorApodo: async (apodo: string): Promise<CategoriaResponseDTO[]> => {
    const response = await api.get<CategoriaResponseDTO[]>('/categorias/buscar', {
      params: { apodo },
    });
    return response.data;
  },

  obtenerTodas: async (): Promise<CategoriaResponseDTO[]> => {
    const response = await api.get<CategoriaResponseDTO[]>('/categorias');
    return response.data;
  },

  obtenerPorSlug: async (slug: string): Promise<CategoriaResponseDTO> => {
    const response = await api.get<CategoriaResponseDTO>(`/categorias/${slug}`);
    return response.data;
  },
};

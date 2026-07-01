import api from './api';
import type { CategoriaResponseDTO, CategoriaRequestDTO } from '../types';

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

  crear: async (categoria: CategoriaRequestDTO): Promise<CategoriaResponseDTO> => {
    const response = await api.post<CategoriaResponseDTO>('/categorias', categoria);
    return response.data;
  },

  obtenerMisCategorias: async (): Promise<CategoriaResponseDTO[]> => {
    const response = await api.get<CategoriaResponseDTO[]>('/categorias/mis-categorias');
    return response.data;
  },
    eliminar: async (categoriaId: number): Promise<void> => {
        await api.delete(`/categorias/${categoriaId}`);
      }
};

import api from './api';
import type { AuthRequestDTO, AuthResponseDTO } from '../types';

export const authService = {
  login: async (credentials: AuthRequestDTO): Promise<AuthResponseDTO> => {
    const response = await api.post<AuthResponseDTO>('/auth/login', credentials);
    return response.data;
  },

  register: async (credentials: AuthRequestDTO): Promise<AuthResponseDTO> => {
    const response = await api.post<AuthResponseDTO>('/auth/registrar', credentials);
    return response.data;
  },
};
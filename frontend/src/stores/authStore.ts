import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthResponseDTO, Usuario } from '../types';

interface AuthState {
  token: string | null;
  userData: Usuario | null;
  isAuthenticated: boolean;
  login: (authResponse: AuthResponseDTO) => void;
  logout: () => void;
  setUserData: (userData: Usuario) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      userData: null,
      isAuthenticated: false,
      
      login: (authResponse: AuthResponseDTO) => {
        set({
          token: authResponse.token,
          userData: {
            id: 0, // Se actualizará al obtener el perfil del usuario
            email: '',
            rol: authResponse.rol,
          },
          isAuthenticated: true,
        });
      },
      
      logout: () => {
        set({
          token: null,
          userData: null,
          isAuthenticated: false,
        });
      },
      
      setUserData: (userData: Usuario) => {
        set({ userData });
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        userData: state.userData,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

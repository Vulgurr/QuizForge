import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface UIState {
  isDarkMode: boolean;
  activeModal: string | null;
  toggleDarkMode: () => void;
  setDarkMode: (isDark: boolean) => void;
  setActiveModal: (modal: string | null) => void;
  closeModal: () => void;
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      isDarkMode: false,
      activeModal: null,
      
      toggleDarkMode: () => {
        set((state) => ({ isDarkMode: !state.isDarkMode }));
      },
      
      setDarkMode: (isDark: boolean) => {
        set({ isDarkMode: isDark });
      },
      
      setActiveModal: (modal: string | null) => {
        set({ activeModal: modal });
      },
      
      closeModal: () => {
        set({ activeModal: null });
      },
    }),
    {
      name: 'ui-storage',
      partialize: (state) => ({
        isDarkMode: state.isDarkMode,
      }),
    }
  )
);

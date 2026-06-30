import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { useUIStore } from '../../stores/uiStore';
import { Search, Moon, Sun, LogOut, User, BookOpen } from 'lucide-react';
import { useState } from 'react';
import BuscadorCategorias from '../common/BuscadorCategorias';
import UserMenuDropdown from '../common/UserMenuDropdown';

function Navbar() {
  const { isAuthenticated, logout } = useAuthStore();
  const { isDarkMode, toggleDarkMode } = useUIStore();
  const navigate = useNavigate();
  const [showSearch, setShowSearch] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <BookOpen className="h-8 w-8 text-indigo-600 dark:text-indigo-400" />
            <span className="text-xl font-bold text-gray-900 dark:text-white">QuizForge</span>
          </Link>

          {/* Buscador - Desktop */}
          <div className="hidden md:flex flex-1 max-w-md mx-8">
            <BuscadorCategorias />
          </div>

          {/* Acciones */}
          <div className="flex items-center space-x-4">
            {/* Botón buscador móvil */}
            <button
              onClick={() => setShowSearch(!showSearch)}
              className="md:hidden p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800"
              data-testid="mobile-search-button"
            >
              <Search className="h-5 w-5 text-gray-600 dark:text-gray-400" />
            </button>

            {/* Toggle tema */}
            <button
              onClick={toggleDarkMode}
              className="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              data-testid="theme-toggle"
              aria-label="Cambiar tema"
            >
              {isDarkMode ? (
                <Sun className="h-5 w-5 text-gray-600 dark:text-gray-400" />
              ) : (
                <Moon className="h-5 w-5 text-gray-600 dark:text-gray-400" />
              )}
            </button>

            {/* Menú usuario */}
            <UserMenuDropdown isAuthenticated={isAuthenticated} onLogout={handleLogout} />
          </div>
        </div>

        {/* Buscador móvil */}
        {showSearch && (
          <div className="md:hidden pb-4">
            <BuscadorCategorias />
          </div>
        )}
      </div>
    </nav>
  );
}

export default Navbar;

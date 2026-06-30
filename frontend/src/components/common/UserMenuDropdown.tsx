import { useState } from 'react';
import { Link } from 'react-router-dom';
import { User, LogOut, LayoutDashboard } from 'lucide-react';

interface UserMenuDropdownProps {
  isAuthenticated: boolean;
  onLogout: () => void;
}

function UserMenuDropdown({ isAuthenticated, onLogout }: UserMenuDropdownProps) {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="relative" data-testid="user-menu-dropdown">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
        data-testid="user-menu-button"
      >
        <User className="h-5 w-5 text-gray-600 dark:text-gray-400" />
      </button>

      {isOpen && (
        <>
          <div
            className="fixed inset-0 z-40"
            onClick={() => setIsOpen(false)}
          />
          <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-50">
            {isAuthenticated ? (
              <>
                <Link
                  to="/dashboard"
                  onClick={() => setIsOpen(false)}
                  className="flex items-center space-x-2 px-4 py-3 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-white transition-colors"
                  data-testid="dashboard-link"
                >
                  <LayoutDashboard className="h-4 w-4" />
                  <span>Dashboard</span>
                </Link>
                <button
                  onClick={() => {
                    onLogout();
                    setIsOpen(false);
                  }}
                  className="w-full flex items-center space-x-2 px-4 py-3 hover:bg-gray-100 dark:hover:bg-gray-700 text-red-600 dark:text-red-400 transition-colors border-t border-gray-200 dark:border-gray-700"
                  data-testid="logout-button"
                >
                  <LogOut className="h-4 w-4" />
                  <span>Cerrar sesión</span>
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/auth/login"
                  onClick={() => setIsOpen(false)}
                  className="block px-4 py-3 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-white transition-colors"
                  data-testid="login-link"
                >
                  Iniciar sesión
                </Link>
                <Link
                  to="/auth/register"
                  onClick={() => setIsOpen(false)}
                  className="block px-4 py-3 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-900 dark:text-white transition-colors border-t border-gray-200 dark:border-gray-700"
                  data-testid="register-link"
                >
                  Registrarse
                </Link>
              </>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default UserMenuDropdown;

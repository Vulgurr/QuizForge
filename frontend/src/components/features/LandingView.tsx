import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../layout/Navbar';
import { categoriaService } from '../../services';
import type { CategoriaResponseDTO } from '../../types';
import { BookOpen, ArrowRight, Loader2 } from 'lucide-react';

function LandingView() {
  const [categorias, setCategorias] = useState<CategoriaResponseDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCategorias = async () => {
      try {
        setIsLoading(true);
        const data = await categoriaService.obtenerTodas();
        setCategorias(data);
        setError(null);
      } catch (err) {
        console.error('Error cargando categorías:', err);
        setError('Error al cargar las categorías. Por favor, intenta nuevamente.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchCategorias();
  }, []);

  const coloresCategoria = [
    'from-blue-500 to-blue-600',
    'from-purple-500 to-purple-600',
    'from-green-500 to-green-600',
    'from-orange-500 to-orange-600',
    'from-pink-500 to-pink-600',
    'from-teal-500 to-teal-600',
  ];

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar />
      
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-indigo-600 to-purple-700 text-white py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <BookOpen className="h-16 w-16 mx-auto mb-6 opacity-90" />
          <h1 className="text-4xl md:text-6xl font-bold mb-6">
            Bienvenido a QuizForge
          </h1>
          <p className="text-xl md:text-2xl mb-8 opacity-90 max-w-2xl mx-auto">
            La plataforma definitiva para crear, compartir y rendir exámenes online
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/auth/register"
              className="inline-flex items-center px-8 py-3 bg-white text-indigo-600 font-semibold rounded-lg hover:bg-gray-100 transition-colors"
              data-testid="register-cta"
            >
              Comenzar ahora
              <ArrowRight className="ml-2 h-5 w-5" />
            </Link>
            <Link
              to="/auth/login"
              className="inline-flex items-center px-8 py-3 bg-transparent border-2 border-white text-white font-semibold rounded-lg hover:bg-white hover:text-indigo-600 transition-colors"
              data-testid="login-cta"
            >
              Iniciar sesión
            </Link>
          </div>
        </div>
      </section>

      {/* Categorías Section */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-8 text-center">
            Explora por Categoría
          </h2>
          
          {isLoading && (
            <div className="flex justify-center items-center py-12" data-testid="loading-spinner">
              <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
              <span className="ml-3 text-gray-600 dark:text-gray-400">Cargando categorías...</span>
            </div>
          )}

          {error && (
            <div className="text-center py-12">
              <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-6 max-w-md mx-auto" data-testid="error-message">
                <p className="text-red-600 dark:text-red-400">{error}</p>
              </div>
            </div>
          )}

          {!isLoading && !error && categorias.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-600 dark:text-gray-400">No hay categorías disponibles</p>
            </div>
          )}

          {!isLoading && !error && categorias.length > 0 && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {categorias.map((categoria, index) => (
                <Link
                  key={categoria.id}
                  to={`/categorias/${categoria.slug}`}
                  className="group relative overflow-hidden rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
                  data-testid={`categoria-card-${categoria.id}`}
                >
                  <div className={`absolute inset-0 bg-gradient-to-br ${coloresCategoria[index % coloresCategoria.length]}`} />
                  <div className="relative p-6 text-white">
                    <h3 className="text-xl font-bold mb-2">{categoria.nombre}</h3>
                    <p className="text-sm opacity-90 mb-4 line-clamp-2">
                      {categoria.descripcion}
                    </p>
                    <div className="flex items-center text-sm font-medium opacity-75 group-hover:opacity-100 transition-opacity">
                      <span>Ver exámenes</span>
                      <ArrowRight className="ml-2 h-4 w-4" />
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-100 dark:bg-gray-800 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-gray-600 dark:text-gray-400">
          <p>&copy; 2024 QuizForge. Todos los derechos reservados.</p>
        </div>
      </footer>
    </div>
  );
}

export default LandingView;

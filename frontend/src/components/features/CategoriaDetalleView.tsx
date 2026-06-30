import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import Navbar from '../layout/Navbar';
import { categoriaService, examenService } from '../../services';
import type { CategoriaResponseDTO, ExamenResponseDTO } from '../../types';
import { Loader2, BookOpen, Clock, User } from 'lucide-react';

function CategoriaDetalleView() {
  const { slug } = useParams<{ slug: string }>();
  const [categoria, setCategoria] = useState<CategoriaResponseDTO | null>(null);
  const [examenes, setExamenes] = useState<ExamenResumenDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!slug) return;

      try {
        setIsLoading(true);
        
        // Fetch categoría y exámenes en paralelo
        const [categoriaData, examenesData] = await Promise.all([
          categoriaService.obtenerPorSlug(slug),
          examenService.obtenerPorCategoria(slug),
        ]);

        setCategoria(categoriaData);
        setExamenes(examenesData);
        setError(null);
      } catch (err) {
        console.error('Error cargando datos:', err);
        setError('Error al cargar la categoría o los exámenes');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [slug]);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
        <Navbar />
        <div className="flex justify-center items-center py-20">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
          <span className="ml-3 text-gray-600 dark:text-gray-400">Cargando...</span>
        </div>
      </div>
    );
  }

  if (error || !categoria) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
        <Navbar />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-6">
            <p className="text-red-600 dark:text-red-400">{error || 'Categoría no encontrada'}</p>
            <Link
              to="/"
              className="inline-block mt-4 text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
            >
              Volver al inicio
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar />
      
      {/* Header de categoría */}
      <div className="bg-gradient-to-r from-indigo-600 to-purple-700 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h1 className="text-4xl font-bold mb-4">{categoria.nombre}</h1>
          <p className="text-xl opacity-90 max-w-3xl">{categoria.descripcion}</p>
        </div>
      </div>

      {/* Lista de exámenes */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">
          Exámenes disponibles ({examenes.length})
        </h2>

        {examenes.length === 0 ? (
          <div className="text-center py-12">
            <BookOpen className="h-16 w-16 mx-auto text-gray-400 mb-4" />
            <p className="text-gray-600 dark:text-gray-400 text-lg">
              No hay exámenes disponibles en esta categoría
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {examenes.map((examen) => (
              <div
                key={examen.id}
                className="bg-white dark:bg-gray-800 rounded-lg shadow-md hover:shadow-lg transition-shadow p-6"
                data-testid={`examen-card-${examen.id}`}
              >
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                  {examen.titulo}
                </h3>
                <p className="text-gray-600 dark:text-gray-400 mb-4 line-clamp-2">
                  {examen.descripcion}
                </p>
                
                <div className="flex items-center text-sm text-gray-500 dark:text-gray-400 mb-4 space-x-4">
                  <div className="flex items-center">
                    <BookOpen className="h-4 w-4 mr-1" />
                    <span>{examen.cantidadPreguntas} preguntas</span>
                  </div>
                  <div className="flex items-center">
                    <User className="h-4 w-4 mr-1" />
                    <span>ID: {examen.creadorId}</span>
                  </div>
                </div>

                <Link
                  to={`/examenes/${examen.slug}/rendir`}
                  className="inline-flex items-center justify-center w-full px-4 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition-colors"
                  data-testid={`rendir-examen-${examen.id}`}
                >
                  Rendir examen
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default CategoriaDetalleView;

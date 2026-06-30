import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../layout/Navbar';
import { categoriaService, examenService } from '../../services';
import type { CategoriaResponseDTO, ExamenResponseDTO } from '../../types';
import { Plus, Edit, Trash2, BookOpen, Loader2 } from 'lucide-react';

function DashboardCreadorView() {
  const navigate = useNavigate();
  const [categorias, setCategorias] = useState<CategoriaResponseDTO[]>([]);
  const [examenes, setExamenes] = useState<ExamenResponseDTO[]>([]);
  const [selectedCategoria, setSelectedCategoria] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
      const fetchData = async () => {
        try {
          setIsLoading(true);

          const [categoriasData, examenesData] = await Promise.all([
            categoriaService.obtenerTodas(),
            // Cambiamos el placeholder por un método real para el creador
            examenService.obtenerMisExamenes(),
          ]);

          setCategorias(categoriasData);
          setExamenes(examenesData);
          setError(null);
        } catch (err) {
          console.error('Error cargando datos del dashboard:', err);
          setError('Error al cargar los datos');
        } finally {
          setIsLoading(false);
        }
      };

      fetchData();
    }, []);

  const handleDeleteExamen = async (examenId: number) => {
    if (!window.confirm('¿Estás seguro de que deseas eliminar este examen?')) {
      return;
    }

    try {
      await examenService.eliminar(examenId);
      setExamenes(examenes.filter((e) => e.id !== examenId));
    } catch (err) {
      console.error('Error eliminando examen:', err);
      setError('Error al eliminar el examen');
    }
  };

  const filteredExamenes = selectedCategoria
    ? examenes.filter((e) => e.categoriaId === selectedCategoria)
    : examenes;

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

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar />
      
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
              Dashboard de Creador
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-2">
              Gestiona tus exámenes y categorías
            </p>
          </div>
          <Link
            to="/dashboard/crear-examen"
            className="inline-flex items-center px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition-colors"
            data-testid="crear-examen-button"
          >
            <Plus className="h-5 w-5 mr-2" />
            Crear Examen
          </Link>
        </div>

        {error && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 mb-6">
            <p className="text-red-600 dark:text-red-400">{error}</p>
          </div>
        )}

        {/* Filtros */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-8">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Filtrar por Categoría
          </h2>
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => setSelectedCategoria(null)}
              className={`px-4 py-2 rounded-lg transition-colors ${
                selectedCategoria === null
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
              }`}
              data-testid="filter-all"
            >
              Todas
            </button>
            {categorias.map((categoria) => (
              <button
                key={categoria.id}
                onClick={() => setSelectedCategoria(categoria.id)}
                className={`px-4 py-2 rounded-lg transition-colors ${
                  selectedCategoria === categoria.id
                    ? 'bg-indigo-600 text-white'
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                }`}
                data-testid={`filter-categoria-${categoria.id}`}
              >
                {categoria.nombre}
              </button>
            ))}
          </div>
        </div>

        {/* Lista de exámenes */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
              Mis Exámenes ({filteredExamenes.length})
            </h2>
          </div>

          {filteredExamenes.length === 0 ? (
            <div className="p-12 text-center">
              <BookOpen className="h-16 w-16 mx-auto text-gray-400 mb-4" />
              <p className="text-gray-600 dark:text-gray-400 text-lg mb-4">
                No tienes exámenes creados
              </p>
              <Link
                to="/dashboard/crear-examen"
                className="inline-flex items-center text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
              >
                <Plus className="h-5 w-5 mr-2" />
                Crear tu primer examen
              </Link>
            </div>
          ) : (
            <div className="divide-y divide-gray-200 dark:divide-gray-700">
                          {filteredExamenes.map((examen) => {
                            // Buscamos el nombre real de la categoría cruzando los datos
                            const categoriaDelExamen = categorias.find(c => c.id === examen.categoriaId);
                            const nombreCategoria = categoriaDelExamen ? categoriaDelExamen.nombre : 'Sin categoría';

                            return (
                              <div
                                key={examen.id}
                                className="px-6 py-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
                                data-testid={`examen-row-${examen.id}`}
                              >
                                <div className="flex items-center justify-between">
                                  <div className="flex-1">
                                    <h3 className="text-lg font-medium text-gray-900 dark:text-white">
                                      {examen.titulo}
                                    </h3>
                                    <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                                      {examen.descripcion}
                                    </p>
                                    <div className="flex items-center mt-2 text-sm text-gray-500 dark:text-gray-400 space-x-4">
                                      <span>{examen.preguntas.length} preguntas</span>
                                      {/* Ahora mostramos el nombre en lugar del ID crudo */}
                                      <span className="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-md text-xs">
                                        {nombreCategoria}
                                      </span>
                                      <span>Creado: {new Date(examen.creadoEn).toLocaleDateString()}</span>
                                    </div>
                                  </div>
                                  <div className="flex items-center space-x-2 ml-4">
                                    <Link
                                      to={`/examenes/${examen.slug}/rendir`}
                                      className="p-2 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                                      title="Ver examen"
                                      data-testid={`view-examen-${examen.id}`}
                                    >
                                      <BookOpen className="h-5 w-5" />
                                    </Link>
                                    {/* Corregimos la ruta de edición */}
                                    <Link
                                      to={`/dashboard/editar-examen/${examen.id}`}
                                      className="p-2 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                                      title="Editar examen"
                                      data-testid={`edit-examen-${examen.id}`}
                                    >
                                      <Edit className="h-5 w-5" />
                                    </Link>
                                    <button
                                      onClick={() => handleDeleteExamen(examen.id)}
                                      className="p-2 text-gray-600 dark:text-gray-400 hover:text-red-600 dark:hover:text-red-400 transition-colors"
                                      title="Eliminar examen"
                                      data-testid={`delete-examen-${examen.id}`}
                                    >
                                      <Trash2 className="h-5 w-5" />
                                    </button>
                                  </div>
                                </div>
                              </div>
                            );
                          })}
                        </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default DashboardCreadorView;

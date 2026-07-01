import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../layout/Navbar';
import { categoriaService, examenService } from '../../services';
import type { CategoriaResponseDTO, ExamenResponseDTO } from '../../types';
import { Plus, Edit, Trash2, BookOpen, Loader2, Settings, X, Check } from 'lucide-react';

function DashboardCreadorView() {
  const navigate = useNavigate();
  const [categorias, setCategorias] = useState<CategoriaResponseDTO[]>([]);
  const [examenes, setExamenes] = useState<ExamenResponseDTO[]>([]);
  const [selectedCategoria, setSelectedCategoria] = useState<number | null>(null);

  // Estados de UI
  const [isEditMode, setIsEditMode] = useState(false);
  const [showCreateCategoria, setShowCreateCategoria] = useState(false);
  const [newCategoria, setNewCategoria] = useState({ nombre: '', descripcion: '', apodos: '' });
  const [isSubmittingCategoria, setIsSubmittingCategoria] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const [categoriasData, examenesData] = await Promise.all([
          categoriaService.obtenerTodas(),
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
    if (!window.confirm('¿Estás seguro de que deseas eliminar este examen?')) return;
    try {
      await examenService.eliminar(examenId);
      setExamenes(examenes.filter((e) => e.id !== examenId));
    } catch (err) {
      console.error('Error eliminando examen:', err);
      setError('Error al eliminar el examen');
    }
  };

  const handleDeleteCategoria = async (categoriaId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!window.confirm('¿Estás seguro de que deseas eliminar esta categoría?')) return;

    try {
      await categoriaService.eliminar(categoriaId);
      setCategorias(categorias.filter((c) => c.id !== categoriaId));
      if (selectedCategoria === categoriaId) setSelectedCategoria(null);
      setError(null);
    } catch (err: any) {
      if (err.response?.status === 409) {
        setError("No se puede borrar la categoría porque contiene exámenes asociados.");
      } else if (err.response?.status === 403) {
        setError("No tienes permisos para eliminar esta categoría.");
      } else {
        setError("Ocurrió un error inesperado al intentar eliminar la categoría.");
      }
    }
  };

  const handleCreateCategoria = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCategoria.nombre.trim()) return;

    setIsSubmittingCategoria(true);
    try {
      const apodosArray = newCategoria.apodos
        ? newCategoria.apodos.split(',').map(a => a.trim()).filter(a => a.length > 0)
        : [];

      const payload = {
        nombre: newCategoria.nombre,
        descripcion: newCategoria.descripcion || 'Sin descripción',
        apodos: apodosArray,
      };

      const creada = await categoriaService.crear(payload);
      setCategorias([...categorias, creada]);
      setNewCategoria({ nombre: '', descripcion: '', apodos: '' });
      setShowCreateCategoria(false);
      setError(null);
    } catch (err) {
      console.error('Error creando categoría:', err);
      setError("Error al crear la categoría.");
    } finally {
      setIsSubmittingCategoria(false);
    }
  };

  const toggleEditMode = () => {
    setIsEditMode(!isEditMode);
    setShowCreateCategoria(false);
    setError(null);
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
          >
            <Plus className="h-5 w-5 mr-2" />
            Crear Examen
          </Link>
        </div>

        {error && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 mb-6 relative">
            <p className="text-red-600 dark:text-red-400 pr-8">{error}</p>
            <button
              onClick={() => setError(null)}
              className="absolute top-4 right-4 text-red-500 hover:text-red-700"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        )}

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-8 border border-gray-100 dark:border-gray-700">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
              Mis Categorías
            </h2>
            <button
              onClick={toggleEditMode}
              className={`flex items-center px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
                isEditMode
                  ? 'bg-indigo-100 text-indigo-700 dark:bg-indigo-900/50 dark:text-indigo-300'
                  : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700'
              }`}
            >
              <Settings className="h-4 w-4 mr-2" />
              {isEditMode ? 'Terminar Edición' : 'Modo Edición'}
            </button>
          </div>

          <div className="flex flex-wrap gap-3">
            {!isEditMode && (
              <button
                onClick={() => setSelectedCategoria(null)}
                className={`px-4 py-2 rounded-lg transition-colors ${
                  selectedCategoria === null
                    ? 'bg-indigo-600 text-white'
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                }`}
              >
                Todas
              </button>
            )}

            {categorias.map((categoria) => (
              <div key={categoria.id} className="relative group">
                <button
                  onClick={() => !isEditMode && setSelectedCategoria(categoria.id)}
                  className={`px-4 py-2 rounded-lg transition-all duration-200 ${
                    isEditMode ? 'pr-10 cursor-default bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300' :
                    selectedCategoria === categoria.id
                      ? 'bg-indigo-600 text-white'
                      : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
                  }`}
                >
                  {categoria.nombre}
                </button>

                {isEditMode && (
                  <button
                    onClick={(e) => handleDeleteCategoria(categoria.id, e)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 p-1 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded hover:bg-red-200 dark:hover:bg-red-900/50 transition-colors"
                    title="Eliminar categoría"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                )}
              </div>
            ))}

            {isEditMode && !showCreateCategoria && (
              <button
                onClick={() => setShowCreateCategoria(true)}
                className="flex items-center px-4 py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 text-gray-500 dark:text-gray-400 hover:border-indigo-500 hover:text-indigo-600 dark:hover:border-indigo-400 dark:hover:text-indigo-400 rounded-lg transition-colors"
              >
                <Plus className="h-4 w-4 mr-1" />
                Nueva Categoría
              </button>
            )}
          </div>

          {showCreateCategoria && (
            <form onSubmit={handleCreateCategoria} className="mt-4 p-4 bg-gray-50 dark:bg-gray-700/30 rounded-lg border border-gray-200 dark:border-gray-700 flex flex-wrap gap-3 items-end animate-in fade-in slide-in-from-top-2">
              <div className="flex-1 min-w-[150px]">
                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Nombre</label>
                <input
                  type="text"
                  required
                  value={newCategoria.nombre}
                  onChange={(e) => setNewCategoria({ ...newCategoria, nombre: e.target.value })}
                  className="w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-800 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                  placeholder="Ej: Programación"
                />
              </div>
              <div className="flex-1 min-w-[150px]">
                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Descripción (Opcional)</label>
                <input
                  type="text"
                  value={newCategoria.descripcion}
                  onChange={(e) => setNewCategoria({ ...newCategoria, descripcion: e.target.value })}
                  className="w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-800 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                  placeholder="Breve descripción..."
                />
              </div>
              <div className="flex-1 min-w-[200px]">
                <label className="block text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Apodos (separados por comas)</label>
                <input
                  type="text"
                  value={newCategoria.apodos}
                  onChange={(e) => setNewCategoria({ ...newCategoria, apodos: e.target.value })}
                  className="w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-800 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                  placeholder="Ej: mate, math, exactas"
                />
              </div>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setShowCreateCategoria(false)}
                  className="px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-50 dark:hover:bg-gray-700"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmittingCategoria}
                  className="flex items-center px-3 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 disabled:opacity-50"
                >
                  {isSubmittingCategoria ? <Loader2 className="h-4 w-4 animate-spin" /> : <Check className="h-4 w-4 mr-1" />}
                  Guardar
                </button>
              </div>
            </form>
          )}
        </div>

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
                No tienes exámenes creados en esta categoría
              </p>
              <Link
                to="/dashboard/crear-examen"
                className="inline-flex items-center text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
              >
                <Plus className="h-5 w-5 mr-2" />
                Crear nuevo examen
              </Link>
            </div>
          ) : (
            <div className="divide-y divide-gray-200 dark:divide-gray-700">
              {filteredExamenes.map((examen) => {
                const categoriaDelExamen = categorias.find(c => c.id === examen.categoriaId);
                const nombreCategoria = categoriaDelExamen ? categoriaDelExamen.nombre : 'Sin categoría';

                return (
                  <div key={examen.id} className="px-6 py-4 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors">
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
                          <span className="px-2 py-1 bg-gray-100 dark:bg-gray-700 rounded-md text-xs">
                            {nombreCategoria}
                          </span>
                          <span>Creado: {new Date(examen.creadoEn).toLocaleDateString()}</span>
                        </div>
                      </div>
                      <div className="flex items-center space-x-2 ml-4">
                        <Link to={`/examenes/${examen.slug}/rendir`} className="p-2 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400">
                          <BookOpen className="h-5 w-5" />
                        </Link>
                        <Link to={`/dashboard/editar-examen/${examen.slug}`} className="p-2 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400">
                          <Edit className="h-5 w-5" />
                        </Link>
                        <button onClick={() => handleDeleteExamen(examen.id)} className="p-2 text-gray-600 dark:text-gray-400 hover:text-red-600 dark:hover:text-red-400">
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
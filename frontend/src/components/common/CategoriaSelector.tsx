import { useState, useEffect } from 'react';
import { categoriaService } from '../../services';
import type { CategoriaResponseDTO } from '../../types';
import { ChevronDown, Plus, Search } from 'lucide-react';

interface CategoriaSelectorProps {
  selectedCategoriaId: number | null;
  onCategoriaSelect: (categoriaId: number | null) => void;
  onCrearCategoria: (nombre: string, descripcion?: string, apodosStr?: string) => Promise<CategoriaResponseDTO>;
  error?: string;
}

function CategoriaSelector({
  selectedCategoriaId,
  onCategoriaSelect,
  onCrearCategoria,
  error
}: CategoriaSelectorProps) {
  const [categorias, setCategorias] = useState<CategoriaResponseDTO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isOpen, setIsOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isCreating, setIsCreating] = useState(false);

  // Estados separados para evitar el problema de actualización de objetos
  const [newCategoriaNombre, setNewCategoriaNombre] = useState('');
  const [newCategoriaDescripcion, setNewCategoriaDescripcion] = useState('');
  const [newCategoriaApodos, setNewCategoriaApodos] = useState('');

  useEffect(() => {
    const fetchCategorias = async () => {
      try {
        setIsLoading(true);
        const data = await categoriaService.obtenerTodas();
        setCategorias(data);
      } catch (err) {
        console.error('Error cargando categorías:', err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchCategorias();
  }, []);

  const filteredCategorias = categorias.filter((cat) => {
    const query = searchQuery.toLowerCase();
    const matchNombre = cat.nombre.toLowerCase().includes(query);
    const matchSlug = cat.slug?.toLowerCase().includes(query) || false;
    const matchDesc = cat.descripcion?.toLowerCase().includes(query) || false;
    const matchApodos = cat.apodos?.some(apodo => apodo.toLowerCase().includes(query)) || false;

    return matchNombre || matchSlug || matchDesc || matchApodos;
  });

  const handleCrearCategoria = async () => {
    if (!newCategoriaNombre.trim()) return;

    try {
      const nuevaCategoria = await onCrearCategoria(
        newCategoriaNombre.trim(),
        newCategoriaDescripcion.trim() || undefined,
        newCategoriaApodos.trim() || undefined
      );

      setCategorias(prev => [...prev, nuevaCategoria]);
      setNewCategoriaNombre('');
      setNewCategoriaDescripcion('');
      setNewCategoriaApodos('');
      setIsCreating(false);
      setIsOpen(false);
    } catch (err) {
      // El error lo maneja el padre
    }
  };

  const selectedCategoria = categorias.find((c) => c.id === selectedCategoriaId);

  return (
    <div className="space-y-2" data-testid="categoria-selector">
      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">
        Categoría <span className="text-red-500">*</span>
      </label>

      <div className="relative">
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className="w-full px-4 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white text-left flex items-center justify-between focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
        >
          <span>
            {selectedCategoria ? selectedCategoria.nombre : 'Seleccionar categoría...'}
          </span>
          <ChevronDown className={`h-4 w-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
        </button>

        {error && (
          <p className="mt-1 text-sm text-red-600 dark:text-red-400">{error}</p>
        )}

        {isOpen && (
          <div className="absolute z-50 w-full mt-2 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg max-h-80 overflow-y-auto">

            {/* Buscador */}
            <div className="p-3 border-b border-gray-200 dark:border-gray-700">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Buscar por nombre, slug o descripción..."
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                />
              </div>
            </div>

            <button
              type="button"
              onClick={() => setIsCreating(true)}
              className="w-full px-4 py-3 text-left hover:bg-gray-100 dark:hover:bg-gray-700 border-b border-gray-200 dark:border-gray-700 flex items-center text-indigo-600 dark:text-indigo-400"
            >
              <Plus className="h-4 w-4 mr-2" />
              Crear nueva categoría
            </button>

            {isCreating && (
              <div className="p-4 bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700">
                <div className="space-y-3">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Nombre
                    </label>
                    <input
                      type="text"
                      value={newCategoriaNombre}
                      onChange={(e) => setNewCategoriaNombre(e.target.value)}
                      placeholder="Nombre de la categoría"
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Descripción (opcional)
                    </label>
                    <input
                      type="text"
                      value={newCategoriaDescripcion}
                      onChange={(e) => setNewCategoriaDescripcion(e.target.value)}
                      placeholder="Descripción breve"
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Apodos (separados por comas)
                    </label>
                    <input
                      type="text"
                      value={newCategoriaApodos}
                      onChange={(e) => setNewCategoriaApodos(e.target.value)}
                      placeholder="Ej: mate, math, exactas"
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                  </div>

                  <div className="flex gap-2 mt-4">
                    <button
                      type="button"
                      onClick={handleCrearCategoria}
                      disabled={!newCategoriaNombre.trim()}
                      className="flex-1 px-3 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Crear
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setIsCreating(false);
                        setNewCategoriaNombre('');
                        setNewCategoriaDescripcion('');
                        setNewCategoriaApodos('');
                      }}
                      className="px-3 py-2 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-sm rounded-lg hover:bg-gray-300 dark:hover:bg-gray-600"
                    >
                      Cancelar
                    </button>
                  </div>
                </div>
              </div>
            )}

            {isLoading ? (
              <div className="px-4 py-3 text-gray-500 dark:text-gray-400 text-sm">
                Cargando categorías...
              </div>
            ) : filteredCategorias.length === 0 ? (
              <div className="px-4 py-3 text-gray-500 dark:text-gray-400 text-sm">
                No se encontraron coincidencias
              </div>
            ) : (
              filteredCategorias.map((categoria) => (
                <button
                  key={categoria.id}
                  type="button"
                  onClick={() => {
                    onCategoriaSelect(categoria.id);
                    setIsOpen(false);
                  }}
                  className={`w-full px-4 py-3 text-left hover:bg-gray-100 dark:hover:bg-gray-700 border-b border-gray-100 dark:border-gray-700 last:border-b-0 ${
                    selectedCategoriaId === categoria.id
                      ? 'bg-indigo-50 dark:bg-indigo-900/20 text-indigo-600 dark:text-indigo-400'
                      : 'text-gray-900 dark:text-white'
                  }`}
                >
                  <div className="font-medium">{categoria.nombre}</div>
                  <div className="flex flex-col gap-1 mt-1">
                    {categoria.slug && (
                      <span className="text-xs text-indigo-500 dark:text-indigo-400 font-mono">
                        /{categoria.slug}
                      </span>
                    )}
                    {categoria.descripcion && (
                      <span className="text-sm text-gray-500 dark:text-gray-400 truncate">
                        {categoria.descripcion}
                      </span>
                    )}
                  </div>
                </button>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default CategoriaSelector;
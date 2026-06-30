import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, X } from 'lucide-react';
import { categoriaService } from '../../services';
import type { CategoriaResponseDTO } from '../../types';

function BuscadorCategorias() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<CategoriaResponseDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const navigate = useNavigate();

  const handleSearch = async (value: string) => {
    setQuery(value);
    
    if (value.length < 2) {
      setResults([]);
      setShowResults(false);
      return;
    }

    setIsLoading(true);
    try {
      const categorias = await categoriaService.buscarPorApodo(value);
      setResults(categorias);
      setShowResults(true);
    } catch (error) {
      console.error('Error buscando categorías:', error);
      setResults([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelectCategoria = (slug: string) => {
    setQuery('');
    setResults([]);
    setShowResults(false);
    navigate(`/categorias/${slug}`);
  };

  const handleClear = () => {
    setQuery('');
    setResults([]);
    setShowResults(false);
  };

  return (
    <div className="relative w-full" data-testid="categoria-search">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
        <input
          type="text"
          value={query}
          onChange={(e) => handleSearch(e.target.value)}
          placeholder="Buscar categorías..."
          className="w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-500 focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
          data-testid="search-input"
        />
        {query && (
          <button
            onClick={handleClear}
            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
            data-testid="clear-search"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>

      {showResults && results.length > 0 && (
        <div className="absolute mt-2 w-full bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-50 max-h-60 overflow-y-auto">
          {results.map((categoria) => (
            <button
              key={categoria.id}
              onClick={() => handleSelectCategoria(categoria.slug)}
              className="w-full text-left px-4 py-3 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors border-b border-gray-100 dark:border-gray-700 last:border-b-0"
              data-testid={`categoria-result-${categoria.id}`}
            >
              <div className="font-medium text-gray-900 dark:text-white">
                {categoria.nombre}
              </div>
              <div className="text-sm text-gray-500 dark:text-gray-400">
                {categoria.descripcion}
              </div>
            </button>
          ))}
        </div>
      )}

      {showResults && query.length >= 2 && results.length === 0 && !isLoading && (
        <div className="absolute mt-2 w-full bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-50 px-4 py-3 text-gray-500 dark:text-gray-400">
          No se encontraron categorías
        </div>
      )}

      {isLoading && (
        <div className="absolute mt-2 w-full bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg z-50 px-4 py-3 text-gray-500 dark:text-gray-400">
          Buscando...
        </div>
      )}
    </div>
  );
}

export default BuscadorCategorias;

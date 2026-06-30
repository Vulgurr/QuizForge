import { useExamenBuilderStore } from '../../stores/examenBuilderStore';
import PreguntaEditorCard from './PreguntaEditorCard';
import { BookOpen, Trash2 } from 'lucide-react';

function ExamenBuilder() {
  const { titulo, descripcion, preguntas, setTitulo, setDescripcion, removePregunta } = useExamenBuilderStore();

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6" data-testid="examen-builder">
      <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4 flex items-center">
        <BookOpen className="h-5 w-5 mr-2" />
        Editor Visual
      </h2>

      {/* Título y descripción */}
      <div className="space-y-4 mb-6">
        <div>
          <label htmlFor="titulo" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Título del examen
          </label>
          <input
            id="titulo"
            type="text"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Ej: Examen de Historia - Primer Parcial"
            className="w-full px-4 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            data-testid="examen-titulo-input"
          />
        </div>

        <div>
          <label htmlFor="descripcion" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Descripción (opcional)
          </label>
          <textarea
            id="descripcion"
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            placeholder="Breve descripción del examen..."
            rows={3}
            className="w-full px-4 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
            data-testid="examen-descripcion-input"
          />
        </div>
      </div>

      {/* Lista de preguntas */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-medium text-gray-900 dark:text-white">
            Preguntas ({preguntas.length})
          </h3>
        </div>

        {preguntas.length === 0 ? (
          <div className="text-center py-12 border-2 border-dashed border-gray-300 dark:border-gray-700 rounded-lg">
            <BookOpen className="h-12 w-12 mx-auto text-gray-400 mb-4" />
            <p className="text-gray-600 dark:text-gray-400">
              No hay preguntas todavía. Importa un JSON o agrégalas manualmente.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {preguntas.map((pregunta, index) => (
              <div
                key={index}
                className="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:border-gray-300 dark:hover:border-gray-600 transition-colors"
                data-testid={`pregunta-card-${index}`}
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center">
                    <span className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 font-medium text-sm mr-3">
                      {index + 1}
                    </span>
                    <span className="text-sm font-medium text-gray-600 dark:text-gray-400">
                      {pregunta.tipo}
                    </span>
                  </div>
                  <button
                    onClick={() => removePregunta(index)}
                    className="p-1 text-gray-400 hover:text-red-600 dark:hover:text-red-400 transition-colors"
                    data-testid={`delete-pregunta-${index}`}
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>

                <PreguntaEditorCard pregunta={pregunta} index={index} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default ExamenBuilder;

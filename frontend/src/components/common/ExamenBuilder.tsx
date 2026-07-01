import { useExamenBuilderStore } from '../../stores/examenBuilderStore';
import PreguntaEditorCard from './PreguntaEditorCard';
import { BookOpen, Trash2 } from 'lucide-react';

function ExamenBuilder() {
  const { titulo, descripcion, preguntas, setTitulo, setDescripcion, removePregunta, addPregunta } = useExamenBuilderStore();

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
      {/* Lista de preguntas */}
            <div className="space-y-6">
              {preguntas.length === 0 ? (
                <div className="text-center py-8 text-gray-500 dark:text-gray-400 border-2 border-dashed border-gray-300 dark:border-gray-700 rounded-lg">
                  No hay preguntas en este examen todavía. Importá un JSON o agregalas manualmente abajo.
                </div>
              ) : (
                preguntas.map((pregunta, index) => (
                  <div
                    key={index}
                    className="relative bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-5 shadow-sm"
                  >
                    {/* Encabezado de la pregunta con botón de eliminar */}
                    <div className="flex justify-between items-center mb-4 border-b border-gray-100 dark:border-gray-700 pb-2">
                      <span className="font-semibold text-gray-700 dark:text-gray-300 flex items-center">
                        Pregunta {index + 1}
                        <span className="ml-3 text-xs font-normal px-2 py-1 bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 rounded-full">
                          {pregunta.tipo.replace('_', ' ')}
                        </span>
                      </span>
                      <button
                        type="button"
                        onClick={() => removePregunta(index)}
                        className="text-red-500 hover:text-red-700 dark:hover:text-red-400 transition-colors p-1"
                        title="Eliminar pregunta"
                        data-testid={`eliminar-pregunta-${index}`}
                      >
                        <Trash2 className="h-5 w-5" />
                      </button>
                    </div>

                    {/* Editor de los campos de la pregunta */}
                    <PreguntaEditorCard pregunta={pregunta} index={index} />
                  </div>
                ))
              )}
            </div>

            {/* === BOTONERA MODO MANUAL === */}
            <div className="mt-8 border-t border-gray-200 dark:border-gray-700 pt-6">
              <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">
                Agregar nueva pregunta
              </h4>
              <div className="flex flex-wrap gap-3">
                <button
                  type="button"
                  onClick={() => addPregunta({ tipo: 'MULTIPLE_CHOICE', texto: '', opciones: ['', '', '', ''], respuestaCorrecta: '' })}
                  className="px-4 py-2 text-sm font-medium bg-indigo-50 text-indigo-700 rounded-lg hover:bg-indigo-100 dark:bg-indigo-900/30 dark:text-indigo-400 dark:hover:bg-indigo-900/50 transition-colors"
                >
                  + Multiple Choice
                </button>
                <button
                  type="button"
                  onClick={() => addPregunta({ tipo: 'VERDADERO_FALSO', texto: '', respuestaCorrecta: true })}
                  className="px-4 py-2 text-sm font-medium bg-emerald-50 text-emerald-700 rounded-lg hover:bg-emerald-100 dark:bg-emerald-900/30 dark:text-emerald-400 dark:hover:bg-emerald-900/50 transition-colors"
                >
                  + Verdadero / Falso
                </button>
                <button
                  type="button"
                  onClick={() => addPregunta({ tipo: 'DESARROLLO_DETERMINISTICO', texto: '', respuestaEsperadaExacta: '' })}
                  className="px-4 py-2 text-sm font-medium bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 dark:bg-blue-900/30 dark:text-blue-400 dark:hover:bg-blue-900/50 transition-colors"
                >
                  + Respuesta Exacta
                </button>
                <button
                  type="button"
                  onClick={() => addPregunta({ tipo: 'DESARROLLO_NO_DETERMINISTICO', texto: '', rubricaEvaluacion: '' })}
                  className="px-4 py-2 text-sm font-medium bg-purple-50 text-purple-700 rounded-lg hover:bg-purple-100 dark:bg-purple-900/30 dark:text-purple-400 dark:hover:bg-purple-900/50 transition-colors"
                >
                  + Desarrollo Libre
                </button>
              </div>
            </div>
          </div>
        );
      }

      export default ExamenBuilder;
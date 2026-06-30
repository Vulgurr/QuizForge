import { useExamenBuilderStore } from '../../stores/examenBuilderStore';
import type { PreguntaDraftDTO } from '../../types';

interface PreguntaEditorCardProps {
  pregunta: PreguntaDraftDTO;
  index: number;
}

function PreguntaEditorCard({ pregunta, index }: PreguntaEditorCardProps) {
  const { updatePregunta } = useExamenBuilderStore();

  const handleFieldChange = (field: keyof PreguntaDraftDTO, value: any) => {
    updatePregunta(index, { ...pregunta, [field]: value });
  };

  return (
    <div className="space-y-3" data-testid={`pregunta-editor-${index}`}>
      {/* Texto de la pregunta */}
      <div>
        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          Texto
        </label>
        <textarea
          value={pregunta.texto}
          onChange={(e) => handleFieldChange('texto', e.target.value)}
          rows={2}
          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
          data-testid={`pregunta-texto-${index}`}
        />
      </div>

      {/* Campos específicos según tipo */}
      {pregunta.tipo === 'MULTIPLE_CHOICE' && (
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Opciones (separadas por coma)
          </label>
          <input
            type="text"
            value={pregunta.opciones?.join(', ') || ''}
            onChange={(e) => handleFieldChange('opciones', e.target.value.split(',').map(o => o.trim()))}
            placeholder="Opción A, Opción B, Opción C"
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            data-testid={`pregunta-opciones-${index}`}
          />
          
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Respuesta correcta
          </label>
          <input
            type="text"
            value={pregunta.respuestaCorrecta as string || ''}
            onChange={(e) => handleFieldChange('respuestaCorrecta', e.target.value)}
            placeholder="Opción correcta"
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            data-testid={`pregunta-respuesta-correcta-${index}`}
          />
        </div>
      )}

      {pregunta.tipo === 'VERDADERO_FALSO' && (
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Respuesta correcta
          </label>
          <select
            value={pregunta.respuestaCorrecta ? 'true' : 'false'}
            onChange={(e) => handleFieldChange('respuestaCorrecta', e.target.value === 'true')}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            data-testid={`pregunta-vf-${index}`}
          >
            <option value="true">Verdadero</option>
            <option value="false">Falso</option>
          </select>
        </div>
      )}

      {pregunta.tipo === 'DESARROLLO_DETERMINISTICO' && (
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Respuesta esperada exacta
          </label>
          <textarea
            value={pregunta.respuestaEsperadaExacta || ''}
            onChange={(e) => handleFieldChange('respuestaEsperadaExacta', e.target.value)}
            rows={2}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
            data-testid={`pregunta-respuesta-esperada-${index}`}
          />
        </div>
      )}

      {pregunta.tipo === 'DESARROLLO_NO_DETERMINISTICO' && (
        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Rúbrica de evaluación
          </label>
          <textarea
            value={pregunta.rubricaEvaluacion || ''}
            onChange={(e) => handleFieldChange('rubricaEvaluacion', e.target.value)}
            rows={3}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
            data-testid={`pregunta-rubrica-${index}`}
          />
        </div>
      )}
    </div>
  );
}

export default PreguntaEditorCard;

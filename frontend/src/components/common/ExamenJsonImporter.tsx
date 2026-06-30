import { useState } from 'react';
import { useExamenBuilderStore } from '../../stores/examenBuilderStore';
import { examenDraftSchema } from '../../schemas';
import { Copy, Check, Upload, AlertCircle } from 'lucide-react';
import { ZodError } from 'zod';

function ExamenJsonImporter() {
  const [jsonInput, setJsonInput] = useState('');
  const [copied, setCopied] = useState(false);
  const { cargarDesdeJson, errorImportacion, setErrorImportacion } = useExamenBuilderStore();

  const promptTemplate = `Actúa como un profesor. Genera un examen en formato JSON estricto con esta estructura:
{
  "titulo": "Título del Examen",
  "descripcion": "Descripción opcional",
  "preguntas": [
    {
      "texto": "Pregunta aquí",
      "tipo": "MULTIPLE_CHOICE",
      "opciones": ["Opción A", "Opción B", "Opción C"],
      "respuestaCorrecta": "Opción A"
    }
  ]
}

Tipos de preguntas disponibles:
- MULTIPLE_CHOICE: Array de opciones + respuesta correcta (string)
- VERDADERO_FALSO: respuestaCorrecta (boolean)
- DESARROLLO_DETERMINISTICO: respuestaEsperadaExacta (string)
- DESARROLLO_NO_DETERMINISTICO: rubricaEvaluacion (string)

No incluyas texto adicional fuera del JSON.`;

  const handleCopy = () => {
    navigator.clipboard.writeText(promptTemplate);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleProcess = () => {
    setErrorImportacion(null);

    try {
      const parsed = JSON.parse(jsonInput);

      // Validamos contra el esquema completo del examen
      examenDraftSchema.parse(parsed);

      cargarDesdeJson(jsonInput);
    } catch (error) {
      if (error instanceof ZodError) {
        // Usamos .issues para evitar el crash de undefined
        const firstError = error.issues[0];
        setErrorImportacion(`Error de validación: ${firstError.path.join('.')} - ${firstError.message}`);
      } else if (error instanceof SyntaxError) {
        setErrorImportacion('JSON inválido. Verifica la sintaxis.');
      } else {
        setErrorImportacion('Error al procesar el JSON');
      }
    }
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6" data-testid="json-importer">
      <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4 flex items-center">
        <Upload className="h-5 w-5 mr-2" />
        Importar desde JSON
      </h2>

      {/* Plantilla */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300">
            Prompt para IA
          </h3>
          <button
            onClick={handleCopy}
            className="flex items-center text-sm text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
            data-testid="copy-prompt-button"
          >
            {copied ? (
              <>
                <Check className="h-4 w-4 mr-1" />
                Copiado
              </>
            ) : (
              <>
                <Copy className="h-4 w-4 mr-1" />
                Copiar
              </>
            )}
          </button>
        </div>
        <div className="bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg p-4">
          <pre className="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-wrap font-mono">
            {promptTemplate}
          </pre>
        </div>
      </div>

      {/* Input JSON */}
      <div>
        <label htmlFor="json-input" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Pega el JSON generado
        </label>
        <textarea
          id="json-input"
          value={jsonInput}
          onChange={(e) => setJsonInput(e.target.value)}
          placeholder='{
  "titulo": "Capitales de Europa",
  "descripcion": "Prueba rápida de geografía",
  "preguntas": [
    {
      "texto": "¿Cuál es la capital de Francia?",
      "tipo": "MULTIPLE_CHOICE",
      "opciones": ["Londres", "París", "Berlín", "Madrid"],
      "respuestaCorrecta": "París"
    }
  ]
}'
          className="w-full h-64 p-4 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white font-mono text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
          data-testid="json-textarea"
        />
      </div>

      {/* Error */}
      {errorImportacion && (
        <div className="mt-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 flex items-start" data-testid="import-error">
          <AlertCircle className="h-5 w-5 text-red-600 dark:text-red-400 mr-2 mt-0.5 flex-shrink-0" />
          <p className="text-sm text-red-600 dark:text-red-400">{errorImportacion}</p>
        </div>
      )}

      {/* Botón procesar */}
      <button
        onClick={handleProcess}
        disabled={!jsonInput.trim()}
        className="mt-4 w-full px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        data-testid="process-json-button"
      >
        Procesar JSON
      </button>
    </div>
  );
}

export default ExamenJsonImporter;
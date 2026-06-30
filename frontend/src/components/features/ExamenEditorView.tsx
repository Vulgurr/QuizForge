import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../layout/Navbar';
import ExamenJsonImporter from '../common/ExamenJsonImporter';
import ExamenBuilder from '../common/ExamenBuilder';
import { useExamenBuilderStore } from '../../stores/examenBuilderStore';
import { examenService } from '../../services';
import { examenDraftSchema } from '../../schemas';
import { ArrowLeft, Save, Loader2 } from 'lucide-react';
import { ZodError } from 'zod';

function ExamenEditorView() {
  const navigate = useNavigate();
  const { titulo, descripcion, preguntas, errorImportacion, limpiarBorrador } = useExamenBuilderStore();
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  // Guard para prevenir pérdida de datos
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (preguntas.length > 0) {
        e.preventDefault();
        e.returnValue = '';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [preguntas.length]);

  const handleSave = async () => {
    // Validar con Zod antes de guardar
    try {
      examenDraftSchema.parse({
        titulo,
        descripcion,
        preguntas,
      });
    } catch (error) {
      if (error instanceof ZodError) {
        const firstError = error.errors[0];
        setSaveError(`Error de validación: ${firstError.message}`);
        return;
      }
    }

    setIsSaving(true);
    setSaveError(null);

    try {
      // Convertir preguntas al formato esperado por el backend
      const preguntasBackend = preguntas.map((p) => {
        const base = {
          texto: p.texto,
          tipo: p.tipo,
        };

        switch (p.tipo) {
          case 'MULTIPLE_CHOICE':
            return {
              ...base,
              opciones: p.opciones,
              respuestaCorrecta: p.respuestaCorrecta,
            };
          case 'VERDADERO_FALSO':
            return {
              ...base,
              respuestaCorrecta: p.respuestaCorrecta,
            };
          case 'DESARROLLO_DETERMINISTICO':
            return {
              ...base,
              respuestaEsperadaExacta: p.respuestaEsperadaExacta,
            };
          case 'DESARROLLO_NO_DETERMINISTICO':
            return {
              ...base,
              rubricaEvaluacion: p.rubricaEvaluacion,
            };
          default:
            return base;
        }
      });

      const examenRequest = {
        titulo,
        descripcion,
        categoriaId: 1, // Placeholder - debería seleccionarse de un dropdown
        preguntas: preguntasBackend,
      };

      await examenService.crear(examenRequest);
      limpiarBorrador();
      navigate('/dashboard');
    } catch (error) {
      console.error('Error guardando examen:', error);
      setSaveError('Error al guardar el examen. Por favor, intenta nuevamente.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    if (preguntas.length > 0 && !window.confirm('¿Estás seguro de que deseas cancelar? Perderás los cambios no guardados.')) {
      return;
    }
    limpiarBorrador();
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <Navbar />
      
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center">
            <button
              onClick={handleCancel}
              className="mr-4 p-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
              data-testid="cancel-button"
            >
              <ArrowLeft className="h-6 w-6" />
            </button>
            <div>
              <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
                Crear Nuevo Examen
              </h1>
              <p className="text-gray-600 dark:text-gray-400 mt-1">
                Importa desde JSON o edita manualmente
              </p>
            </div>
          </div>
          
          <button
            onClick={handleSave}
            disabled={isSaving || preguntas.length === 0}
            className="inline-flex items-center px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            data-testid="save-examen-button"
          >
            {isSaving ? (
              <>
                <Loader2 className="h-5 w-5 mr-2 animate-spin" />
                Guardando...
              </>
            ) : (
              <>
                <Save className="h-5 w-5 mr-2" />
                Guardar Examen
              </>
            )}
          </button>
        </div>

        {saveError && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 mb-6">
            <p className="text-red-600 dark:text-red-400">{saveError}</p>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Importador JSON */}
          <div>
            <ExamenJsonImporter />
          </div>

          {/* Editor visual */}
          <div>
            <ExamenBuilder />
          </div>
        </div>
      </div>
    </div>
  );
}

export default ExamenEditorView;

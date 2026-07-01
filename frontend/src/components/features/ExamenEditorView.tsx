import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Navbar from '../layout/Navbar';
import ExamenJsonImporter from '../common/ExamenJsonImporter';
import ExamenBuilder from '../common/ExamenBuilder';
import CategoriaSelector from '../common/CategoriaSelector';
import { useExamenBuilderStore } from '../../stores/examenBuilderStore';
import { examenService, categoriaService } from '../../services';
import { examenDraftSchema } from '../../schemas';
import type { CategoriaRequestDTO, ExamenResponseDTO } from '../../types';
import { ArrowLeft, Save, Loader2, Edit, FileJson } from 'lucide-react';
import { ZodError } from 'zod';

function ExamenEditorView() {
  const navigate = useNavigate();
  const { slug } = useParams<{ slug?: string }>();
    const isEditMode = !!slug;
  
  // Extraemos examenId y setExamenId del store
    const { examenId, setExamenId, titulo, descripcion, categoriaId, preguntas, errorImportacion, limpiarBorrador, setCategoriaId, setTitulo, setDescripcion, setPreguntas } = useExamenBuilderStore();
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [categoriaError, setCategoriaError] = useState<string | null>(null);
  const [modoManual, setModoManual] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

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

  // Cargar datos del examen en modo edición
  // Cargar datos del examen en modo edición
    useEffect(() => {
      // 1. Envolvemos todo en una función asíncrona
      const cargarExamen = async () => {
        if (!slug) return; // Si no hay slug, estamos creando, no editando

        try {
          setIsLoading(true);
          // Ahora sí podemos usar await tranquilamente
          const examen = await examenService.obtenerPorSlug(slug);

          // Cargar datos en el store (incluyendo el ID para cuando guardemos)
          setExamenId(examen.id);
          setTitulo(examen.titulo);
          setDescripcion(examen.descripcion || '');
          setCategoriaId(examen.categoriaId);

          // Convertir preguntas del backend al formato del store
          const preguntasStore = examen.preguntas.map((p) => {
            const base: any = {
              texto: p.texto,
              tipo: p.tipo,
            };

            switch (p.tipo) {
              case 'MULTIPLE_CHOICE':
                return {
                  ...base,
                  opciones: (p as any).opciones,
                  respuestaCorrecta: (p as any).respuestaCorrecta,
                };
              case 'VERDADERO_FALSO':
                return {
                  ...base,
                  respuestaCorrecta: (p as any).respuestaCorrecta,
                };
              case 'DESARROLLO_DETERMINISTICO':
                return {
                  ...base,
                  respuestaEsperadaExacta: (p as any).respuestaEsperadaExacta,
                };
              case 'DESARROLLO_NO_DETERMINISTICO':
                return {
                  ...base,
                  rubricaEvaluacion: (p as any).rubricaEvaluacion,
                };
              default:
                return base;
            }
          });

          setPreguntas(preguntasStore);
        } catch (error: any) {
          console.error('Error cargando el examen para edición:', error);
        } finally {
          setIsLoading(false);
        }
      };

      // 2. Llamamos a la función asíncrona
      cargarExamen();
    }, [slug, setExamenId, setTitulo, setDescripcion, setCategoriaId, setPreguntas]);
    // Le agregamos el slug a los parámetros que recibimos del selector
      const handleCrearCategoria = async (nombre: string, descripcion?: string, apodosStr?: string) => {
          try {
            // 1. Convertimos el string separado por comas en un array limpio
            const apodosArray = apodosStr
              ? apodosStr.split(',').map(a => a.trim()).filter(a => a.length > 0)
              : [];

            const categoriaRequest = {
              nombre,
              descripcion,
              apodos: apodosArray,
            };

            const nuevaCategoria = await categoriaService.crear(categoriaRequest as CategoriaRequestDTO);
            setCategoriaId(nuevaCategoria.id);
            setCategoriaError(null);
            return nuevaCategoria;
          } catch (error) {
            console.error('Error creando categoría:', error);
            setSaveError('Error al crear la categoría. Por favor, intenta nuevamente.');
            throw error;
          }
        };

  const handleSave = async () => {
    // 1. Limpiar errores previos
    setSaveError(null);
    setCategoriaError(null);

    // 2. Validar Título
    if (!titulo.trim()) {
      setSaveError('El examen debe tener un título.');
      return;
    }

    // 3. Validar Categoría
    if (!categoriaId) {
      setCategoriaError('Debes seleccionar una categoría');
      return;
    }

    // 4. Validar cantidad de preguntas
    if (preguntas.length === 0) {
      setSaveError('El examen debe tener al menos una pregunta.');
      return;
    }

    // 5. Validar cada pregunta individualmente
    for (let i = 0; i < preguntas.length; i++) {
      const p = preguntas[i];
      const num = i + 1;

      if (!p.texto.trim()) {
        setSaveError(`La pregunta ${num} no tiene texto. Por favor, escribí el enunciado.`);
        return;
      }

      switch (p.tipo) {
        case 'MULTIPLE_CHOICE':
          if (!p.opciones || p.opciones.length < 2 || p.opciones.some(opt => !opt.trim())) {
            setSaveError(`La pregunta ${num} debe tener al menos 2 opciones válidas y no vacías.`);
            return;
          }
          if (!p.respuestaCorrecta || typeof p.respuestaCorrecta !== 'string' || !p.respuestaCorrecta.trim()) {
            setSaveError(`La pregunta ${num} debe tener especificada la respuesta correcta.`);
            return;
          }
          if (!p.opciones.includes(p.respuestaCorrecta.trim())) {
            setSaveError(`En la pregunta ${num}, la respuesta correcta ("${p.respuestaCorrecta}") no coincide exactamente con ninguna de las opciones ingresadas.`);
            return;
          }
          break;
        case 'VERDADERO_FALSO':
          if (p.respuestaCorrecta === undefined || p.respuestaCorrecta === null) {
            setSaveError(`La pregunta ${num} debe tener una respuesta (Verdadero o Falso).`);
            return;
          }
          break;
        case 'DESARROLLO_DETERMINISTICO':
          if (!p.respuestaEsperadaExacta || !p.respuestaEsperadaExacta.trim()) {
            setSaveError(`La pregunta ${num} debe tener una respuesta esperada exacta.`);
            return;
          }
          break;
        case 'DESARROLLO_NO_DETERMINISTICO':
          if (!p.rubricaEvaluacion || !p.rubricaEvaluacion.trim()) {
            setSaveError(`La pregunta ${num} debe tener una rúbrica de evaluación detallada.`);
            return;
          }
          break;
      }
    }

    // 6. Validación final con Zod
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
        categoriaId,
        preguntas: preguntasBackend,
      };

      if (isEditMode && examenId) {
              await examenService.actualizar(examenId, examenRequest);
            } else {
              await examenService.crear(examenRequest);
            }

      limpiarBorrador();
      navigate('/dashboard');
    } catch (error: any) {
      const statusCode = error?.response?.status || error?.status;

      if (statusCode === 409) {
        setSaveError('Ya existe un examen con ese nombre. Por favor, elegí un título diferente.');
        return;
      }

      const errorData = error?.response?.data || error?.data;
      if (errorData) {
        if (typeof errorData === 'string') {
          setSaveError(errorData);
          return;
        } else if (errorData.message) {
          setSaveError(errorData.message);
          return;
        }
      }

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
                {isEditMode ? 'Editar Examen' : 'Crear Nuevo Examen'}
              </h1>
              <p className="text-gray-600 dark:text-gray-400 mt-1">
                Importa desde JSON o edita manualmente
              </p>
            </div>
          </div>

          {/* Toggle modo manual */}
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setModoManual(!modoManual)}
              className={`flex items-center px-4 py-2 rounded-lg transition-colors ${
                modoManual
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
              }`}
              data-testid="modo-manual-toggle"
            >
              <Edit className="h-4 w-4 mr-2" />
              Ingreso Manual
            </button>
            <button
              onClick={() => setModoManual(!modoManual)}
              className={`flex items-center px-4 py-2 rounded-lg transition-colors ${
                !modoManual
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600'
              }`}
              data-testid="modo-json-toggle"
            >
              <FileJson className="h-4 w-4 mr-2" />
              Importar JSON
            </button>
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
                  {isEditMode ? 'Actualizar Examen' : 'Crear Examen'}
                </>
              )}
            </button>
          </div>
        </div>

        {saveError && (
          <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 mb-6">
            <p className="text-red-600 dark:text-red-400">{saveError}</p>
          </div>
        )}

        {/* Layout condicional según modo */}
        {modoManual ? (
          // Modo manual: ExamenBuilder ocupa todo el ancho
          <div className="space-y-6">
            <CategoriaSelector
              selectedCategoriaId={categoriaId}
              onCategoriaSelect={setCategoriaId}
              onCrearCategoria={handleCrearCategoria}
              error={categoriaError}
            />
            <ExamenBuilder />
          </div>
        ) : (
          // Modo JSON: Grid con ambos componentes
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="space-y-6">
              <CategoriaSelector
                selectedCategoriaId={categoriaId}
                onCategoriaSelect={setCategoriaId}
                onCrearCategoria={handleCrearCategoria}
                error={categoriaError}
              />
              <ExamenJsonImporter />
            </div>

            <div>
              <ExamenBuilder />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default ExamenEditorView;
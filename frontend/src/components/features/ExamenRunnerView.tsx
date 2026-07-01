import { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { examenService } from '../../services';
import { useQuizRunnerStore } from '../../stores/quizRunnerStore';
import type { ExamenResponseDTO, PreguntaConTipo } from '../../types';
import { usePageVisibility } from '../../hooks/usePageVisibility';
import { Clock, ChevronLeft, ChevronRight, CheckCircle, XCircle } from 'lucide-react';

function ExamenRunnerView() {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const isVisible = usePageVisibility();
  
  const [examen, setExamen] = useState<ExamenResponseDTO | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showResultModal, setShowResultModal] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const { answers, isSubmitted, score, setAnswer, submitExam, resetQuiz, setExamenId } = useQuizRunnerStore();
  
  // Timer state
  const [elapsedTime, setElapsedTime] = useState(0);
  const submittedRef = useRef(isSubmitted);
  submittedRef.current = isSubmitted;
  useEffect(() => {
      return () => {
          if (!submittedRef.current) {
              resetQuiz();
          }
      };
  }, [resetQuiz]);
  // Fetch examen
  useEffect(() => {
    const fetchExamen = async () => {
      if (!slug) return;

      try {
        setIsLoading(true);
        const examenData = await examenService.obtenerPorSlug(slug);
        setExamen(examenData);
        setExamenId(examenData.id);
        setError(null);
      } catch (err) {
        console.error('Error cargando examen:', err);
        setError('Error al cargar el examen');
      } finally {
        setIsLoading(false);
      }
    };

    fetchExamen();
  }, [slug, setExamenId]);

  // Timer effect - solo avanza cuando la página es visible
  useEffect(() => {
    if (!isVisible || isSubmitted || showResultModal) return;

    const interval = setInterval(() => {
      setElapsedTime((prev) => prev + 1);
    }, 1000);

    return () => clearInterval(interval);
  }, [isVisible, isSubmitted, showResultModal]);

  // Guard para prevenir pérdida de datos
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (Object.keys(answers).length > 0 && !isSubmitted) {
        e.preventDefault();
        e.returnValue = '';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [Object.keys(answers).length, isSubmitted]);


  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleAnswer = (preguntaId: number, respuesta: string) => {
    setAnswer(preguntaId, respuesta);
  };
    const handleSubmit = async () => {
        if (!examen || Object.keys(answers).length === 0) return;

        setIsSubmitting(true);
        try {
          const respuestasArray = Object.entries(answers).map(([preguntaIdStr, valor]) => ({
            preguntaId: Number(preguntaIdStr),
            valorDichoPorElUsuario: valor,
          }));

          const result = await examenService.corregir(examen.id, { respuestas: respuestasArray });


          submitExam(result);
          setShowResultModal(true);
        } catch (err) {
          // ...
        }
      };

  const handleNext = () => {
    if (examen && currentIndex < examen.preguntas.length - 1) {
      setCurrentIndex(currentIndex + 1);
    }
  };

  const handlePrevious = () => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
    }
  };

  const handleBackToCategories = () => {
    resetQuiz();
    navigate(`/categorias/${examen?.categoriaId ? '' : ''}`);
  };

  const renderPregunta = (pregunta: PreguntaConTipo) => {
    const currentAnswer = answers[pregunta.id];

    switch (pregunta.tipo) {
      case 'MULTIPLE_CHOICE':
        return (
          <div className="space-y-3" data-testid={`pregunta-multiple-choice-${pregunta.id}`}>
            {pregunta.opciones.map((opcion, idx) => (
              <button
                key={idx}
                onClick={() => handleAnswer(pregunta.id, opcion)}
                disabled={isSubmitted}
                className={`w-full text-left p-4 rounded-lg border-2 transition-colors ${
                  currentAnswer === opcion
                    ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-900/20'
                    : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
                } ${isSubmitted ? 'cursor-not-allowed opacity-60' : ''}`}
                data-testid={`opcion-${idx}`}
              >
                <div className="flex items-center">
                  <div className={`w-5 h-5 rounded-full border-2 mr-3 flex items-center justify-center ${
                    currentAnswer === opcion ? 'border-indigo-500' : 'border-gray-300'
                  }`}>
                    {currentAnswer === opcion && (
                      <div className="w-3 h-3 rounded-full bg-indigo-500" />
                    )}
                  </div>
                  <span className="text-gray-900 dark:text-white">{opcion}</span>
                </div>
              </button>
            ))}
          </div>
        );

      case 'VERDADERO_FALSO':
        return (
          <div className="flex gap-4" data-testid={`pregunta-vf-${pregunta.id}`}>
            <button
              onClick={() => handleAnswer(pregunta.id, 'true')}
              disabled={isSubmitted}
              className={`flex-1 p-4 rounded-lg border-2 transition-colors ${
                currentAnswer === 'true'
                  ? 'border-green-500 bg-green-50 dark:bg-green-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
              } ${isSubmitted ? 'cursor-not-allowed opacity-60' : ''}`}
              data-testid="opcion-verdadero"
            >
              <div className="flex items-center justify-center">
                <CheckCircle className={`h-6 w-6 mr-2 ${currentAnswer === 'true' ? 'text-green-500' : 'text-gray-400'}`} />
                <span className="text-gray-900 dark:text-white font-medium">Verdadero</span>
              </div>
            </button>
            <button
              onClick={() => handleAnswer(pregunta.id, 'false')}
              disabled={isSubmitted}
              className={`flex-1 p-4 rounded-lg border-2 transition-colors ${
                currentAnswer === 'false'
                  ? 'border-red-500 bg-red-50 dark:bg-red-900/20'
                  : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
              } ${isSubmitted ? 'cursor-not-allowed opacity-60' : ''}`}
              data-testid="opcion-falso"
            >
              <div className="flex items-center justify-center">
                <XCircle className={`h-6 w-6 mr-2 ${currentAnswer === 'false' ? 'text-red-500' : 'text-gray-400'}`} />
                <span className="text-gray-900 dark:text-white font-medium">Falso</span>
              </div>
            </button>
          </div>
        );

      case 'DESARROLLO_DETERMINISTICO':
      case 'DESARROLLO_NO_DETERMINISTICO':
        return (
          <div data-testid={`pregunta-desarrollo-${pregunta.id}`}>
            <textarea
              value={currentAnswer || ''}
              onChange={(e) => handleAnswer(pregunta.id, e.target.value)}
              disabled={isSubmitted}
              rows={6}
              className="w-full p-4 border-2 border-gray-200 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
              placeholder="Escribe tu respuesta aquí..."
              data-testid="respuesta-desarrollo"
            />
          </div>
        );

      default:
        return <div>Tipo de pregunta no soportado</div>;
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
          <p className="mt-4 text-gray-600 dark:text-gray-400">Cargando examen...</p>
        </div>
      </div>
    );
  }

  if (error || !examen) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-6 max-w-md">
          <p className="text-red-600 dark:text-red-400">{error || 'Examen no encontrado'}</p>
          <button
            onClick={handleBackToCategories}
            className="mt-4 text-indigo-600 hover:text-indigo-500 dark:text-indigo-400"
          >
            Volver
          </button>
        </div>
      </div>
    );
  }

  const currentPregunta = examen.preguntas[currentIndex];
  const progress = ((currentIndex + 1) / examen.preguntas.length) * 100;
  const answeredCount = Object.keys(answers).length;
  const totalQuestions = examen.preguntas.length;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header fijo */}
      <div className="sticky top-0 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 z-40">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            {/* Timer */}
            {!isSubmitted && !showResultModal && (
              <div className="flex items-center space-x-2 text-gray-600 dark:text-gray-400">
                <Clock className="h-5 w-5" />
                <span className="font-mono text-lg" data-testid="timer">
                  {formatTime(elapsedTime)}
                </span>
              </div>
            )}

            {/* Progress */}
            <div className="flex-1 mx-8">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-gray-600 dark:text-gray-400">
                  Pregunta {currentIndex + 1} de {totalQuestions}
                </span>
                <span className="text-sm text-gray-600 dark:text-gray-400">
                  {answeredCount} de {totalQuestions} respondidas
                </span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div
                  className="bg-indigo-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${progress}%` }}
                  data-testid="progress-bar"
                />
              </div>
            </div>

            {/* Botón entregar */}
            {!isSubmitted && !showResultModal && (
              <button
                onClick={handleSubmit}
                disabled={Object.keys(answers).length === 0 || isSubmitting}
                className="px-6 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                data-testid="submit-examen"
              >
                {isSubmitting ? 'Enviando...' : 'Entregar'}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Contenido principal */}
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">
            {currentPregunta.texto}
          </h2>
          
          {renderPregunta(currentPregunta)}

          {/* Navegación */}
          {!isSubmitted && !showResultModal && (
            <div className="flex justify-between mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
              <button
                onClick={handlePrevious}
                disabled={currentIndex === 0}
                className="flex items-center px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                data-testid="previous-question"
              >
                <ChevronLeft className="h-5 w-5 mr-2" />
                Anterior
              </button>
              
              <button
                onClick={handleNext}
                disabled={currentIndex === examen.preguntas.length - 1}
                className="flex items-center px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                data-testid="next-question"
              >
                Siguiente
                <ChevronRight className="h-5 w-5 ml-2" />
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Modal de resultados */}
      {showResultModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full p-8" data-testid="result-modal">
            <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-4 text-center">
              ¡Examen completado!
            </h3>
            
            <div className="text-center mb-6">
              <div className="text-5xl font-bold text-indigo-600 dark:text-indigo-400 mb-2">
                {score?.toFixed(1) || '0.0'}
              </div>
              <p className="text-gray-600 dark:text-gray-400">Puntaje final</p>
            </div>

            <div className="space-y-3 mb-6">
              <div className="flex justify-between text-gray-600 dark:text-gray-400">
                <span>Total de preguntas:</span>
                <span className="font-medium text-gray-900 dark:text-white">{totalQuestions}</span>
              </div>
              <div className="flex justify-between text-gray-600 dark:text-gray-400">
                <span>Respondidas:</span>
                <span className="font-medium text-gray-900 dark:text-white">{answeredCount}</span>
              </div>
            </div>

            <button
              onClick={handleBackToCategories}
              className="w-full px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition-colors"
              data-testid="back-to-categories"
            >
              Volver a categorías
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default ExamenRunnerView;

// Tipos base para la aplicación QuizForge
// Basados en los DTOs del backend

// ==================== AUTH ====================
export interface AuthRequestDTO {
  email: string;
  password: string;
}

export interface AuthResponseDTO {
  token: string;
  rol: string;
}

// ==================== CATEGORIAS ====================
export interface CategoriaResponseDTO {
  id: number;
  nombre: string;
  descripcion: string;
  slug: string;
  apodos: string[];
}

// ==================== EXAMENES ====================
export interface ExamenResponseDTO {
  id: number;
  titulo: string;
  descripcion: string;
  slug: string;
  creadorId: number;
  categoriaId: number;
  creadoEn: string; // LocalDateTime formateado como ISO string
  preguntas: PreguntaDTO[];
}

export interface ExamenResumenDTO {
  id: number;
  titulo: string;
  descripcion: string;
  slug: string;
  creadorId: number;
  categoriaId: number;
  cantidadPreguntas: number; // <- Propiedad que manda el backend
}

export interface ExamenRequestDTO {
  titulo: string;
  descripcion: string;
  categoriaId: number;
  preguntas: PreguntaCreateDTO[];
}

export interface ExamenResumenDTO {
  id: number;
  titulo: string;
  slug: string;
  creadorId: number;
  categoriaId: number;
  creadoEn: string;
}

// ==================== PREGUNTAS ====================
export type PreguntaTipo = 
  | 'MULTIPLE_CHOICE'
  | 'VERDADERO_FALSO'
  | 'DESARROLLO_DETERMINISTICO'
  | 'DESARROLLO_NO_DETERMINISTICO';

export interface PreguntaDTO {
  id: number;
  texto: string;
  tipo: PreguntaTipo;
}

export interface PreguntaMultipleChoiceDTO extends PreguntaDTO {
  tipo: 'MULTIPLE_CHOICE';
  opciones: string[];
  respuestaCorrecta: string;
  examenId?: number;
}

export interface PreguntaVerdaderoFalsoDTO extends PreguntaDTO {
  tipo: 'VERDADERO_FALSO';
  respuestaCorrecta: boolean;
  examenId?: number;
}

export interface PreguntaDesarrolloDeterministicoDTO extends PreguntaDTO {
  tipo: 'DESARROLLO_DETERMINISTICO';
  respuestaEsperadaExacta: string;
  examenId?: number;
}

export interface PreguntaDesarrolloNoDeterministicoDTO extends PreguntaDTO {
  tipo: 'DESARROLLO_NO_DETERMINISTICO';
  rubricaEvaluacion: string;
  examenId?: number;
}

// Tipo unión para todas las preguntas
export type PreguntaConTipo = 
  | PreguntaMultipleChoiceDTO
  | PreguntaVerdaderoFalsoDTO
  | PreguntaDesarrolloDeterministicoDTO
  | PreguntaDesarrolloNoDeterministicoDTO;

// ==================== PREGUNTAS CREATE (para crear/editar) ====================
export interface PreguntaCreateDTO {
  texto: string;
  tipo: PreguntaTipo;
  examenId?: number;
}

export interface PreguntaMultipleChoiceCreateDTO extends PreguntaCreateDTO {
  tipo: 'MULTIPLE_CHOICE';
  opciones: string[];
  respuestaCorrecta: string;
}

export interface PreguntaVerdaderoFalsoCreateDTO extends PreguntaCreateDTO {
  tipo: 'VERDADERO_FALSO';
  respuestaCorrecta: boolean;
}

export interface PreguntaDesarrolloDeterministicoCreateDTO extends PreguntaCreateDTO {
  tipo: 'DESARROLLO_DETERMINISTICO';
  respuestaEsperadaExacta: string;
}

export interface PreguntaDesarrolloNoDeterministicoCreateDTO extends PreguntaCreateDTO {
  tipo: 'DESARROLLO_NO_DETERMINISTICO';
  rubricaEvaluacion: string;
}

// Tipo unión para crear preguntas
export type PreguntaCreateConTipo = 
  | PreguntaMultipleChoiceCreateDTO
  | PreguntaVerdaderoFalsoCreateDTO
  | PreguntaDesarrolloDeterministicoCreateDTO
  | PreguntaDesarrolloNoDeterministicoCreateDTO;

// ==================== RESPUESTAS ====================
export interface RespuestaClienteDTO {
  preguntaId: number;
  valorDichoPorElUsuario: string;
}

// ==================== CORRECCION ====================
export interface CorreccionResponseDTO {
  examenId: number;
  puntajeFinal: number;
  totalPreguntas: number;
  correctas: number;
}

export interface CorreccionRequestDTO {
  respuestas: RespuestaClienteDTO[];
}

// ==================== TIPOS PARA EL FRONTEND ====================
// Tipos específicos del frontend que no existen en el backend

export interface Usuario {
  id: number;
  email: string;
  rol: string;
}

// Tipo para el borrador de examen en el ExamenBuilderStore
export interface PreguntaDraftDTO {
  texto: string;
  tipo: PreguntaTipo;
  opciones?: string[];
  respuestaCorrecta?: string | boolean;
  respuestaEsperadaExacta?: string;
  rubricaEvaluacion?: string;
}

export interface ExamenDraft {
  titulo: string;
  descripcion: string;
  preguntas: PreguntaDraftDTO[];
}

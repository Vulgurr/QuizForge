import { z } from 'zod';
import type { PreguntaTipo } from '../types';

// ==================== AUTH SCHEMAS ====================
export const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(8, 'La contraseña debe tener al menos 6 caracteres'),
});

export const registerSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(8, 'La contraseña debe tener al menos 6 caracteres'),
});

// ==================== EXAMEN SCHEMAS ====================
export const examenRequestSchema = z.object({
  titulo: z.string().min(1, 'El título es obligatorio').max(200, 'El título no puede exceder 200 caracteres'),
  descripcion: z.string().max(500, 'La descripción no puede exceder 500 caracteres').optional(),
  categoriaId: z.number().int().positive('La categoría es obligatoria'),
  preguntas: z.array(z.any()).min(1, 'Debe haber al menos una pregunta'),
});

// ==================== PREGUNTA SCHEMAS ====================
const preguntaBaseSchema = z.object({
  texto: z.string().min(1, 'El texto de la pregunta es obligatorio'),
  tipo: z.enum(['MULTIPLE_CHOICE', 'VERDADERO_FALSO', 'DESARROLLO_DETERMINISTICO', 'DESARROLLO_NO_DETERMINISTICO']),
});

export const preguntaMultipleChoiceSchema = preguntaBaseSchema.extend({
  tipo: z.literal('MULTIPLE_CHOICE'),
  opciones: z.array(z.string()).min(2, 'Debe haber al menos 2 opciones').max(6, 'Máximo 6 opciones'),
  respuestaCorrecta: z.string().min(1, 'La respuesta correcta es obligatoria'),
});

export const preguntaVerdaderoFalsoSchema = preguntaBaseSchema.extend({
  tipo: z.literal('VERDADERO_FALSO'),
  respuestaCorrecta: z.boolean(),
});

export const preguntaDesarrolloDeterministicoSchema = preguntaBaseSchema.extend({
  tipo: z.literal('DESARROLLO_DETERMINISTICO'),
  respuestaEsperadaExacta: z.string().min(1, 'La respuesta esperada es obligatoria'),
});

export const preguntaDesarrolloNoDeterministicoSchema = preguntaBaseSchema.extend({
  tipo: z.literal('DESARROLLO_NO_DETERMINISTICO'),
  rubricaEvaluacion: z.string().min(1, 'La rúbrica de evaluación es obligatoria'),
});

// Schema unión para cualquier tipo de pregunta
export const preguntaSchema = z.discriminatedUnion('tipo', [
  preguntaMultipleChoiceSchema,
  preguntaVerdaderoFalsoSchema,
  preguntaDesarrolloDeterministicoSchema,
  preguntaDesarrolloNoDeterministicoSchema,
]);

// ==================== EXAMEN DRAFT SCHEMA (para importación JSON) ====================
export const examenDraftSchema = z.object({
  titulo: z.string().min(1, 'El título es obligatorio'),
  descripcion: z.string().optional(),
  preguntas: z.array(preguntaSchema).min(1, 'Debe haber al menos una pregunta'),
});

// ==================== RESPUESTA CLIENTE SCHEMA ====================
export const respuestaClienteSchema = z.object({
  preguntaId: z.number().int().positive('El ID de pregunta es inválido'),
  valorDichoPorElUsuario: z.string().min(1, 'La respuesta es obligatoria'),
});

export const correccionRequestSchema = z.object({
  respuestas: z.array(respuestaClienteSchema).min(1, 'Debe haber al menos una respuesta'),
});

// ==================== CATEGORIA SCHEMAS ====================
export const categoriaRequestSchema = z.object({
  nombre: z.string().min(1, 'El nombre es obligatorio').max(100, 'El nombre no puede exceder 100 caracteres'),
  descripcion: z.string().max(500, 'La descripción no puede exceder 500 caracteres').optional(),
  apodos: z.array(z.string()).optional(),
});

// ==================== TYPES ====================
export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type ExamenRequestFormData = z.infer<typeof examenRequestSchema>;
export type PreguntaMultipleChoiceFormData = z.infer<typeof preguntaMultipleChoiceSchema>;
export type PreguntaVerdaderoFalsoFormData = z.infer<typeof preguntaVerdaderoFalsoSchema>;
export type PreguntaDesarrolloDeterministicoFormData = z.infer<typeof preguntaDesarrolloDeterministicoSchema>;
export type PreguntaDesarrolloNoDeterministicoFormData = z.infer<typeof preguntaDesarrolloNoDeterministicoSchema>;
export type PreguntaFormData = z.infer<typeof preguntaSchema>;
export type ExamenDraftFormData = z.infer<typeof examenDraftSchema>;
export type RespuestaClienteFormData = z.infer<typeof respuestaClienteSchema>;
export type CorreccionRequestFormData = z.infer<typeof correccionRequestSchema>;
export type CategoriaRequestFormData = z.infer<typeof categoriaRequestSchema>;

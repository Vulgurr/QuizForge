# Reglas de Desarrollo para QuizForge

- Arquitectura: Clean Architecture.
- Lógica de Negocio: SIEMPRE en clases "Gestor". Los "Controller" solo delegan y retornan DTOs.
- Lenguaje: Java 21, Spring Boot 3.x.
- Código: Seguir principios SOLID.
- Estructura de paquetes: com.quizforge.backend.[model|repository|gestor|controller|dto]
- Módulo Examen: crear entidad `Examen` en `model`, `ExamenRepository` en `repository`, `ExamenGestor` en `gestor`, `ExamenController` en `controller`.
- Persistencia JPA: usar `@Entity`, `@Id`, `@GeneratedValue`, `@Repository`, `@Service`/`@Component` según corresponda.
- Las validaciones y lógica de negocio deben residir exclusivamente en el Gestor. Los Controladores solo delegan y retornan DTOs.

## Patrones Específicos
- **Polimorfismo en Preguntas:** La entidad `Pregunta` es una clase abstracta con estrategia de herencia `SINGLE_TABLE`. Toda nueva variante de pregunta debe heredar de esta clase base y definir su propio `DiscriminatorValue`. No se utilizan campos JSON dinámicos para metadatos.
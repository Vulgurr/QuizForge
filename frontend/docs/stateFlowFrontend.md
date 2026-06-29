# Gestión de Estado - QuizForge

## Objetivo

Este documento define la estrategia de gestión de estado de la aplicación **QuizForge**, especificando qué información debe almacenarse de forma global, qué datos pertenecen exclusivamente a una sesión de examen y qué estado debe permanecer local dentro de cada componente.

---

# 1. Estado Global (Zustand)

Estos datos deben ser accesibles desde cualquier parte de la aplicación y sobrevivir al cambio de páginas.

## AuthStore

Responsable de toda la autenticación del usuario.

### Estado

| Variable          | Tipo                 | Descripción                                         |
| ----------------- | -------------------- | --------------------------------------------------- |
| `token`           | `string \| null`     | JWT del usuario autenticado.                        |
| `userData`        | `{ id, email, rol }` | Información básica del usuario autenticado.         |
| `isAuthenticated` | `boolean`            | Valor derivado de la existencia de un token válido. |

### Responsabilidades

* Login.
* Logout.
* Persistencia del token.
* Obtener información del usuario autenticado.
* Invalidar automáticamente la sesión ante errores **401** o **403**.

---

## UIStore

Responsable de configuraciones globales relacionadas con la interfaz.

### Estado

| Variable      | Tipo             | Descripción                                                      |
| ------------- | ---------------- | ---------------------------------------------------------------- |
| `isDarkMode`  | `boolean`        | Preferencia de tema visual.                                      |
| `activeModal` | `string \| null` | Modal actualmente abierto para evitar superposición de ventanas. |

---

# 2. Estado de Sesión (Motor del Examen)

Este estado únicamente existe mientras el usuario está resolviendo un examen.

## QuizRunnerStore

### Estado

| Variable      | Tipo                  | Descripción                                           |
| ------------- | --------------------- | ----------------------------------------------------- |
| `answers`     | `Map<number, string>` | Relación entre ID de pregunta y respuesta elegida.    |
| `isSubmitted` | `boolean`             | Indica si el examen ya fue entregado.                 |
| `score`       | `number \| null`      | Puntaje calculado por el backend luego de la entrega. |

### Responsabilidades

* Almacenar las respuestas seleccionadas.
* Permitir modificar respuestas hasta la entrega.
* Bloquear la edición una vez enviado el examen.
* Guardar el resultado recibido del backend.
* Limpiar el estado cuando finaliza el examen o el usuario abandona la sesión.

---

# 3. Estado Local (useState)

Información que únicamente le interesa al componente donde se utiliza.

## AuthForms

Estado exclusivo de formularios de autenticación.

| Variable    | Descripción                            |
| ----------- | -------------------------------------- |
| `isLoading` | Indica si la petición está en curso.   |
| `error`     | Mensaje de error recibido del backend. |

---

## ExamenCard

Estado visual de una tarjeta de examen.

| Variable    | Descripción                                  |
| ----------- | -------------------------------------------- |
| `isHovered` | Controla efectos visuales al pasar el mouse. |

---

## Buscador

Estado temporal del campo de búsqueda.

| Variable      | Descripción                                                   |
| ------------- | ------------------------------------------------------------- |
| `searchQuery` | Texto ingresado por el usuario antes de disparar la búsqueda. |

---

# Reglas de Gestión de Estado

Estas reglas deben respetarse durante todo el desarrollo del frontend.

## 1. Estado Global

No utilizar `useState` para información que deba persistir entre páginas o ser compartida por múltiples componentes. En esos casos debe utilizarse **Zustand**.

---

## 2. Motor del Examen

Toda la lógica relacionada con la resolución y entrega del examen pertenece exclusivamente al componente **ExamenRunner**.

El estado de las respuestas debe mantenerse mientras el usuario realiza el examen y eliminarse una vez finalizado.


## ExamenBuilderStore

Representa el estado temporal de la creación/edición de un examen por parte del profesor.

### Estado

| Variable           | Tipo                        | Descripción                                        |
| ------------------ | --------------------------- | -------------------------------------------------- |
| `titulo`           | `string`                    | Título del examen en borrador.                     |
| `descripcion`      | `string`                    | Descripción del examen.                            |
| `preguntas`        | `Array<PreguntaDraftDTO>`   | Lista de preguntas generadas/editadas.             |
| `errorImportacion` | `string \| null`            | Mensaje de error si falla el parseo de Zod.        |

### Responsabilidades

* Almacenar el borrador temporal del examen para evitar pérdida de datos.
* Procesar y validar (`cargarDesdeJson`) la estructura JSON importada externamente.
* Limpiar el estado (`limpiarBorrador`) al salir o al guardar con éxito en el backend.

---

## 3. Autenticación

El **JWT** almacenado en `AuthStore` es la única fuente de verdad para determinar si un usuario se encuentra autenticado.

No deben existir estados paralelos como:

* `isLogged`
* `isGuest`
* `loggedUser`

Todo debe derivarse del token.

---

## 4. Manejo de errores de autenticación

Si cualquier petición al backend devuelve un código HTTP:

* **401 Unauthorized**
* **403 Forbidden**

el `AuthStore` debe:

1. Eliminar el token.
2. Limpiar los datos del usuario.
3. Ejecutar el logout.
4. Redirigir al usuario al flujo de autenticación.

---

# Principios Generales

* Cada dato debe tener una única fuente de verdad.
* El estado global debe mantenerse lo más pequeño posible.
* El estado local debe utilizarse únicamente para comportamiento interno de un componente.
* Evitar duplicar información entre Stores y componentes.
* Los Stores deben contener únicamente estado compartido y lógica de negocio relacionada.
* Los componentes deben ser lo más declarativos posible y delegar la lógica compleja al Store correspondiente.

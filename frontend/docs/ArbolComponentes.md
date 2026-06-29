# Árbol de Componentes: QuizForge Frontend

- `AppRouter` (Maneja las rutas web)
    - `Navbar` (Smart: Contiene la barra de búsqueda global y lee el estado de sesión)
        - `BuscadorCategorias` (Smart: Hace fetch a /api/categorias/buscar?apodo=...)
        - `UserMenuDropdown` (Dumb: Opciones de Login o link al Dashboard si está logueado)

    - `LandingView` (Vista principal `/`)
        - `HeroSection` (Dumb: Presentación y call-to-action)
        - `CategoriasDestacadasGrid` (Smart: Fetch a categorías populares)

    - `AuthView` (Layout para `/auth/login` y `/auth/register`)
        - `LoginForm` / `RegisterForm` (Smart: Manejan validaciones y POST al GestorAuth)

    - `CategoriaDetalleView` (Vista `/categorias/:slug`)
        - `HeaderCategoria` (Dumb: Título y descripción)
        - `ListaExamenes` (Smart: Fetch a /api/categorias/{slug}/examenes)
            - `ExamenCard` (Dumb: Muestra título, autor y botón "Rendir")

    - `ExamenRunnerView` (El motor donde se rinde el examen `/examenes/:slug/rendir`)
        - `ExamenHeader` (Dumb: Progreso y cronómetro opcional)
        - `MotorPreguntas` (Smart: Mantiene las respuestas locales del usuario)
            - `PreguntaRenderer` (Dumb: Componente polimórfico que renderiza V/F, Choice o Desarrollo según el 'tipo')
        - `ModalResultado` (Smart: Hace POST para entregar y muestra la nota recibida del backend)

    - `DashboardCreadorView` (Layout privado `/dashboard`)
      - `MisCategoriasGrid` (Smart: Fetch a las categorías donde el usuario aportó)
      - `PanelGestionExamenes` (Smart: Lista los exámenes propios de esa categoría)
      - `BotoneraAcciones` (Dumb: Editar, Borrar Pregunta, Borrar Examen)

        - `ExamenEditorView` (Vista principal `/dashboard/crear-examen`)
            - `ExamenJsonImporter` (Smart: Maneja el input del usuario, valida con Zod y despacha al ExamenBuilderStore)
            - `ExamenBuilder` (Smart: Lee las preguntas del Store y orquesta la lista)
                - `PreguntaEditorCard` (Dumb: Formulario individual para editar una pregunta existente)
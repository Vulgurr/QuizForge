# Configuración de Seguridad

## 1. `passwordEncoder()`

Define un **Bean** de `BCryptPasswordEncoder`, el algoritmo estándar utilizado para almacenar contraseñas de forma segura.

## 2. `securityFilterChain(HttpSecurity http)`

Es el componente principal de Spring Security encargado de definir las reglas que se aplicarán a todas las peticiones HTTP de la aplicación.

### Deshabilitación de CSRF

```java
csrf(AbstractHttpConfigurer::disable)
```

La protección contra **Cross-Site Request Forgery (CSRF)** se deshabilita porque la aplicación funciona como una API REST autenticada mediante JWT.

<details>
  <summary><b>¿Qué es CSRF?</b></summary>
El Cross-Site Request Forgery (Falsificación de Petición en Sitios Cruzados) es un ataque donde un usuario autenticado es engañado para ejecutar acciones no deseadas en una aplicación web en la que ya está logueado.

El punto clave es el navegador y las cookies:

Tradicionalmente, las aplicaciones web usaban Cookies de Sesión.
Cuando el navegador hace una petición a tubanco.com, automáticamente envía las cookies de esa sesión junto con la petición.
El atacante crea un sitio malicioso (sitio-hacker.com) que contiene un formulario o un script que hace una petición a [tubanco.com/transferir](https://tubanco.com/transferir).
El navegador, al ver que la petición va a tubanco.com, adjunta automáticamente la cookie de sesión del usuario.
El banco, al ver la cookie válida, cree que el usuario realmente quiere hacer la transferencia y la ejecuta. El usuario ni siquiera sabe que pasó.

¿Por qué JWT y REST no sufren (generalmente) este ataque?
Cuando pasas a una arquitectura REST con JWT, cambias la forma en que el cliente se identifica ante el servidor.

1. Adiós a las Cookies automáticas
A diferencia de las cookies de sesión tradicionales, un JWT (JSON Web Token) no es manejado automáticamente por el navegador. Cuando el servidor te envía el token, tú lo guardas manualmente (por ejemplo, en localStorage o sessionStorage) y tú debes añadirlo manualmente en el header de cada petición HTTP usando Authorization: Bearer <token>.

2. La barrera del Header
Como el navegador no sabe que debe adjuntar ese token a las peticiones, un atacante no puede simplemente engañar al navegador para que haga una petición "autenticada".

Si el sitio malicioso intenta hacer una petición a tu API, esa petición no llevará el header Authorization.

Tu servidor recibirá la petición, verá que no tiene el token y simplemente rechazará la solicitud (401 Unauthorized).

¿Cuándo SÍ deberías activar CSRF?
Aunque en el 99% de los casos de APIs REST con Bearer tokens no necesitas CSRF, hay una excepción:

Si guardas el JWT dentro de una Cookie (y no en el almacenamiento local):
Si decides guardar el JWT en una Cookie (por ejemplo, para que sea "HttpOnly" y evitar ataques XSS), entonces tu navegador sí volverá a enviar las cookies automáticamente en cada petición. En ese escenario, estarías expuesto a CSRF y tendrías que volver a activar la protección CSRF.

En resumen:
Desactivamos el CSRF porque estamos usando una estrategia Stateless (sin estado). Como el cliente tiene la obligación de enviar el token explícitamente en el header para cada petición, un sitio tercero no puede forzar al navegador a "firmar" esa petición de forma automática.

</details>

En este modelo:

* No se utilizan sesiones de servidor.
* No se utilizan cookies para autenticar usuarios.
* Cada petición envía explícitamente un token.

Por este motivo, la protección CSRF no resulta necesaria.

---

### Gestión de sesiones

```java
sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

La política `STATELESS` indica que el servidor no almacenará sesiones de usuario.

#### Consecuencias

* Cada solicitud es independiente.
* El servidor no recuerda usuarios autenticados entre peticiones.
* Todas las solicitudes protegidas deben incluir un JWT válido.

Este comportamiento es fundamental en arquitecturas REST modernas.

---

### Reglas de autorización

```java
authorizeHttpRequests(auth -> auth
    .requestMatchers(...).permitAll()
    .anyRequest().authenticated()
)
```

Aquí se definen los permisos de acceso a cada endpoint.

#### Endpoints públicos

```java
.requestMatchers(
    "/api/auth/**",
    "/api/examenes/**",
    "/api/categorias/**"
).permitAll()
```

Permite acceder sin autenticación a:

* Endpoints de autenticación.
* Consulta de exámenes.
* Consulta de categorías.

#### Endpoints protegidos

```java
.anyRequest().authenticated()
```

Indica que cualquier ruta que no haya sido declarada previamente como pública requerirá autenticación.

Para acceder a estas rutas el cliente deberá enviar un JWT válido en la cabecera correspondiente.

---

## Resumen

La configuración de seguridad implementa una API REST basada en JWT con las siguientes características:

* Contraseñas protegidas mediante BCrypt.
* Sin almacenamiento de sesiones en el servidor.
* Autenticación basada en tokens.
* Endpoints públicos definidos explícitamente.
* Protección automática del resto de las rutas.

Este enfoque es actualmente uno de los más utilizados para aplicaciones web modernas y APIs REST.


# Anotaciones y Componentes Utilizados

## 1. Controladores y Web (Manejan peticiones HTTP)

### `@RestController`

Le avisa a Spring que esa clase es la puerta de entrada para tu API y que devolverá datos (generalmente en formato JSON).

### `@RequestMapping`, `@PostMapping`, `@GetMapping`, `@DeleteMapping`

Definen las rutas de tu API y qué método HTTP responderá cada una.

Ejemplos:

- `POST /api/auth/login`
- `GET /api/categorias`
- `DELETE /api/examenes/{id}`

### `@RequestBody`

Toma el JSON enviado por el cliente y lo transforma automáticamente en un objeto Java (normalmente un DTO).

### `@RequestHeader`

Permite extraer valores de las cabeceras HTTP de la petición.

Por ejemplo, se utiliza para obtener el token JWT desde la cabecera `Authorization`.

### `@PathVariable`

Extrae parámetros dinámicos directamente desde la URL.

Ejemplos:

```java
@GetMapping("/categorias/{categoriaSlug}")
public ResponseEntity<?> obtener(
    @PathVariable String categoriaSlug
)
```

```java
@DeleteMapping("/examenes/{id}")
public ResponseEntity<?> eliminar(
    @PathVariable Integer id
)
```

---

## 2. Base de Datos (JPA e Hibernate)

### `@Entity` y `@Table`

Indican que una clase Java representa una tabla de la base de datos.

Ejemplos:

- `Usuario`
- `Categoria`
- `Examen`
- `Pregunta`

### `@Id` y `@GeneratedValue`

Definen la clave primaria de la entidad y permiten que su valor se genere automáticamente.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;
```

### `@Column`

Permite configurar propiedades específicas de una columna.

Por ejemplo:

- Nombre real de la columna.
- Si acepta valores nulos.
- Restricciones de unicidad.

### `@OneToMany`, `@ManyToOne` y `@JoinColumn`

Permiten modelar relaciones entre entidades.

Ejemplo:

- Una categoría contiene muchos exámenes.
- Un examen contiene muchas preguntas.

### `@ElementCollection` y `@CollectionTable`

Se utilizan para almacenar listas de valores simples en una tabla relacionada.

Ejemplo:

```java
private List<String> apodos;
```

Cada apodo se almacena en una tabla secundaria vinculada a la categoría.

### `@Query` y `@Param`

Permiten escribir consultas personalizadas cuando los métodos automáticos de Spring Data no son suficientes.

Ejemplo:

```java
@Query("""
SELECT c
FROM Categoria c
JOIN c.apodos a
WHERE LOWER(a) = LOWER(:apodo)
""")
List<Categoria> buscarPorApodo(
    @Param("apodo") String apodo
);
```

---

## 3. Lógica de Negocio y Arquitectura

### `@Service`

Marca una clase como un servicio de negocio.

En el proyecto se utiliza para los gestores:

- `GestorAuth`
- `GestorCategoria`
- `GestorExamen`
- `GestorSeguridad`

### `@Repository`

Marca una clase o interfaz como componente de acceso a datos.

Spring la detecta automáticamente y genera la implementación necesaria.

### `@Transactional`

Garantiza que un conjunto de operaciones se ejecute como una única transacción.

Si ocurre un error durante el proceso:

- Se cancelan todos los cambios realizados.
- La base de datos vuelve a su estado anterior.

Esto evita inconsistencias y datos parcialmente guardados.

### `@Value`

Permite inyectar valores provenientes de propiedades o variables de entorno.

Ejemplo:

```java
@Value("${jwt.secret}")
private String jwtSecret;
```

### `@SpringBootApplication`

Es la anotación principal de una aplicación Spring Boot.

Se coloca sobre la clase que inicia el proyecto y habilita:

- Configuración automática.
- Escaneo de componentes.
- Arranque de Spring Boot.

---

## 4. Seguridad

### `@Configuration`

Indica que una clase contiene configuraciones que Spring debe ejecutar al iniciar la aplicación.

### `@EnableWebSecurity`

Activa Spring Security y permite personalizar las reglas de autenticación y autorización.

### `@Bean`

Indica que un objeto debe ser administrado por Spring.

Ejemplo:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Spring crea una única instancia y la reutiliza donde sea necesaria.

---

## 5. Lombok (Reducción de Código Repetitivo)

### `@Getter`

Genera automáticamente todos los métodos getter de la clase.

### `@Setter`

Genera automáticamente todos los métodos setter de la clase.

### `@NoArgsConstructor`

Genera un constructor vacío.

Ejemplo:

```java
public Usuario() {
}
```

### Beneficios de Lombok

Reduce significativamente la cantidad de código repetitivo.

Sin Lombok:

- Hay que escribir getters.
- Hay que escribir setters.
- Hay que escribir constructores.

Con Lombok, todo ese código se genera automáticamente durante la compilación.
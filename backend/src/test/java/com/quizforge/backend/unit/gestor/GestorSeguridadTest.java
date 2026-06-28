package com.quizforge.backend.unit.gestor;
import com.quizforge.backend.gestor.GestorSeguridad;
import com.quizforge.backend.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GestorSeguridadTest {

    private GestorSeguridad gestorSeguridad;

    @BeforeEach
    void setUp() {
        // Arrange general: JJWT requiere una clave de al menos 256 bits (32 caracteres) para el algoritmo HMAC-SHA
        String secretoPrueba = "EstaClaveDePruebaEsSuperSecretaYTieneMasDe32Caracteres";
        long expiracionPruebaMs = 3600000; // 1 hora

        // Instanciamos el gestor manualmente ya que usa @Value en su constructor
        gestorSeguridad = new GestorSeguridad(secretoPrueba, expiracionPruebaMs);
    }

    @Test
    void generarToken_usuarioValido_retornaTokenJwtFormateado() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(15);
        usuario.setRol("USER");

        // Act
        String token = gestorSeguridad.generarToken(usuario);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // Un JWT válido tiene 3 partes separadas por puntos
    }

    @Test
    void extraerUsuarioId_tokenValidoConPrefijoBearer_retornaId() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(42);
        usuario.setRol("ADMIN");
        String tokenOriginal = gestorSeguridad.generarToken(usuario);
        String tokenConBearer = "Bearer " + tokenOriginal;

        // Act
        int usuarioId = gestorSeguridad.extraerUsuarioId(tokenConBearer);

        // Assert
        assertThat(usuarioId).isEqualTo(42);
    }

    @Test
    void extraerRol_tokenValidoSinPrefijo_retornaRol() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setRol("ADMIN");
        String token = gestorSeguridad.generarToken(usuario);

        // Act
        String rol = gestorSeguridad.extraerRol(token);

        // Assert
        assertThat(rol).isEqualTo("ADMIN");
    }

    @Test
    void extraerUsuarioId_tokenInvalidoOAdulterado_lanzaExcepcion() {
        // Arrange
        String tokenAdulterado = "Bearer eyJhbGciOiJIUzI1NiJ9.TokenFalso.FirmaInvalida";

        // Act & Assert
        assertThatThrownBy(() -> gestorSeguridad.extraerUsuarioId(tokenAdulterado))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token inválido");
    }

    @Test
    void validarRolAdmin_rolAdmin_noLanzaExcepcion() {
        // Arrange
        String rol = "ADMIN";

        // Act & Assert (Si no lanza nada, el test pasa)
        gestorSeguridad.validarRolAdmin(rol);
    }

    @Test
    void validarRolAdmin_rolUser_lanzaExcepcion() {
        // Arrange
        String rol = "USER";

        // Act & Assert
        assertThatThrownBy(() -> gestorSeguridad.validarRolAdmin(rol))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Se requiere rol ADMIN");
    }

    @Test
    void esPropietarioOAdmin_usuarioEsAdmin_retornaTrue() {
        // Arrange
        int usuarioIdIntruso = 99;
        int creadorId = 1;
        String rol = "ADMIN";

        // Act
        boolean resultado = gestorSeguridad.esPropietarioOAdmin(usuarioIdIntruso, creadorId, rol);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void esPropietarioOAdmin_usuarioEsPropietario_retornaTrue() {
        // Arrange
        int usuarioId = 5;
        int creadorId = 5;
        String rol = "USER";

        // Act
        boolean resultado = gestorSeguridad.esPropietarioOAdmin(usuarioId, creadorId, rol);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    void esPropietarioOAdmin_noEsPropietarioNiAdmin_retornaFalse() {
        // Arrange
        int usuarioIdIntruso = 99;
        int creadorId = 1;
        String rol = "USER";

        // Act
        boolean resultado = gestorSeguridad.esPropietarioOAdmin(usuarioIdIntruso, creadorId, rol);

        // Assert
        assertThat(resultado).isFalse();
    }
}
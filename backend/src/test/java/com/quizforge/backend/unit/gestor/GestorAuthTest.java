package com.quizforge.backend.unit.gestor;
import com.quizforge.backend.dto.AuthRequestDTO;
import com.quizforge.backend.dto.AuthResponseDTO;
import com.quizforge.backend.gestor.GestorAuth;
import com.quizforge.backend.gestor.GestorSeguridad;
import com.quizforge.backend.model.Usuario;
import com.quizforge.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorAuthTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GestorSeguridad gestorSeguridad;

    @InjectMocks
    private GestorAuth gestorAuth;

    @Test
    void registrarUsuario_datosValidos_guardaYRetornaUsuario() {
        // Arrange
        AuthRequestDTO requestDTO = new AuthRequestDTO("test@quizforge.com", "Password123");
        String hashSimulado = "hash_falso_123";

        when(usuarioRepository.findByEmail("test@quizforge.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(requestDTO.password())).thenReturn(hashSimulado);

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1);
            return u;
        });

        // Act
        Usuario resultado = gestorAuth.registrarUsuario(requestDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("test@quizforge.com");
        assertThat(resultado.getPasswordHash()).isEqualTo(hashSimulado);
        assertThat(resultado.getRol()).isEqualTo("USER");

        verify(usuarioRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void registrarUsuario_emailYaRegistrado_lanzaExcepcion() {
        // Arrange
        AuthRequestDTO requestDTO = new AuthRequestDTO("test@quizforge.com", "Password123");

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setEmail("test@quizforge.com");

        when(usuarioRepository.findByEmail("test@quizforge.com")).thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        assertThatThrownBy(() -> gestorAuth.registrarUsuario(requestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El email ya está registrado");

        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registrarUsuario_passwordCorta_lanzaExcepcion() {
        // Arrange
        AuthRequestDTO requestDTO = new AuthRequestDTO("test@quizforge.com", "1234"); // Solo 4 caracteres

        // Act & Assert
        assertThatThrownBy(() -> gestorAuth.registrarUsuario(requestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("La contraseña debe tener al menos 8 caracteres");

        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void autenticar_credencialesValidas_retornaAuthResponseDTO() {
        // Arrange
        AuthRequestDTO requestDTO = new AuthRequestDTO("admin@quizforge.com", "AdminPass123");

        Usuario usuarioEnBD = new Usuario();
        usuarioEnBD.setId(10);
        usuarioEnBD.setEmail("admin@quizforge.com");
        usuarioEnBD.setPasswordHash("hash_real_de_bd");
        usuarioEnBD.setRol("ADMIN");

        String tokenGenerado = "jwt_token_simulado.12345.signature";

        when(usuarioRepository.findByEmail("admin@quizforge.com")).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches(requestDTO.password(), usuarioEnBD.getPasswordHash())).thenReturn(true);
        when(gestorSeguridad.generarToken(usuarioEnBD)).thenReturn(tokenGenerado);

        // Act
        AuthResponseDTO resultado = gestorAuth.autenticar(requestDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.token()).isEqualTo(tokenGenerado);
        assertThat(resultado.rol()).isEqualTo("ADMIN");

        verify(usuarioRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(gestorSeguridad, times(1)).generarToken(any(Usuario.class));
    }

    @Test
    void autenticar_emailInexistente_lanzaExcepcion() {
        // Arrange
        AuthRequestDTO requestDTO = new AuthRequestDTO("fantasma@quizforge.com", "Pass12345");

        when(usuarioRepository.findByEmail("fantasma@quizforge.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> gestorAuth.autenticar(requestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales inválidas"); // Se valida que devuelva el mensaje genérico por seguridad

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(gestorSeguridad, never()).generarToken(any(Usuario.class));
    }

    @Test
    void autenticar_passwordIncorrecta_lanzaExcepcion() {
        // Arrange
        AuthRequestDTO requestDTO = new AuthRequestDTO("user@quizforge.com", "PasswordErronea");

        Usuario usuarioEnBD = new Usuario();
        usuarioEnBD.setEmail("user@quizforge.com");
        usuarioEnBD.setPasswordHash("hash_real_de_bd");

        when(usuarioRepository.findByEmail("user@quizforge.com")).thenReturn(Optional.of(usuarioEnBD));
        when(passwordEncoder.matches(requestDTO.password(), usuarioEnBD.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> gestorAuth.autenticar(requestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales inválidas");

        verify(gestorSeguridad, never()).generarToken(any(Usuario.class));
    }
}
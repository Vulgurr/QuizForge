package com.quizforge.backend.gestor;

import com.quizforge.backend.dto.AuthRequestDTO;
import com.quizforge.backend.dto.AuthResponseDTO;
import com.quizforge.backend.model.Usuario;
import com.quizforge.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class GestorAuth {

    private static final String ROL_USUARIO = "USER";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GestorSeguridad gestorSeguridad;

    public GestorAuth(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            GestorSeguridad gestorSeguridad
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.gestorSeguridad = gestorSeguridad;
    }

    @Transactional
    public Usuario registrarUsuario(AuthRequestDTO dto) {
        validarAuthRequest(dto);

        String emailNormalizado = dto.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(passwordEncoder.encode(dto.password()));
        usuario.setRol(ROL_USUARIO);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO autenticar(AuthRequestDTO dto) {
        validarAuthRequest(dto);

        String emailNormalizado = dto.email().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales inválidas"
                ));

        if (!passwordEncoder.matches(dto.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = gestorSeguridad.generarToken(usuario);
        return new AuthResponseDTO(token, usuario.getRol());
    }

    private void validarAuthRequest(AuthRequestDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es obligatorio");
        }
        if (dto.email() == null || dto.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es obligatorio");
        }
        if (!EMAIL_PATTERN.matcher(dto.email().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email no es válido");
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        if (dto.password().length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres"
            );
        }
    }
}

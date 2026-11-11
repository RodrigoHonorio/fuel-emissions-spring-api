package com.example.emissionsCalculator.controller.auth;

import com.example.emissionsCalculator.dto.auth.AuthRequest;
import com.example.emissionsCalculator.dto.auth.AuthResponse;
import com.example.emissionsCalculator.dto.auth.RegisterRequest;
import com.example.emissionsCalculator.entity.user.User;
import com.example.emissionsCalculator.repository.user.UserRepository;
import com.example.emissionsCalculator.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Nome de utilizador já existe.");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER")
                .build();

        userRepository.save(user);
        return ResponseEntity.ok("Utilizador registado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // Procura o utilizador sem lançar exceção direta que rebente o servidor
        User user = userRepository.findByUsername(request.username()).orElse(null);

        // Se o utilizador não existe ou a palavra-passe está errada, retorna 401 Unauthorized limpo
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Utilizador ou palavra-passe inválidos.");
        }

        // Se estiver tudo correto, gera o token e retorna com sucesso
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }
}
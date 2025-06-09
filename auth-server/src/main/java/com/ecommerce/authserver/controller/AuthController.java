
package com.ecommerce.authserver.controller;

import com.ecommerce.authserver.dto.AuthResponse;
import com.ecommerce.authserver.dto.LoginRequest;
import com.ecommerce.authserver.dto.UserRegistrationDTO;
import com.ecommerce.authserver.model.User;
import com.ecommerce.authserver.service.JwtService;
import com.ecommerce.authserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        try {
            userService.registerUser(registrationDTO);
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            User user = userService.findByUsername(loginRequest.getUsername());
            String token = jwtService.generateToken(user);
            
            AuthResponse response = new AuthResponse();
            response.setAccessToken(token);
            response.setExpiresIn(jwtService.getExpirationTime());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token, @RequestParam String username) {
        boolean isValid = jwtService.validateToken(token, username);
        return ResponseEntity.ok(isValid);
    }
}

package edu.sjsu.cs157a.sjsu_event_manager.controller;

import edu.sjsu.cs157a.sjsu_event_manager.dto.JwtResponse;
import edu.sjsu.cs157a.sjsu_event_manager.dto.LoginRequest;
import edu.sjsu.cs157a.sjsu_event_manager.dto.MessageResponse;
import edu.sjsu.cs157a.sjsu_event_manager.dto.SignupRequest;
import edu.sjsu.cs157a.sjsu_event_manager.dto.UserInfoResponse;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import edu.sjsu.cs157a.sjsu_event_manager.repository.UserRepository;
import edu.sjsu.cs157a.sjsu_event_manager.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${sjsu.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        ResponseCookie jwtCookie = ResponseCookie.from("jwt-token", jwt)
            .path("/api")
            .maxAge(jwtExpirationMs / 1000)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .build();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found after authentication."));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok()
               .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
               .body(new UserInfoResponse(
                         user.getUserId(),
                         user.getUsername(),
                         user.getEmail(),
                         roles));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = ResponseCookie.from("jwt-token", "")
                                            .path("/api")
                                            .maxAge(0)
                                            .httpOnly(true)
                                            .secure(true)
                                            .sameSite("Lax")
                                            .build();

        return ResponseEntity.ok()
               .header(HttpHeaders.SET_COOKIE, cookie.toString())
               .body(new MessageResponse("Logout successful!"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setRole(User.Role.USER);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
} 
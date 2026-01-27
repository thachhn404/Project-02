package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.security.JwtService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.security.UserPrincipal;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.LoginRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RegisterRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.mapper.UserMapper;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.RoleRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    public AuthenResponse register(RegisterRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid full name");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        String roleCode = (request.getRoleCode() == null || request.getRoleCode().isBlank()) ? "CITIZEN" : request.getRoleCode();
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role does not exist"));
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        u.setFullName(request.getFullName());
        u.setPhone(request.getPhone());
        u.setRole(role);
        u.setStatus("active");
        userRepository.save(u);
        String token = jwtService.generateToken(new UserPrincipal(u));
        return new AuthenResponse(token, "Bearer", jwtService.getExpirationMs(), userMapper.toDto(u));
    }

    public AuthenResponse login(LoginRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthenResponse(token, "Bearer", jwtService.getExpirationMs(), userMapper.toDto(user));
    }

    public void logout() { }
}

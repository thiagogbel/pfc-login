package com.example.demo.controller;

import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String salvarUsuario(Usuario usuario) {
        if (repository.findByUsername(usuario.getUsername()) != null) {
            return "redirect:/cadastro?existe=true";
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        if(usuario.getRole() == null || usuario.getRole().isEmpty()) {
            usuario.setRole("ROLE_USER"); 
        }
        repository.save(usuario);
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
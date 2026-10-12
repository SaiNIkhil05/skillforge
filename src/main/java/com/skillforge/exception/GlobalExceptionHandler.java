package com.skillforge.exception;

import com.skillforge.repository.CareerRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final CareerRoleRepository careerRoleRepository;

    public GlobalExceptionHandler(CareerRoleRepository careerRoleRepository) {
        this.careerRoleRepository = careerRoleRepository;
    }

    @ExceptionHandler(AuthenticationException.class)
    public Object handleAuthenticationException(AuthenticationException ex, HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Unauthorized");
            err.put("message", "Invalid email or password. Please verify your credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
        model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        return "auth/login";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(InvalidDomainException.class)
    public Object handleInvalidDomain(InvalidDomainException ex, HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Invalid Domain");
            err.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("roles", careerRoleRepository.findAll());
        return "auth/register";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request, Model model) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String firstMessage = errors.values().stream().findFirst().orElse("Validation failed");

        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Validation Failed");
            body.put("message", firstMessage);
            body.put("fieldErrors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        model.addAttribute("errorMessage", firstMessage);
        model.addAttribute("roles", careerRoleRepository.findAll());
        return "auth/register";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Bad Request");
            err.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("roles", careerRoleRepository.findAll());
        return "auth/register";
    }
}

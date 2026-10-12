package com.skillforge.controller;

import com.skillforge.dto.AuthResponse;
import com.skillforge.dto.LoginRequest;
import com.skillforge.dto.RegisterRequest;
import com.skillforge.model.CareerRole;
import com.skillforge.model.User;
import com.skillforge.model.UserRole;
import com.skillforge.repository.CareerRoleRepository;
import com.skillforge.repository.SkillRepository;
import com.skillforge.security.JwtUtil;
import com.skillforge.service.AuthService;
import com.skillforge.service.ProfileService;
import com.skillforge.dto.*;
import com.skillforge.model.CareerRole;
import com.skillforge.model.RefreshToken;
import com.skillforge.model.User;
import com.skillforge.model.UserRole;
import com.skillforge.repository.CareerRoleRepository;
import com.skillforge.repository.SkillRepository;
import com.skillforge.security.JwtUtil;
import com.skillforge.service.AuthService;
import com.skillforge.service.ProfileService;
import com.skillforge.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    private final AuthService authService;
    private final CareerRoleRepository careerRoleRepository;
    private final SkillRepository skillRepository;
    private final ProfileService profileService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.cookie-name:SF_AUTH_TOKEN}")
    private String cookieName;

    @Value("${jwt.refresh-cookie-name:SF_REFRESH_TOKEN}")
    private String refreshCookieName;

    public AuthController(AuthService authService,
                          CareerRoleRepository careerRoleRepository,
                          SkillRepository skillRepository,
                          ProfileService profileService,
                          RefreshTokenService refreshTokenService,
                          JwtUtil jwtUtil) {
        this.authService = authService;
        this.careerRoleRepository = careerRoleRepository;
        this.skillRepository = skillRepository;
        this.profileService = profileService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
    }

    // ==========================================
    // THYMELEAF PAGES
    // ==========================================

    @GetMapping("/")
    public String landingPage(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        List<CareerRole> roles = careerRoleRepository.findAll();
        model.addAttribute("roles", roles);
        model.addAttribute("totalRoles", roles.size());
        model.addAttribute("totalSkills", skillRepository.count());
        return "landing";
    }

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "verified", required = false) String verified,
                            Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "You have been logged out successfully.");
        }
        if (verified != null) {
            model.addAttribute("successMessage", "Email verified! You can now sign in.");
        }
        return "auth/login";
    }

    private void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from(cookieName, accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtUtil.getExpirationMs() / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        if (refreshToken != null) {
            ResponseCookie refreshCookie = ResponseCookie.from(refreshCookieName, refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie clearAccess = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());

        ResponseCookie clearRefresh = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
    }

    private String resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                              BindingResult bindingResult,
                              HttpServletResponse response,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            AuthResponse authResponse = authService.login(request);
            User user = profileService.getUserByEmail(authResponse.getEmail());
            RefreshToken rt = refreshTokenService.createRefreshToken(user);

            addAuthCookies(response, authResponse.getToken(), rt.getToken());

            if (UserRole.ADMIN.name().equals(authResponse.getRole())) {
                return "redirect:/admin/roles";
            } else {
                return "redirect:/dashboard";
            }
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Authentication failed: " + ex.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        model.addAttribute("roles", careerRoleRepository.findAll());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", careerRoleRepository.findAll());
            return "auth/register";
        }

        try {
            User user = authService.register(request);
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            RefreshToken rt = refreshTokenService.createRefreshToken(user);

            addAuthCookies(response, token, rt.getToken());

            return "redirect:/dashboard";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("roles", careerRoleRepository.findAll());
            return "auth/register";
        }
    }

    @GetMapping("/verify")
    public String handleVerify(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        try {
            authService.verifyEmail(token);
            redirectAttributes.addFlashAttribute("successMessage", "Email successfully verified. Please sign in.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Verification failed: " + ex.getMessage());
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String handleLogout(HttpServletRequest request, HttpServletResponse response) {
        String rt = resolveCookie(request, refreshCookieName);
        if (rt != null) {
            refreshTokenService.revokeToken(rt);
        }
        clearAuthCookies(response);
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout=true";
    }

    // ==========================================
    // REST API ENDPOINTS (For React Frontend)
    // ==========================================

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@Valid @RequestBody RegisterRequest request,
                                         HttpServletResponse response) {
        User user = authService.register(request);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        RefreshToken rt = refreshTokenService.createRefreshToken(user);

        addAuthCookies(response, token, rt.getToken());

        AuthResponse authResp = new AuthResponse(
                token,
                rt.getToken(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                true,
                "Registration successful"
        );
        return ResponseEntity.ok(authResp);
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> apiLogin(@Valid @RequestBody LoginRequest request,
                                                 HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        User user = profileService.getUserByEmail(authResponse.getEmail());
        RefreshToken rt = refreshTokenService.createRefreshToken(user);

        authResponse.setRefreshToken(rt.getToken());
        addAuthCookies(response, authResponse.getToken(), rt.getToken());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/api/auth/refresh")
    @ResponseBody
    public ResponseEntity<?> apiRefreshToken(@RequestBody(required = false) TokenRefreshRequest request,
                                             HttpServletRequest httpRequest,
                                             HttpServletResponse httpResponse) {
        String tokenStr = null;
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            tokenStr = request.getRefreshToken().trim();
        } else {
            tokenStr = resolveCookie(httpRequest, refreshCookieName);
            if (tokenStr == null || tokenStr.isBlank()) {
                tokenStr = httpRequest.getHeader("X-Refresh-Token");
            }
        }

        if (tokenStr == null || tokenStr.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "BadRequest", "message", "Refresh token is missing from request body, cookies, or headers."));
        }

        try {
            RefreshToken rotated = refreshTokenService.rotateRefreshToken(tokenStr);
            User user = rotated.getUser();
            String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            addAuthCookies(httpResponse, newAccessToken, rotated.getToken());

            TokenRefreshResponse refreshResp = new TokenRefreshResponse(
                    newAccessToken,
                    rotated.getToken(),
                    jwtUtil.getExpirationMs()
            );
            return ResponseEntity.ok(refreshResp);
        } catch (Exception ex) {
            clearAuthCookies(httpResponse);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", ex.getMessage()));
        }
    }

    @GetMapping("/api/auth/verify")
    @ResponseBody
    public ResponseEntity<?> apiVerify(@RequestParam("token") String token) {
        boolean verified = authService.verifyEmail(token);
        Map<String, Object> resp = new HashMap<>();
        resp.put("verified", verified);
        resp.put("message", "Email verified successfully.");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<?> apiCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User user = profileService.getUserByEmail(auth.getName());
        Map<String, Object> resp = new HashMap<>();
        resp.put("userId", user.getUserId());
        resp.put("fullName", user.getFullName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole().name());
        resp.put("education", user.getEducation());
        resp.put("department", user.getDepartment());
        resp.put("emailVerified", user.getEmailVerified());
        if (user.getTargetRole() != null) {
            resp.put("targetRoleId", user.getTargetRole().getRoleId());
            resp.put("targetRoleName", user.getTargetRole().getRoleName());
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> apiLogout(HttpServletRequest request, HttpServletResponse response) {
        String rt = resolveCookie(request, refreshCookieName);
        if (rt != null) {
            refreshTokenService.revokeToken(rt);
        }
        clearAuthCookies(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}

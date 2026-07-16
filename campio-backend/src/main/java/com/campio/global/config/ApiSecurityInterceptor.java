package com.campio.global.config;

import com.campio.domain.user.UserService;
import com.campio.global.exception.ForbiddenException;
import com.campio.global.exception.UnauthorizedException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ApiSecurityInterceptor implements HandlerInterceptor {

  private static final Set<String> PUBLIC_MUTATIONS = Set.of("/api/auth/login", "/api/auth/signup");
  private static final Set<String> MUTATION_METHODS = Set.of("POST", "PATCH", "PUT", "DELETE");

  private final UserService userService;

  @Value("${campio.frontend-origin:http://localhost:5173}")
  private String frontendOrigins;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

    if (!request.getRequestURI().startsWith("/api/") || !MUTATION_METHODS.contains(request.getMethod())) {
      return true;
    }
    validateOrigin(request);
    if (!PUBLIC_MUTATIONS.contains(request.getRequestURI())
        && userService.optionalCurrentUserId(request.getSession(false)) == null) {
      throw new UnauthorizedException("Login required");
    }
    return true;
  }

  private void validateOrigin(HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin == null || origin.isBlank()) return;
    Set<String> allowed = Arrays.stream(frontendOrigins.split(","))
        .map(String::trim).filter(value -> !value.isBlank()).collect(Collectors.toSet());
    if (!allowed.contains(origin)) throw new ForbiddenException("Origin not allowed");
  }
}

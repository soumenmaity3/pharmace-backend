package pharmase.com.pharmase_monolithi.auth.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Authorization {
    public String token(String authHeader){
        if (authHeader == null || authHeader.isBlank()) {
            return "Missing or invalid token";
        }

        if (!authHeader.startsWith("Bearer ")) {
            return "Invalid token format";
        }
        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            return "Empty token";
        }

        return token;
    }
}
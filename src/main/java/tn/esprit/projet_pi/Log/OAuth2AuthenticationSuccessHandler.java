package tn.esprit.projet_pi.Log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tn.esprit.projet_pi.Log.JwtService;
import tn.esprit.projet_pi.Service.UserService;
import tn.esprit.projet_pi.entity.Role;
import tn.esprit.projet_pi.entity.User;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtService jwtTokenProvider;

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // Debug the entire attributes map
        System.out.println("All attributes: " + oauth2User.getAttributes());

        // Google sometimes nests the profile info
        String email = null;
        if (oauth2User.getAttribute("email") != null) {
            email = oauth2User.getAttribute("email");
        } else {
            // Try to get from nested structure if present
            Map<String, Object> attributes = oauth2User.getAttributes();
            if (attributes.containsKey("email")) {
                email = (String) attributes.get("email");
            }
        }

        System.out.println("Extracted email: " + email);

        // Proceed with email if found
        if (email != null) {
            response.sendRedirect("http://localhost:4200/auth/oauth2-success?email=" + email);
        } else {
            response.sendRedirect("http://localhost:4200/auth/error?message=Email not found in OAuth response");
        }
    }
}

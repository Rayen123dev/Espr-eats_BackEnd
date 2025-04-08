package tn.esprit.projet_pi.Log;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // Si vous avez un service utilisateur, vous pouvez l'injecter ici
    // private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // Extraire les informations de l'utilisateur
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        System.out.println("Authentification Google réussie pour : " + email);

        // Ici vous pourriez créer ou mettre à jour l'utilisateur dans votre base de données
        // userService.processOAuthPostLogin(email, name);

        // Rediriger vers le frontend avec un token JWT si nécessaire
        response.sendRedirect("http://localhost:4200/auth/oauth2-success?email=" + email);
    }
}
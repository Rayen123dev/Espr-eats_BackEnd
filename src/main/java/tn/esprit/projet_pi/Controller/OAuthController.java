package tn.esprit.projet_pi.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.projet_pi.Log.JwtService;
import tn.esprit.projet_pi.Service.UserService;
import tn.esprit.projet_pi.entity.OAuthUser;
import tn.esprit.projet_pi.entity.User;

@RestController
@RequestMapping("/api/auth")
public class OAuthController {

    @Autowired
    @Lazy
    private UserService userService;
    private JwtService jwtTokenProvider;

    @GetMapping("/oauth2-user")
    public ResponseEntity<?> getOAuth2UserInfo(@RequestParam String email) {
        // Find or create user based on email
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Create response with JWT token
        OAuthUser response = new OAuthUser();
        response.setEmail(user.getEmail());
        response.setName(user.getNom());
        response.setRole(user.getRole().toString());

        response.setToken(jwtTokenProvider.generateToken(user));

        return ResponseEntity.ok(response);
    }
}
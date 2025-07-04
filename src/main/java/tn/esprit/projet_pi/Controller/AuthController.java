package tn.esprit.projet_pi.Controller;

import jakarta.persistence.Entity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.projet_pi.Log.JwtService;
import tn.esprit.projet_pi.Log.LoginRequest;
import tn.esprit.projet_pi.Log.RegisterRequest;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.Service.*;
import tn.esprit.projet_pi.entity.ForgotPasswordRequest;
import tn.esprit.projet_pi.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import java.util.HashMap;
import java.util.Map;


import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final EmailService emailService;
    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final CloudinaryService cloudinaryService;
    private final CaptchaService captchaService;
    @Autowired
    private LoginAttemptService loginAttemptService;


    public AuthController(UserService userService, EmailService emailService, UserRepo userRepo, JwtService jwtService, CloudinaryService cloudinaryService, CaptchaService captchaService) {
        this.userService = userService;
        this.emailService = emailService;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.cloudinaryService = cloudinaryService;
        this.captchaService = captchaService;
    }
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Collections.singletonMap("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Image upload failed: " + e.getMessage());
        }
    }

    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signup(@RequestBody RegisterRequest request) {
        List<User> users = userRepo.findAll();
        for (User user : users) {
            if ((user.getEmail()).equals(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already in use.");
            }
        }
        User user = new User();
        user.setNom(request.getNom());
        user.setEmail(request.getEmail());
        user.setMdp(request.getMdp());
        user.setRole(request.getRole());
        user.setAge(request.getAge());
        user.setLink_Image(request.getLink_Image());
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        User registeredUser = userService.register(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return ResponseEntity.ok("Utilisateur inscrit avec succès. Veuillez vérifier votre e-mail.");

    }

    @PostMapping("/login")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
        String email = loginRequest.getEmail();

        if (loginAttemptService.isBlocked(email)) {
            long minutes = loginAttemptService.getRemainingLockTime(email) / 60000;
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Compte bloqué temporairement. Réessayez dans " + minutes + " minute(s).");
        }

        boolean captchaVerified = captchaService.verifyCaptcha(loginRequest.getCaptchaToken());
        if (!captchaVerified) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Échec de la vérification CAPTCHA.");
        }

        String token = userService.login(email, loginRequest.getMdp());

        if (token != null) {
            loginAttemptService.loginSucceeded(email);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } else {
            loginAttemptService.loginFailed(email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides.");
        }
    }


    @DeleteMapping("/user_del/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete user.");
        }
    }
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/usersp")
    public ResponseEntity<Map<String, Object>> getPaginatedUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search
    ) {
        Page<User> paginatedUsers = userService.getPaginatedUsers(page, size, filter, search);

        Map<String, Object> response = new HashMap<>();
        response.put("data", paginatedUsers.getContent());
        response.put("totalItems", paginatedUsers.getTotalElements());
        response.put("totalPages", paginatedUsers.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    @PutMapping("/userUpdate/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        boolean updated = userService.updateUser(id, updatedUser);
        if (updated) {
            return ResponseEntity.ok("User updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("User not found or update failed.");
        }
    }

    @PutMapping("/BlocUser/{id}")
    public ResponseEntity<String> BlocUser(@PathVariable Long id, @RequestBody User updatedUser) {
        boolean updated = userService.blocUser(id);
        if (updated) {
            return ResponseEntity.ok("User updated Bloc successfully.");
        } else {
            return ResponseEntity.badRequest().body("User not found or update failed.");
        }
    }

    @PutMapping("/ActiveUser/{id}")
    public ResponseEntity<String> ActiveUser(@PathVariable Long id, @RequestBody User updatedUser) {
        boolean updated = userService.activUser(id);
        if (updated) {
            return ResponseEntity.ok("User updated Active successfully.");
        } else {
            return ResponseEntity.badRequest().body("User not found or update failed.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail(); // On récupère l'email de l'objet envoyé

        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            emailService.sendResetPasswordEmail(user.getEmail().toString(),jwtService.generateToken(user)); // Envoi de l'email de réinitialisation
            return ResponseEntity.ok(Collections.singletonMap("message", "Un lien de réinitialisation a été envoyé à votre adresse email."));
        }
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Aucun utilisateur trouvé avec cet email."));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        String email = jwtService.extractUsername(token); // Extraction de l'email du token
        Optional<User> userOptional = userRepo.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur introuvable.");
        }
        User user = userOptional.get();
        // Optionnel : vous pouvez vérifier que le token n'est pas expiré, selon votre logique
        user.setMdp(passwordEncoder.encode(newPassword)); // Encodage du nouveau mot de passe
        userRepo.save(user); // Sauvegarde du mot de passe réinitialisé
        return ResponseEntity.ok("Votre mot de passe a été réinitialisé avec succès.");
    }
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail() {
        try {
            emailService.sendResetPasswordEmail("benmassoudrayen7@gmail.com", "test-token");
            return ResponseEntity.ok("Email envoyé avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        // Look for the user by verification token
        User user = userService.findByVerificationToken(token);

        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Token invalide ou expiré !");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if the user is already verified
        if (Boolean.TRUE.equals(user.getVerified())) {  // Safely checking for null
            Map<String, String> response = new HashMap<>();
            response.put("message", "L'email a déjà été vérifié.");
            return ResponseEntity.ok(response);
        }

        // Set the user as verified and remove the verification token
        user.setVerified(true);
        user.setVerificationToken(null);

        // Save the updated user to the database
        userService.saveUser(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Email vérifié avec succès !");

        // Return a success response
        return ResponseEntity.ok(response);
    }




    @GetMapping("/search")
    public List<User> search(@RequestParam("query") String query) {
        // Fetch all users from the service
        List<User> allItems = userService.getAllUsers();

        // Perform search filtering on 'nom' and 'email'
        return allItems.stream()
                .filter(item -> item.getNom().toLowerCase().contains(query.toLowerCase()) ||
                        item.getEmail().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

}

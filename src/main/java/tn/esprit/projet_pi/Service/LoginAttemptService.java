package tn.esprit.projet_pi.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Log.JwtService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class LoginAttemptService {
    private final EmailService emailService;
    private final UserService userService;
    private final JwtService jwtService;

    private final int MAX_ATTEMPTS = 3;
    private final long LOCK_TIME = 15 * 60 * 1000; // 15 minutes

    private Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private Map<String, Long> lockTime = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(EmailService emailService, UserService userService, JwtService jwtService) {
        this.emailService = emailService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public void loginFailed(String email) {
        int attemptsCount = attempts.getOrDefault(email, 0) + 1;
        attempts.put(email, attemptsCount);

        if (attemptsCount >= MAX_ATTEMPTS) {
            lockTime.put(email, System.currentTimeMillis());
            emailService.sendalertpwd(email, jwtService.generateToken(userService.getUserByEmail(email)));
        }
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
        lockTime.remove(email);
    }

    public boolean isBlocked(String email) {
        if (!lockTime.containsKey(email)) return false;
        long timePassed = System.currentTimeMillis() - lockTime.get(email);
        if (timePassed > LOCK_TIME) {
            lockTime.remove(email);
            attempts.remove(email);
            return false;
        }
        return true;
    }

    public long getRemainingLockTime(String email) {
        if (!lockTime.containsKey(email)) return 0;
        long passed = System.currentTimeMillis() - lockTime.get(email);
        return Math.max(0, LOCK_TIME - passed);
    }
}

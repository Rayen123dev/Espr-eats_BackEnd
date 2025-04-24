package tn.esprit.projet_pi.Service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.View;
import tn.esprit.projet_pi.Log.JwtService;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;


import java.time.LocalDateTime;
import java.util.*;

import static java.security.KeyRep.Type.SECRET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class UserService implements UserInterface{
    @Autowired
    UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String login(String email, String password) {
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isPresent()) {
            User u = user.get();
            if (passwordEncoder.matches(password,u.getMdp())) {
                u.setLastLogin(LocalDateTime.now());
                userRepo.save(u);
                return JwtService.generateToken(u);
            }
        }
        return null;

    }

    @Override
    public User register(User user) {
        user.setMdp(passwordEncoder.encode(user.getMdp()));
        return userRepo.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepo.findById(Math.toIntExact(id)).get();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).get();
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepo.findByNom(username).get();
    }

    @Override
    public boolean updateUser(Long id, User updatedUser) {
            return userRepo.findByidUser(id).map(user -> {
                user.setNom(updatedUser.getNom());
                user.setEmail(updatedUser.getEmail());
                user.setAge(updatedUser.getAge());
                userRepo.save(user);
                return true;
            }).orElse(false);
    }

    @Override
    public boolean deleteUser(Long id) {
        User user = userRepo.findByidUser(id).get();
        userRepo.delete(user);
        return true;
    }

    @Override
    public boolean addUser(User user) {
        return false;
    }

    @Override
    public List<User> getUserByName(String username) {
        return List.of();
    }

    @Override
    public List<User> getUserByRole(String role) {
        return List.of();
    }

    @Override
    public boolean blocUser(Long id) {
        return userRepo.findByidUser(id).map(user -> {
            user.setVerified(Boolean.valueOf("NULL"));
            userRepo.save(user);
            return true;
        }).orElse(false);
    }

    @Override
    public boolean activUser(Long id) {
        return userRepo.findByidUser(id).map(user -> {
            // Log the user object to see if it's being retrieved correctly
            System.out.println("Found user: " + user);

            user.setVerified(true);  // Set the verified status to true
            userRepo.save(user);  // Save the updated user

            return true;  // Return true indicating success
        }).orElse(false);  // If the user is not found, return false
    }


    public User findByVerificationToken(String token) {
        return userRepo.findByVerificationToken(token);
    }

    public void saveUser(User user) {
        userRepo.save(user);
    }

    public Page<User> getPaginatedUsers(int page, int size, String filter, String search) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (search != null && !search.isEmpty()) {
            if (filter != null && !filter.isEmpty()) {
                return userRepo.findByRoleAndNomContainingIgnoreCase(filter, search, pageable);
            } else {
                return userRepo.findByNomContainingIgnoreCase(search, pageable);
            }
        } else {
            if (filter != null && !filter.isEmpty()) {
                return userRepo.findByRole(filter, pageable);
            } else {
                return userRepo.findAll(pageable);
            }
        }
    }

}

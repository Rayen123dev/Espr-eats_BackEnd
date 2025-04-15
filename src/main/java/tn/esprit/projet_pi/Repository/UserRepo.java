package tn.esprit.projet_pi.Repository;

import tn.esprit.projet_pi.entity.Role;
import tn.esprit.projet_pi.entity.User;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface UserRepo extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByNom(String nom);
    Optional<User> findByidUser(Long idUser);
    Optional<User> findByEmailIgnoreCase(String email);


    public User findByVerificationToken(String token);

    List<User> findByRole(Role role);


    Page<User> findByNomContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByRoleAndNomContainingIgnoreCase(String role, String name, Pageable pageable);

}

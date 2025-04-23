package com.example.forum.Repository;

import com.example.forum.Entity.Post;
import com.example.forum.Entity.Reaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Integer> {
    @Query("SELECT r FROM Reaction r WHERE r.parent.postID = :postId AND r.user.idUser = :userId")
    Optional<Reaction> findByPostAndUser(
            @Param("postId") Integer postId,
            @Param("userId") Integer userId
    );

    List<Reaction> findByParentPostID(Integer postId);
}

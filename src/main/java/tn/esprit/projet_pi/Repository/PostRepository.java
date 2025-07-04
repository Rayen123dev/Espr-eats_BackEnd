//package com.example.forum.Repository;
package tn.esprit.projet_pi.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import tn.esprit.projet_pi.entity.Post;

@Repository
public interface PostRepository  extends JpaRepository<Post, Integer> {
    // Retrieve only parent posts (posts without a parent)
    List<Post> findByParentIsNull();

    // Retrieve replies for a specific post
    List<Post> findByParentPostID(Integer parentId);

    @EntityGraph(attributePaths = {"replies", "reactions"})
    @Query("SELECT p FROM Post p WHERE p.notified = false")
    List<Post> findAllWithRepliesAndReactions();

    List<Post> findByContentContainingIgnoreCase(String keyword);
    Page<Post> findAll(Pageable pageable);

    //Page<Post> findByAuthor_IdUser(Long author_idUser, Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.author.idUser = :userId")
    Page<Post> findByAuthorIdUser(@Param("userId") Integer userId, Pageable pageable);
}

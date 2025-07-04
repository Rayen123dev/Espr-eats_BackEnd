//package com.example.forum.Service;
package tn.esprit.projet_pi.Service;

import org.springframework.data.domain.Page;
import tn.esprit.projet_pi.Controller.PostDTO;
import tn.esprit.projet_pi.entity.Post;

import java.util.List;
import java.util.Optional;

public interface IPostService {
    public List<Post> retrieveAllPosts();
    public Optional<Post> retrievePost(Integer id);
    public Post addPost(Post post);
    public void deletePost(Integer post);
    public Post updatePost(Post post);

    public List<Post> retrieveParentPosts();
    public List<Post> retrieveReplies(Integer postId);
    public Optional<Post> findById (Integer postId);
    public List<Post> searchPosts(String keyword);

    Page<PostDTO> getAllPosts(int page, int size);

    void deletePost1(Integer postID, Integer userID);
}

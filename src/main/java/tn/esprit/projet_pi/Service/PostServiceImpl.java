//package com.example.forum.Service;
package tn.esprit.projet_pi.Service;

//import com.example.forum.Entity.Post;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Controller.PostDTO;
import tn.esprit.projet_pi.Repository.PostRepository;
import tn.esprit.projet_pi.entity.Post;


import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PostServiceImpl implements IPostService{

    @Autowired
    PostRepository postRepository;

    @Override
    public List<Post> retrieveAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Optional<Post> retrievePost(Integer id) {
        return postRepository.findById(id);
    }

    @Override
    public Post addPost(Post post) {
        return postRepository.save(post);
    }

    @Override
    public void deletePost(Integer postId) {
        postRepository.deleteById(postId);
    }
    public void deletePost1(Integer postId, Integer userID) {
        postRepository.deleteById(postId);
    }

    @Override
    public Post updatePost(Post post) {
        return postRepository.save(post);
    }

    @Override
    public List<Post> retrieveParentPosts() {
        return postRepository.findByParentIsNull();
    }

    @Override
    public List<Post> retrieveReplies(Integer postId) {
        return postRepository.findByParentPostID(postId);
    }

    @Override
    public Optional<Post> findById(Integer postId) {
        return postRepository.findById(postId);
    }

    @Override
    public List<Post> searchPosts(String keyword) {
        return postRepository.findByContentContainingIgnoreCase(keyword);
    }

    public Page<PostDTO> getAllPosts(int page, int size) {
        Page<Post> posts = postRepository.findAll(PageRequest.of(page, size));
        return posts.map(PostDTO::new);
    }


    public Page<PostDTO> getPostsByUser(Integer userId, int page, int size) {
        Page<Post> posts = postRepository.findByAuthorIdUser(userId, PageRequest.of(page, size));
        return posts.map(PostDTO::new);
    }

}

//package com.example.forum.Controller;
package tn.esprit.projet_pi.Controller;

import tn.esprit.projet_pi.entity.Post;
import java.util.List;

public class PostWithRepliesDTO {

    private Post post;
    private List<Post> replies;

    public PostWithRepliesDTO(Post post, List<Post> replies) {
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public List<Post> getReplies() {
        return replies;
    }

    public void setReplies(List<Post> replies) {
        this.replies = replies;
    }
}

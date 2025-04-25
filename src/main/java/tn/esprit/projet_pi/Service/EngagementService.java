//package com.example.forum.Service;
package tn.esprit.projet_pi.Service;


import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.PostRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.Post;
import tn.esprit.projet_pi.entity.User;

import java.util.List;

@Service
public class EngagementService {

    private static final Logger log = LoggerFactory.getLogger(EngagementService.class);
    private final PostRepository postRepository;
    private final EmailForumService emailService;
    private final UserRepo userRepository;

    @Value("${engagement.threshold}")
    private int engagementThreshold;

    // Explicit constructor for dependency injection
    @Autowired
    public EngagementService(PostRepository postRepository,
                             EmailForumService emailService,
                             UserRepo userRepository) {
        this.postRepository = postRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkPostEngagement() {
        List<Post> posts = postRepository.findAllWithRepliesAndReactions();

        for (Post post : posts) {
            int totalEngagement = (post.getReplies() != null ? post.getReplies().size() : 0)
                    + (post.getReactions() != null ? post.getReactions().size() : 0);

            if (totalEngagement >= engagementThreshold && !post.isNotified()) {
                User author = userRepository.findById(Math.toIntExact(post.getAuthor().getId_user()))
                        .orElseThrow(() -> new RuntimeException("User not found"));

                sendEngagementEmail(author, post, totalEngagement);
                post.setNotified(true);
                postRepository.save(post);
            }
        }
    }

    private void sendEngagementEmail(User user, Post post, int engagementScore) {
        try {
            log.info("Attempting to send email to: {}", user.getEmail());

            String subject = "Your post is trending!";
            String content = String.format(
                    "Hi %s,\n\n" +
                            "Your post '%s' has reached %d engagements!\n\n" +
                            "Statistics:\n" +
                            "- Replies: %d\n" +
                            "- Reactions: %d\n\n" +
                            "Keep up the great content!",
                    "arijmoulahi@gmail.com",
                    post.getContent().length() > 50
                            ? post.getContent().substring(0, 50) + "..."
                            : post.getContent(),
                    engagementScore,
                    post.getReplies() != null ? post.getReplies().size() : 0,
                    post.getReactions() != null ? post.getReactions().size() : 0
            );

            emailService.sendSimpleMessage(user.getEmail(), subject, content);
            log.info("Email sent successfully to {}","arij.moulehi@esprit.tn");
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
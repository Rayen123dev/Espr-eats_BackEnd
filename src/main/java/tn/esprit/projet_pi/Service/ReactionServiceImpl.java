//package com.example.forum.Service;
package tn.esprit.projet_pi.Service;

//import com.example.forum.Entity.Reaction;

//import com.example.forum.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.projet_pi.Repository.PostRepository;
import tn.esprit.projet_pi.Repository.ReactionRepository;
import tn.esprit.projet_pi.Repository.UserRepo;
import tn.esprit.projet_pi.entity.EmojiType;
import tn.esprit.projet_pi.entity.Reaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReactionServiceImpl implements IReactionService{

    @Autowired
    ReactionRepository reactionRepository;

    @Autowired
    UserRepo userRepository;

    @Autowired
    PostRepository postRepository;

    @Override
    public List<Reaction> retrieveAllReactions() {
        return reactionRepository.findAll();
    }

    @Override
    public Optional<Reaction> retrieveReaction(Integer id) {
        return reactionRepository.findById(id);
    }

    @Override
    public Reaction addReaction(Reaction reaction) {
        return reactionRepository.save(reaction);
    }

    @Override
    public void deleteReaction(Integer reaction) {
        reactionRepository.deleteById(reaction);
    }

    @Override
    public Reaction updateReaction(Reaction reaction) {
        return reactionRepository.save(reaction);
    }

    @Override
    @Transactional
    public Reaction toggleReaction(Integer postId, Integer userId, EmojiType emoji) {
        // Find existing reaction
        Optional<Reaction> existing = reactionRepository.findByPostAndUser(postId, userId);

        if (existing.isPresent()) {
            Reaction reaction = existing.get();
            if (reaction.getEmoji() == emoji) {
                // Remove if same emoji
                reactionRepository.delete(reaction);
                return null;
            } else {
                // Update if different emoji
                reaction.setEmoji(emoji);
                return reactionRepository.save(reaction);
            }
        } else {
            // Add new reaction
            Reaction newReaction = new Reaction();
            newReaction.setEmoji(emoji);
            newReaction.setUser(userRepository.findById(userId).orElseThrow());
            newReaction.setParent(postRepository.findById(postId).orElseThrow());
            newReaction.setCreatedAt(LocalDateTime.now());
            return reactionRepository.save(newReaction);
        }
    }

    @Override
    public List<Reaction> getReactionsByPost(Integer postId) {
        return reactionRepository.findByParentPostID(postId);
    }

}

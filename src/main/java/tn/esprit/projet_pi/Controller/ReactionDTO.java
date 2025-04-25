//package com.example.forum.Controller;
package tn.esprit.projet_pi.Controller;


import tn.esprit.projet_pi.entity.EmojiType;
import tn.esprit.projet_pi.entity.Reaction;

import java.time.LocalDateTime;

public class ReactionDTO {
    private Integer reactionId;
    private EmojiType emoji;
    private Integer userId;
    private String username;
    private LocalDateTime createdAt;

    public ReactionDTO(Reaction reaction) {
        this.reactionId = reaction.getReactionID();
        this.emoji = reaction.getEmoji();
        this.userId = Math.toIntExact(reaction.getUser().getId_user());
        this.username = reaction.getUser().getNom();
        this.createdAt = reaction.getCreatedAt();
    }
}

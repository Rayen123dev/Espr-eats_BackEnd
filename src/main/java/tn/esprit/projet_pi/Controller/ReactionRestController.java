//package com.example.forum.Controller;
package tn.esprit.projet_pi.Controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Service.IReactionService;
import tn.esprit.projet_pi.entity.EmojiType;
import tn.esprit.projet_pi.entity.Post;
import tn.esprit.projet_pi.entity.Reaction;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/reaction")
//@CrossOrigin(origins = "http://localhost:4200/posts")  // Allow requests from your frontend
public class ReactionRestController {

    @Autowired
    IReactionService reactionService;

    @GetMapping("/get-all-reactions")
    public List<Reaction> listAllReactions(){
        return reactionService.retrieveAllReactions();
    }

    @GetMapping("/display-reaction/{reaction-id}")
    public Optional<Reaction> displayReaction(@PathVariable("reaction-id") Integer id){
        return reactionService.retrieveReaction(id);
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/add-reaction")
    public ResponseEntity<?> addReaction(@RequestBody Reaction reaction) {
        try {
            reactionService.addReaction(reaction);
            //return ResponseEntity.ok("Reaction added successfully");
            return ResponseEntity.ok(Collections.singletonMap("message", "Reaction added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding reaction");
        }
    }


    @DeleteMapping("/delete-reaction/{reaction-id}")
    public  void deleteReaction(@PathVariable ("reaction-id")Integer id){
        reactionService.deleteReaction(id);
    }

    @PutMapping("/update/{reactionID}")
    public Post updateReaction(@RequestBody Reaction reaction){
        return reactionService.updateReaction(reaction).getParent();
    }

    ///mods here
// Toggle reaction endpoint
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleReaction(
            @RequestParam Integer postId,
            @RequestParam Integer userId,
            @RequestParam String emojiType) {

        try {
            EmojiType emoji = EmojiType.valueOf(emojiType.toUpperCase());
            Reaction reaction = reactionService.toggleReaction(postId, userId, emoji);

            Map<String, Object> response = new HashMap<>();
            if (reaction != null) {
                // Return basic reaction info without full DTO
                response.put("reactionId", reaction.getReactionID());
                response.put("emoji", reaction.getEmoji().name());
                response.put("userId", reaction.getUser().getId_user());
            } else {
                response.put("removed", true);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get reactions for post
    @GetMapping("/by-post/{postId}")
    public ResponseEntity<List<Map<String, Object>>> getReactionsByPost(@PathVariable Integer postId) {
        List<Reaction> reactions = reactionService.getReactionsByPost(postId);

        List<Map<String, Object>> response = reactions.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reactionId", r.getReactionID());
                    map.put("emoji", r.getEmoji().name());
                    map.put("userId", r.getUser().getId_user());
                    map.put("username", r.getUser().getNom());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}

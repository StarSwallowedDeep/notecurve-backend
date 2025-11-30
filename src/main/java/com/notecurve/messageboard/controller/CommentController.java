package com.notecurve.messageboard.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.notecurve.messageboard.dto.CommentRequest;
import com.notecurve.messageboard.dto.CommentDTO;
import com.notecurve.messageboard.service.CommentService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/message-board/{messageBoardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable("messageBoardId") Long messageBoardId, 
            @RequestBody CommentRequest commentRequest) {

        CommentDTO commentDTO = commentService.createComment(messageBoardId, commentRequest.getContent());
        return ResponseEntity.ok(commentDTO);
    }

    @GetMapping("/message-board/{messageBoardId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CommentDTO>> getCommentsByMessageBoard(@PathVariable("messageBoardId") Long messageBoardId) {
        List<CommentDTO> commentDTOs = commentService.getCommentsByMessageBoard(messageBoardId);
        return ResponseEntity.ok(commentDTOs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") Long id) {
        boolean isDeleted = commentService.deleteComment(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

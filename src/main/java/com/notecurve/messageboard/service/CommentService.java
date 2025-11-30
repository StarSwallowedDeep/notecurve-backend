package com.notecurve.messageboard.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.notecurve.messageboard.dto.CommentDTO;
import com.notecurve.messageboard.domain.Comment;
import com.notecurve.messageboard.domain.MessageBoard;
import com.notecurve.messageboard.repository.CommentRepository;
import com.notecurve.messageboard.repository.MessageBoardRepository;
import com.notecurve.user.domain.User;
import com.notecurve.user.repository.UserRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MessageBoardRepository messageBoardRepository;

    @Autowired
    private UserRepository userRepository;

    public CommentDTO createComment(Long messageBoardId, String content) {
        MessageBoard messageBoard = getMessageBoard(messageBoardId);
        User currentUser = getCurrentUser();

        if (commentRepository.existsByMessageBoardAndUser(messageBoard, currentUser)) {
            throw new RuntimeException("각 사용자는 하나의 댓글만 작성할 수 있습니다.");
        }

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setMessageBoard(messageBoard);
        comment.setUser(currentUser);

        comment = commentRepository.save(comment);

        return convertToDTO(comment);
    }

    public List<CommentDTO> getCommentsByMessageBoard(Long messageBoardId) {
        MessageBoard messageBoard = getMessageBoard(messageBoardId);

        // 수정된 findByMessageBoard를 사용하여 User 정보를 한 번에 로딩
        List<Comment> comments = commentRepository.findByMessageBoard(messageBoard);  

        // Comment -> CommentDTO 변환
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean deleteComment(Long id) {
        Comment comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            return false;
        }

        User currentUser = getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("댓글 작성자만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
        return true;
    }

    // 메시지 보드 조회
    private MessageBoard getMessageBoard(Long messageBoardId) {
        return messageBoardRepository.findById(messageBoardId)
                .orElseThrow(() -> new RuntimeException("Message Board not found"));
    }

    // 현재 로그인된 사용자 가져오기
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Comment -> CommentDTO 변환
    private CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(comment.getId(), comment.getContent(), comment.getUserDTO());
    }
}

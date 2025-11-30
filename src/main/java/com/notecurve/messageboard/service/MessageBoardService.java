package com.notecurve.messageboard.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import com.notecurve.messageboard.domain.MessageBoard;
import com.notecurve.messageboard.repository.MessageBoardRepository;
import com.notecurve.user.domain.User;
import com.notecurve.user.repository.UserRepository; 
import com.notecurve.auth.security.UserDetailsImpl;

@Service
public class MessageBoardService {

    private final MessageBoardRepository messageBoardRepository;
    private final UserRepository userRepository;

    // 생성자 주입
    public MessageBoardService(MessageBoardRepository messageBoardRepository, UserRepository userRepository) {
        this.messageBoardRepository = messageBoardRepository;
        this.userRepository = userRepository;
    }

    // 게시판 생성
    public MessageBoard createMessageBoard(String title) {
        String username = getCurrentUsername();
        User user = userRepository.findByLoginIdOrThrow(username);

        MessageBoard messageBoard = new MessageBoard();
        messageBoard.setTitle(title);
        messageBoard.setUser(user);

        return messageBoardRepository.save(messageBoard);
    }

    // 전체 게시판 조회
    public List<MessageBoard> getAllMessageBoards() {
        return messageBoardRepository.findAll();
    }

    // 게시판 조회
    public MessageBoard getMessageBoard(Long id) {
        return messageBoardRepository.findById(id).orElse(null);
    }

    // 게시판 삭제
    public boolean deleteMessageBoard(Long id) {
        MessageBoard messageBoard = messageBoardRepository.findById(id).orElse(null);
        if (messageBoard != null) {
            String currentUsername = getCurrentUsername();
            User currentUser = userRepository.findByLoginIdOrThrow(currentUsername);

            if (messageBoard.getUser().equals(currentUser)) {
                messageBoardRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

    // 현재 로그인한 사용자의 username을 반환하는 메소드
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getUsername();
        }
        throw new IllegalStateException("로그인된 사용자가 없습니다.");
    }
}

package com.notecurve.messageboard.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.notecurve.messageboard.domain.MessageBoard;
import com.notecurve.messageboard.service.MessageBoardService;
import com.notecurve.messageboard.dto.MessageBoardDTO;

@RestController
@RequestMapping("/api/message-boards")
public class MessageBoardController {

    private final MessageBoardService messageBoardService;

    // 생성자 주입
    public MessageBoardController(MessageBoardService messageBoardService) {
        this.messageBoardService = messageBoardService;
    }

    // 게시판 생성 - title만 받음
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageBoard> createMessageBoard(@RequestBody MessageBoardDTO messageBoardDto) {
        // 게시판 생성 시, 현재 로그인한 사용자 정보를 기반으로 게시판 생성
        MessageBoard messageBoard = messageBoardService.createMessageBoard(messageBoardDto.getTitle());
        return ResponseEntity.ok(messageBoard);
    }

    // 전체 게시판 조회
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<MessageBoardDTO>> getAllMessageBoards() {
        List<MessageBoard> messageBoards = messageBoardService.getAllMessageBoards();
        List<MessageBoardDTO> messageBoardDTOs = messageBoards.stream()
            .map(messageBoard -> convertToDTO(messageBoard, false))  // 댓글은 제외하고 변환
            .collect(Collectors.toList());
        return ResponseEntity.ok(messageBoardDTOs);
    }

    // 게시판 조회 - ID로 조회
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<MessageBoardDTO> getMessageBoard(@PathVariable("id") Long id) {
        MessageBoard messageBoard = messageBoardService.getMessageBoard(id);
        if (messageBoard != null) {
            // 게시판 조회 시, 댓글 포함 여부를 true로 설정
            MessageBoardDTO messageBoardDTO = convertToDTO(messageBoard, true); 
            return ResponseEntity.ok(messageBoardDTO);
        }
        return ResponseEntity.notFound().build();
    }

    // 게시판 삭제 - ID로 삭제
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteMessageBoard(@PathVariable("id") Long id) {
        boolean isDeleted = messageBoardService.deleteMessageBoard(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // DTO 변환 로직
    private MessageBoardDTO convertToDTO(MessageBoard messageBoard, boolean includeComments) {
        String formattedDate = messageBoard.getFormattedCreatedAt();
        
        if (includeComments) {
            return new MessageBoardDTO(
                    messageBoard.getId(),
                    messageBoard.getTitle(),
                    formattedDate,
                    messageBoard.getCommentsDTO(),
                    messageBoard.getUser().getName()
            );
        } else {
            return new MessageBoardDTO(
                    messageBoard.getId(),
                    messageBoard.getTitle(),
                    formattedDate,
                    null,
                    messageBoard.getUser().getName()
            );
        }
    }
}

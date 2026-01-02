package com.notecurve.note.controller;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.notecurve.auth.security.UserDetailsImpl;
import com.notecurve.note.dto.NoteDTO;
import com.notecurve.note.dto.NotesRequest;
import com.notecurve.note.service.NoteService;
import com.notecurve.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // 사용자별 노트 조회
    @GetMapping
    public ResponseEntity<List<NoteDTO>> getNotes(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        List<NoteDTO> noteDTOs = noteService.getNotesByUser(user).stream()
                .map(noteService::convertToDTO)
                .toList();
        return ResponseEntity.ok(noteDTOs);
    }

    // 특정 노트 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNote(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        try {
            return ResponseEntity.ok(noteService.convertToDTO(noteService.getNoteWithRelations(id, user)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 노트 생성
    @PostMapping
    public ResponseEntity<List<NoteDTO>> createNotes(@Valid @RequestBody NotesRequest notesRequest,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        try {
            List<NoteDTO> savedNotes = noteService.createNotes(user, notesRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedNotes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // 노트 수정
    @PatchMapping("/{id}")
    public ResponseEntity<NoteDTO> updateNote(@PathVariable Long id,
                                              @Valid @RequestBody NoteDTO noteDTO,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        try {
            NoteDTO updated = noteService.updateNote(user, id, noteDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 노트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        try {
            noteService.deleteNote(noteService.getNoteWithRelations(id, user));
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

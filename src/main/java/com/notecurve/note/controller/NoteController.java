package com.notecurve.note.controller;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.extern.slf4j.Slf4j;

import com.notecurve.auth.security.UserDetailsImpl;
import com.notecurve.note.domain.Note;
import com.notecurve.note.dto.NoteDTO;
import com.notecurve.note.dto.NotesRequest;
import com.notecurve.note.service.NoteService;
import com.notecurve.notefile.service.NoteFileService;
import com.notecurve.category.dto.CategoryDTO;
import com.notecurve.user.dto.UserDTO;
import com.notecurve.user.domain.User;
import com.notecurve.category.domain.Category;

@RestController
@RequestMapping("/api/notes")
@Slf4j
public class NoteController {

    private final NoteService noteService;
    private final NoteFileService noteFileService;

    public NoteController(NoteService noteService, NoteFileService noteFileService) {
        this.noteService = noteService;
        this.noteFileService = noteFileService;
    }

    // 사용자별 노트 조회
    @GetMapping
    public ResponseEntity<List<NoteDTO>> getNotes() {
        User user = getCurrentUser();
        List<NoteDTO> noteDTOs = noteService.getNotesByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(noteDTOs);
    }

    // 특정 노트 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNote(@PathVariable("id") Long id) {
        User user = getCurrentUser();
        try {
            Note note = noteService.getNoteWithRelations(id, user);
            return ResponseEntity.ok(convertToDTO(note));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 노트 생성
    @PostMapping
    public ResponseEntity<List<NoteDTO>> createNotes(@Valid @RequestBody NotesRequest notesRequest) {
        User user = getCurrentUser();

        // category를 final로 선언하여 람다 내부에서 사용 가능
        final Category category;
        if (notesRequest.getCategory() != null) {
            Category tempCategory = new Category();
            tempCategory.setId(notesRequest.getCategory());

            if (!noteService.isUserAuthorizedForCategory(tempCategory.getId(), user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            category = tempCategory;
        } else {
            category = null;
        }

        List<NoteDTO> savedNoteDTOs = notesRequest.getNotes().stream().map(noteDTO -> {
            Note note = new Note();
            note.setTitle(noteDTO.getTitle() != null ? noteDTO.getTitle() : "제목 없음");
            note.setContent(noteDTO.getContent() != null ? noteDTO.getContent() : "내용 없음");
            note.setUser(user);
            note.setCategory(category);

            Note savedNote = noteService.saveNote(note);
            return convertToDTO(savedNote);
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedNoteDTOs);
    }

    // 노트 수정
    @PatchMapping("/{id}")
    public ResponseEntity<NoteDTO> updateNote(@PathVariable("id") Long id, @Valid @RequestBody NoteDTO noteDTO) {
        User user = getCurrentUser();
        Note existingNote;
        try {
            existingNote = noteService.getNoteWithRelations(id, user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }

        if (!checkCategoryAuthorization(existingNote.getCategory(), user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 파일 삭제 처리 (부분 삭제)
        if (noteDTO.getDeletedFileIds() != null && !noteDTO.getDeletedFileIds().isEmpty()) {
            for (Long fileId : noteDTO.getDeletedFileIds()) {
                try {
                    noteFileService.deleteFile(fileId, user.getId());
                } catch (IOException e) {
                    log.error("Failed to delete file with id {}", fileId, e);
                }
            }
        }

        existingNote.setTitle(noteDTO.getTitle() != null ? noteDTO.getTitle() : "제목 없음");
        existingNote.setContent(noteDTO.getContent() != null ? noteDTO.getContent() : "내용 없음");

        Note updatedNote = noteService.saveNote(existingNote);
        return ResponseEntity.ok(convertToDTO(updatedNote));
    }

    // 노트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable("id") Long id) {
        User user = getCurrentUser();
        try {
            Note note = noteService.getNoteWithRelations(id, user);
            noteService.deleteNote(note);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    private NoteDTO convertToDTO(Note note) {
        String userName = note.getUser() != null ? note.getUser().getName() : "Unknown";
        Category category = note.getCategory();
        Long categoryId = category != null ? category.getId() : null;
        String categoryName = category != null ? category.getName() : "No Category";
        UserDTO categoryUserDTO = category != null && category.getUser() != null
                ? new UserDTO(category.getUser().getName())
                : null;

        return NoteDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdDate(note.getCreatedDate())
                .user(new UserDTO(userName))
                .category(new CategoryDTO(categoryId, categoryName, categoryUserDTO))
                .build();
    }

    private boolean checkCategoryAuthorization(Category category, User user) {
        return category == null || noteService.isUserAuthorizedForCategory(category.getId(), user);
    }
}

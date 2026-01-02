package com.notecurve.note.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notecurve.category.domain.Category;
import com.notecurve.category.repository.CategoryRepository;
import com.notecurve.note.domain.Note;
import com.notecurve.note.dto.NoteDTO;
import com.notecurve.note.dto.NotesRequest;
import com.notecurve.note.repository.NoteRepository;
import com.notecurve.notefile.service.NoteFileService;
import com.notecurve.user.domain.User;
import com.notecurve.category.dto.CategoryDTO;
import com.notecurve.notefile.domain.NoteFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteFileService noteFileService;
    private final CategoryRepository categoryRepository;

    // 사용자별 노트 조회
    public List<Note> getNotesByUser(User user) {
        return noteRepository.findByUserWithCategory(user);
    }

    // 카테고리별 노트 조회
    public List<Note> getNotesByCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to category");
        }

        return noteRepository.findByCategoryWithFetch(category);
    }

    // 특정 노트 조회
    public Note getNoteWithRelations(Long noteId, User user) {
        return noteRepository.findByIdAndUserWithCategory(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found or unauthorized"));
    }

    // 노트 생성
    @Transactional
    public List<NoteDTO> createNotes(User user, NotesRequest notesRequest) {
        
        // category를 final로 선언하여 람다 내부에서 사용 가능
        final Category category = (notesRequest.getCategory() != null) ? new Category() : null;

        if (notesRequest.getCategory() != null) {
            category.setId(notesRequest.getCategory());
            if (!isUserAuthorizedForCategory(category.getId(), user)) {
                throw new RuntimeException("Forbidden category access");
            }
        }

        return notesRequest.getNotes().stream().map(dto -> {
            Note note = new Note();
            note.setTitle(dto.getTitle() != null ? dto.getTitle() : "제목 없음");
            note.setContent(dto.getContent() != null ? dto.getContent() : "내용 없음");
            note.setUser(user);
            note.setCategory(category);

            Note saved = saveNote(note);
            return convertToDTO(saved);
        }).toList();
    }

    // 노트 수정
    @Transactional
    public NoteDTO updateNote(User user, Long noteId, NoteDTO noteDTO) {
        Note note = getNoteWithRelations(noteId, user);

        if (noteDTO.getDeletedFileIds() != null) {
            for (Long fileId : noteDTO.getDeletedFileIds()) {
                try {
                    noteFileService.deleteFile(fileId, user.getId());
                } catch (IOException e) {
                    throw new RuntimeException("파일 삭제 실패", e);
                }
            }
        }

        note.setTitle(noteDTO.getTitle() != null ? noteDTO.getTitle() : "제목 없음");
        note.setContent(noteDTO.getContent() != null ? noteDTO.getContent() : "내용 없음");

        Note updated = saveNote(note);
        return convertToDTO(updated);
    }

    // 노트 저장
    @Transactional
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    // 노트 삭제
    @Transactional
    public void deleteNote(Note note) {
        if (note.getFiles() != null) {
            for (NoteFile file : note.getFiles()) {
                try {
                    noteFileService.deletePhysicalFile(file);
                } catch (IOException e) {
                    throw new RuntimeException("파일 삭제 실패", e);
                }
            }
        }
        noteRepository.delete(note);
    }

    // 카테고리 권한 체크
    public boolean isUserAuthorizedForCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return category.getUser().getId().equals(user.getId());
    }

    // DTO 변환
    public NoteDTO convertToDTO(Note note) {
        Category category = note.getCategory();
        Long categoryId = category != null ? category.getId() : null;
        String categoryName = category != null ? category.getName() : "No Category";

        return NoteDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdDate(note.getCreatedDate())
                .category(new CategoryDTO(categoryId, categoryName))
                .build();
    }
}

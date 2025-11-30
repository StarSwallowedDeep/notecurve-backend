package com.notecurve.note.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.notecurve.category.domain.Category;
import com.notecurve.category.repository.CategoryRepository;
import com.notecurve.note.domain.Note;
import com.notecurve.note.repository.NoteRepository;
import com.notecurve.notefile.domain.NoteFile;
import com.notecurve.notefile.service.NoteFileService;
import com.notecurve.user.domain.User;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final NoteFileService noteFileService;

    public NoteService(NoteRepository noteRepository,
                       CategoryRepository categoryRepository,
                       NoteFileService noteFileService) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.noteFileService = noteFileService;
    }

    public List<Note> getNotesByUser(User user) {
        return noteRepository.findAllByUserWithCategoryAndUser(user);
    }

    public List<Note> getNotesByCategory(Long categoryId, User user) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to category");
        }

        return noteRepository.findByCategoryWithFetch(category);
    }

    public Note getNoteWithRelations(Long noteId, User user) {
        return noteRepository.findByIdAndUserWithCategoryAndUser(noteId, user)
                .orElseThrow(() -> new RuntimeException("Note not found or unauthorized"));
    }

    @Transactional
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(Long id, User user, String newTitle, String newContent, Category category) {

        Note note = noteRepository.findByIdAndUserWithCategoryAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Note not found or unauthorized"));

        if (category != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized category assignment");
        }

        note.setTitle(newTitle);
        note.setContent(newContent);
        note.setCategory(category);

        return note;
    }

    @Transactional
    public void deleteNote(Note note) {
        if (note.getFiles() != null && !note.getFiles().isEmpty()) {
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

    public boolean isUserAuthorizedForCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return category.getUser().getId().equals(user.getId());
    }
}

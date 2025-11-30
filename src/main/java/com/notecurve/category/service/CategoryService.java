package com.notecurve.category.service;

import com.notecurve.category.domain.Category;
import com.notecurve.category.repository.CategoryRepository;
import com.notecurve.note.service.NoteService;
import com.notecurve.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.notecurve.note.domain.Note;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final NoteService noteService;

    public CategoryService(CategoryRepository categoryRepository, NoteService noteService) {
        this.categoryRepository = categoryRepository;
        this.noteService = noteService;
    }

    // 사용자별 카테고리 조회
    public List<Category> getCategoriesByUser(User user) {
        return categoryRepository.findByUser(user); // 이미 @EntityGraph가 리포지토리에서 처리됨
    }

    // 특정 카테고리 조회 (사용자 필터)
    public Optional<Category> getCategoryByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user);
    }

    // 카테고리 저장
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // 카테고리 삭제
    @Transactional
    public void deleteCategoryAndNotes(Category category) {
        // 카테고리와 관련된 노트를 먼저 삭제
        category.getNotes().forEach(noteService::deleteNote);

        // 카테고리 삭제
        categoryRepository.delete(category);
    }
}

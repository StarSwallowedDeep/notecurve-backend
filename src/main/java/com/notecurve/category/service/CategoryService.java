package com.notecurve.category.service;

import com.notecurve.category.domain.Category;
import com.notecurve.category.repository.CategoryRepository;
import com.notecurve.category.dto.CategoryDTO;
import com.notecurve.note.service.NoteService;
import com.notecurve.note.dto.NoteSummaryDTO;
import com.notecurve.user.domain.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final NoteService noteService;

    public Optional<Category> getCategoryByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user);
    }

    // 사용자별 카테고리 조회 + DTO 변환
    public List<CategoryDTO> getCategoriesDTOByUser(User user) {
        List<Category> categories = categoryRepository.findByUser(user);
        return categories.stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());
    }

    // 특정 카테고리 조회 + DTO 변환
    public Optional<CategoryDTO> getCategoryDTOByIdAndUser(Long id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
                .map(this::toCategoryDTO);
    }

    // 카테고리 저장
    public CategoryDTO saveCategory(Category category) {
        Category saved = categoryRepository.save(category);
        return toCategoryDTO(saved);
    }

    // 카테고리 삭제
    @Transactional
    public void deleteCategoryAndNotes(Category category) {
        category.getNotes().forEach(noteService::deleteNote);
        categoryRepository.delete(category);
    }

    // Category → CategoryDTO 변환 메서드
    private CategoryDTO toCategoryDTO(Category category) {
        List<NoteSummaryDTO> noteSummaries = category.getNotes().stream()
                .map(note -> new NoteSummaryDTO(note.getId(), note.getTitle()))
                .collect(Collectors.toList());

        return new CategoryDTO(
                category.getId(),
                category.getName(),
                noteSummaries
        );
    }
}

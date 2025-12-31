package com.notecurve.category.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.notecurve.auth.security.UserDetailsImpl;
import com.notecurve.category.domain.Category;
import com.notecurve.category.service.CategoryService;
import com.notecurve.category.dto.CategoryDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 사용자별 카테고리 조회 (notes 포함)
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(categoryService.getCategoriesDTOByUser(userDetails.getUser()));
    }

    // 특정 카테고리 조회 (notes 포함)
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<CategoryDTO> categoryDTOOpt = categoryService.getCategoryDTOByIdAndUser(id, userDetails.getUser());
        if (categoryDTOOpt.isPresent()) {
            return ResponseEntity.ok(categoryDTOOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 카테고리 생성
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody Category category, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        category.setUser(userDetails.getUser());
        if (category.getName() == null || category.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.saveCategory(category));
    }

    // 카테고리 수정
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody Category category, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<CategoryDTO> updatedOpt = categoryService.getCategoryByIdAndUser(id, userDetails.getUser())
                .map(existing -> {
                    existing.setName(category.getName());
                    return categoryService.saveCategory(existing);
                });
        if (updatedOpt.isPresent()) {
            return ResponseEntity.ok(updatedOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 카테고리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryAndNotes(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Optional<Category> categoryOpt = categoryService.getCategoryByIdAndUser(id, userDetails.getUser());
        if (categoryOpt.isPresent()) {
            categoryService.deleteCategoryAndNotes(categoryOpt.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

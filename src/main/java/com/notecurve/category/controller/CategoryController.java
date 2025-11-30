package com.notecurve.category.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.notecurve.auth.security.UserDetailsImpl;
import com.notecurve.category.domain.Category;
import com.notecurve.category.service.CategoryService;
import com.notecurve.note.dto.NoteSummaryDTO;
import com.notecurve.category.dto.CategoryDTO;
import com.notecurve.user.dto.UserDTO;
import com.notecurve.user.domain.User;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 사용자별 카테고리 조회 (notes 포함)
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userDetails.getUser();
        List<Category> categories = categoryService.getCategoriesByUser(user);

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::toCategoryDTO)
                .collect(Collectors.toList());

        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    // 특정 카테고리 조회 (notes 포함)
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userDetails.getUser();
        Optional<Category> categoryOpt = categoryService.getCategoryByIdAndUser(id, user);

        return categoryOpt
                .map(category -> new ResponseEntity<>(toCategoryDTO(category), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 카테고리 생성
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody Category category, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userDetails.getUser();
        category.setUser(user);

        if (category.getName() == null || category.getName().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Category savedCategory = categoryService.saveCategory(category);
        return new ResponseEntity<>(toCategoryDTO(savedCategory), HttpStatus.CREATED);
    }

    // 카테고리 수정 (PATCH)
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable("id") Long id, @RequestBody Category category, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userDetails.getUser();
        Optional<Category> existingOpt = categoryService.getCategoryByIdAndUser(id, user);

        return existingOpt
                .map(existing -> {
                    existing.setName(category.getName());
                    Category updated = categoryService.saveCategory(existing);
                    return new ResponseEntity<>(toCategoryDTO(updated), HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 카테고리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryAndNotes(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userDetails.getUser();
        Optional<Category> categoryOpt = categoryService.getCategoryByIdAndUser(id, user);

        if (categoryOpt.isPresent()) {
            categoryService.deleteCategoryAndNotes(categoryOpt.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Category 객체를 CategoryDTO 객체로 변환하는 메서드
    private CategoryDTO toCategoryDTO(Category category) {
        List<NoteSummaryDTO> noteSummaries = category.getNotes().stream()
                .map(note -> new NoteSummaryDTO(note.getId(), note.getTitle()))
                .collect(Collectors.toList());

        return new CategoryDTO(
                category.getId(),
                category.getName(),
                new UserDTO(category.getUser().getName()),
                noteSummaries
        );
    }
}

package com.notecurve.note.dto;

import com.notecurve.category.dto.CategoryDTO;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteDTO {

    private Long id;

    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    @Builder.Default
    private String title = "제목 없음";

    @Builder.Default
    private String content = "내용 없음";

    private LocalDateTime createdDate;

    private CategoryDTO category;

    private List<Long> deletedFileIds;
}

package com.notecurve.category.dto;

import java.util.Collections;
import java.util.List;

import com.notecurve.note.dto.NoteSummaryDTO;

public class CategoryDTO {
    private final Long id;
    private final String name;
    private final List<NoteSummaryDTO> notes;
    private final int notesCount;

    // 기존 생성자: 노트 개수만 받는 경우
    public CategoryDTO(Long id, String name, int notesCount) {
        this.id = id;
        this.name = name;
        this.notes = Collections.emptyList();
        this.notesCount = notesCount;
    }

    // 기존 생성자: 노트 리스트 없이 초기화
    public CategoryDTO(Long id, String name) {
        this(id, name, 0);
    }

    // 새 생성자: 노트 리스트 포함
    public CategoryDTO(Long id, String name, List<NoteSummaryDTO> notes) {
        this.id = id;
        this.name = name;
        this.notes = notes != null ? notes : Collections.emptyList();
        this.notesCount = this.notes.size(); // notesCount는 항상 자동 계산
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<NoteSummaryDTO> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public int getNotesCount() {
        return notesCount;
    }
}

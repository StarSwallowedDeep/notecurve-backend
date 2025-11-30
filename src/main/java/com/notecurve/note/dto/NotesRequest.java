package com.notecurve.note.dto;

import java.util.List;

import jakarta.validation.Valid;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotesRequest {

    private Long category;

    @Valid
    private List<NoteDTO> notes;
}

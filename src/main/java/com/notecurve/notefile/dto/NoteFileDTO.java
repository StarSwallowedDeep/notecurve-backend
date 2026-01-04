package com.notecurve.notefile.dto;

public record NoteFileDTO(
        Long id,
        String originalName,
        String fileType,
        Long fileSize
) {}

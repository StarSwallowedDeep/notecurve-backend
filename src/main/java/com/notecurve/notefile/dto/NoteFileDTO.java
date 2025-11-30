package com.notecurve.notefile.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoteFileDTO {

    private final Long id;
    private final String originalName;
    private final String fileType;
    private final Long fileSize;
}

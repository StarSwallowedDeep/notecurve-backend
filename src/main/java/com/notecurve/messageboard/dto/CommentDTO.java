package com.notecurve.messageboard.dto;

import com.notecurve.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private UserDTO user;
}

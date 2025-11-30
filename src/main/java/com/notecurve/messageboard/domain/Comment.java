package com.notecurve.messageboard.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.notecurve.user.domain.User;
import com.notecurve.user.dto.UserDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne
    @JoinColumn(name = "message_board_id")
    @JsonBackReference
    private MessageBoard messageBoard;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public UserDTO getUserDTO() {
        return new UserDTO(user.getName());
    }
}

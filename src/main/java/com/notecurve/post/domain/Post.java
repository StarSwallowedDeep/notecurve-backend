package com.notecurve.post.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

import com.notecurve.user.domain.User;
import com.notecurve.post.dto.PostRequestDto;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subtitle;
    private String category;

    @Lob
    private String content;

    private LocalDate date;

    private String thumbnailImageUrl;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> contentImageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 게시글 수정 메서드
    public void updateFrom(PostRequestDto dto, List<PostImage> updatedImages) {
        this.title = dto.getTitle();
        this.subtitle = dto.getSubtitle();
        this.category = dto.getCategory();
        this.content = dto.getContent();

        if (dto.getThumbnail() != null) {
            this.thumbnailImageUrl = dto.getThumbnail();
        }

        if (updatedImages != null) {
            setContentImageUrls(updatedImages);
        }
    }

    // 기존 PostImage 리스트를 모두 제거하고 새 리스트로 교체
    public void setContentImageUrls(List<PostImage> newPostImages) {
        this.contentImageUrls.clear();
        if (newPostImages != null) {
            for (PostImage img : newPostImages) {
                img.setPost(this);
                this.contentImageUrls.add(img);
            }
        }
    }
}

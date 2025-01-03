package com.buysellgo.helpdeskservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Entity
@Table(name = "tbl_onetoone_inquiry")
public class OneToOneInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "onetoonoe_inquiry_id")
    private Long onetooneInquiryId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public static OneToOneInquiry of(Long userId,
                                     String content) {
        return OneToOneInquiry.builder()
                .userId(userId)
                .content(content)
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                .build();
    }

    public Vo toVo(){
        return new Vo(onetooneInquiryId, userId, content, createdAt, updatedAt);
    }

    private record Vo(Long onetooneInquiryId,
                      Long userId,
                      String content,
                      Timestamp createdAt,
                      Timestamp updatedAt) {
    }
}

package com.goorm.jpa_basic.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "member")
@Getter
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId; //PK

    @Column(nullable = false, unique = true, length = 100)
    private String email; //이메일

    @Setter
    @Column(nullable = false)
    private String password; //비밀번호

    private String phoneNumber; //전화번호
    private String address; //주소
    private String mName; //이름

    @Setter
    private String status; //인증여부

    @CreationTimestamp
    private LocalDateTime createdAt; //생성날짜

    @UpdateTimestamp
    private LocalDateTime modifiedAt; //수정날짜

}

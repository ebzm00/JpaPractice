package com.goorm.jpa_basic.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "member")
@Getter
@Setter
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long member_id; //PK

    @Column(nullable = false, unique = true, length = 100)
    private String email; //이메일

    private String password; //비밀번호
    private String m_tel; //전화번호
    private String m_address; //주소

    @Column(nullable = false,length = 50)
    private String m_name; //이름

    private String status; //인증여부

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at; //생성날짜
    private LocalDateTime modified_at; //수정날짜

}


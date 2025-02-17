package com.goorm.jpa_basic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payment_id;

    private String payment_name;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at; //생성날짜
    private LocalDateTime modified_at; //수정날짜

}

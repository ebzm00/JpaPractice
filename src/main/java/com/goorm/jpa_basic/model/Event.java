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
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long event_id;

    private String event_name; //이벤트이름
    private String discount_type;  //할인타입
    private Integer discount_value; //할인값
    private LocalDateTime start_date; //할인시작일
    private LocalDateTime end_date; //할인종료일

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at; //생성날짜
    private LocalDateTime modified_at; //수정날짜
    private boolean active; //할인활성화유무
}

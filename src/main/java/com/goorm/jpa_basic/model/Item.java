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
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long item_id;

    @ManyToOne
    @JoinColumn(name="store_id")
    private Store store_id;

    @ManyToOne
    @JoinColumn(name="ct_id")
    private Category ct_id;

    private Integer i_price;

    private String image;

    //enum타입 작성법
    @Enumerated(EnumType.STRING)
    private OutStatus out_status;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at; //생성날짜
    private LocalDateTime modified_at; //수정날짜

    private String i_name;

    private String remark;

    @ManyToOne
    @JoinColumn(name="lang_id")
    private Lang lang_id;
}

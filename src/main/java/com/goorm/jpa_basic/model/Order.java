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
@Table(name="Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_id;

    private String table_number;

    //enum타입 작성법
    @Enumerated(EnumType.STRING)
    private OrderStatus order_status;

    private Integer amount;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment_id;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at; //생성날짜
    private LocalDateTime modified_at; //수정날짜

    @ManyToOne
    @JoinColumn(name ="store_id")
    private Store store_id;
}

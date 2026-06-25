package com.dailymart.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "wishlist")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Wishlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist public void prePersist() { addedAt = LocalDateTime.now(); }
}

package com.dailymart.repository;

import com.dailymart.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // field: approved (not isApproved)
    Page<Review> findByProductIdAndApprovedTrue(Long productId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :pid AND r.approved = true")
    Double getAverageRating(@Param("pid") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :pid AND r.approved = true")
    Long getReviewCount(@Param("pid") Long productId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);
}

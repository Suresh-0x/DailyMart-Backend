package com.dailymart.repository;

import com.dailymart.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    // field: active (not isActive)
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Product> searchProducts(@Param("q") String query, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "p.sellingPrice BETWEEN :min AND :max")
    Page<Product> findByPriceRange(@Param("min") BigDecimal min,
                                   @Param("max") BigDecimal max,
                                   Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :qty WHERE p.id = :id")
    void decrementStock(@Param("id") Long id, @Param("qty") int qty);
}

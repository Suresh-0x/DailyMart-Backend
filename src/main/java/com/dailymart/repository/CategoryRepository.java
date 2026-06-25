package com.dailymart.repository;

import com.dailymart.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    // field: active (not isActive)
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIsNullAndActiveTrueOrderByDisplayOrderAsc();
}

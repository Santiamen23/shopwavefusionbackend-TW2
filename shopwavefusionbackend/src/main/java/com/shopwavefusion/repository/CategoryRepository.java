package com.shopwavefusion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopwavefusion.modal.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	public Category findByName(String name);

	@Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND LOWER(c.parentCategory.name) = LOWER(:parentCategoryName)")
	public Category findByNameAndParant(@Param("name") String name,
			@Param("parentCategoryName") String parentCategoryName);
}


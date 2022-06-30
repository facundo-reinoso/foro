package com.example.demo.repository;

import com.example.demo.models.CommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<CommentsEntity, Long> {

    @Query(value = "SELECT * FROM comments WHERE comments.parent_id LIKE %?1% AND comments.status LIKE 1 ORDER BY comments.creation_date", nativeQuery = true)
    List<CommentsEntity> getComments(Long parentId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE comments SET comments.status = 0 WHERE comments.parent_id LIKE %?1% OR comments.id LIKE %?1%", nativeQuery = true)
    void deleteCommentsById(Long id);
}

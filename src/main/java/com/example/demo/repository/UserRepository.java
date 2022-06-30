package com.example.demo.repository;

import com.example.demo.models.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UsersEntity, Long> {

    @Query(value = "SELECT * FROM users WHERE status = 1 AND username = ?1", nativeQuery = true)
    Optional<UsersEntity> getUser(String user);

    @Query(value = "SELECT * FROM users WHERE status = 1 AND id = ?1", nativeQuery = true)
    Optional<UsersEntity> getUserById(Long id);

    @Query(value = "SELECT * FROM users WHERE status = 1 AND username LIKE ?1%", nativeQuery = true)
    public List<UsersEntity> getUserByUser(String user);

}

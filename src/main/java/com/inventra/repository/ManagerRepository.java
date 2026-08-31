package com.inventra.repository;

import com.inventra.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {
     Optional<Manager> findByUserId(Long userId);
     boolean existsByUserId(Long userId);
     List<Manager> findByActiveTrue();
}
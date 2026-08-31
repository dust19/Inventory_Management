package com.inventra.repository;

import com.inventra.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
     Optional<Staff> findByUserId(Long userId);
     boolean existsByUserId(Long userId);
     List<Staff> findByManagerId(Long managerId);
     List<Staff> findByActiveTrue();
}
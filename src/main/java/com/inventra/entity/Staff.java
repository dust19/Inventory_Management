package com.inventra.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
@Data
public class Staff {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false, unique = true)
     private User user;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "manager_id")   // nullable — admin assigns later
     private Manager manager;

     @Column(name = "is_active")
     private boolean active = true;

     @Column(name = "created_at", updatable = false)
     private LocalDateTime createdAt;

     @Column(name = "updated_at")
     private LocalDateTime updatedAt;

     @PrePersist
     protected void onCreate() {
          createdAt = LocalDateTime.now();
          updatedAt = LocalDateTime.now();
     }

     @PreUpdate
     protected void onUpdate() {
          updatedAt = LocalDateTime.now();
     }
}
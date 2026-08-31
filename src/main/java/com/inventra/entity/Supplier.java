package com.inventra.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="Suppliers")
@Data
public class Supplier {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true, unique = true)
    private User user;

    @ManyToOne
	@JoinColumn(name="category_id", nullable=false)
	private Category category;
	
	@Column(name="company_name", nullable=false)
	private String companyName;

	private String address;
	
	@Column(name="is_active")
	private boolean active=true;
	
	@Column(name="created_at",updatable=false)
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

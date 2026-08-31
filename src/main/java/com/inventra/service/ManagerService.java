package com.inventra.service;

import com.inventra.dto.request.ManagerRequestDTO;
import com.inventra.dto.response.ManagerResponseDTO;
import com.inventra.entity.Manager;
import com.inventra.entity.User;
import com.inventra.entity.enums.Role;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.ManagerRepository;
import com.inventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {

     private final ManagerRepository managerRepo;
     private final UserRepository userRepo;

     @Transactional
     public ManagerResponseDTO createManager(ManagerRequestDTO dto) {
          if (dto.getUserId() == null)
               throw new IllegalArgumentException("UserId is required");

          User user = userRepo.findById(dto.getUserId())
                  .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + dto.getUserId()));

          if (user.getRole() != Role.ROLE_MANAGER)
               throw new IllegalArgumentException("User must have ROLE_MANAGER");

          if (managerRepo.existsByUserId(dto.getUserId()))
               throw new IllegalArgumentException("Manager profile already exists for this user");

          Manager manager = new Manager();
          manager.setUser(user);
          manager.setDepartment(dto.getDepartment());
          manager.setActive(true);

          return toDTO(managerRepo.save(manager));
     }

     public List<ManagerResponseDTO> getAllManagers() {
          return managerRepo.findAll().stream().map(this::toDTO).toList();
     }

     public ManagerResponseDTO getManagerById(Long id) {
          return toDTO(managerRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id " + id)));
     }

     public ManagerResponseDTO getManagerByUserId(Long userId) {
          return toDTO(managerRepo.findByUserId(userId)
                  .orElseThrow(() -> new ResourceNotFoundException("Manager not found for userId " + userId)));
     }

     @Transactional
     public ManagerResponseDTO updateManager(Long id, ManagerRequestDTO dto) {
          Manager existing = managerRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id " + id));

          if (dto.getDepartment() != null && !dto.getDepartment().isBlank())
               existing.setDepartment(dto.getDepartment().trim());

          return toDTO(managerRepo.save(existing));
     }

     @Transactional
     public ManagerResponseDTO setActive(Long id, boolean active) {

          Manager manager = managerRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

          manager.setActive(active);

          if (manager.getUser() != null) {
               manager.getUser().setActive(active);
               userRepo.save(manager.getUser());
          }

          return toDTO(managerRepo.save(manager));
     }

     @Transactional
     public void deleteManager(Long id) {
          Manager manager = managerRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id " + id));
          managerRepo.delete(manager);
     }

     private ManagerResponseDTO toDTO(Manager m) {
          ManagerResponseDTO dto = new ManagerResponseDTO();
          dto.setId(m.getId());
          dto.setDepartment(m.getDepartment());
          dto.setActive(m.isActive());
          dto.setCreatedAt(m.getCreatedAt());
          if (m.getUser() != null) {
               dto.setUserId(m.getUser().getId());
               dto.setUserName(m.getUser().getName());
               dto.setUserEmail(m.getUser().getEmail());
          }
          return dto;
     }
}
package com.inventra.service;

import com.inventra.dto.request.StaffRequestDTO;
import com.inventra.dto.response.StaffResponseDTO;
import com.inventra.entity.Manager;
import com.inventra.entity.Staff;
import com.inventra.entity.User;
import com.inventra.entity.enums.Role;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.ManagerRepository;
import com.inventra.repository.StaffRepository;
import com.inventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

     private final StaffRepository staffRepo;
     private final UserRepository userRepo;
     private final ManagerRepository managerRepo;

     @Transactional
     public StaffResponseDTO createStaff(StaffRequestDTO dto) {
          if (dto.getUserId() == null)
               throw new IllegalArgumentException("UserId is required");

          User user = userRepo.findById(dto.getUserId())
                  .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + dto.getUserId()));

          if (user.getRole() != Role.ROLE_STAFF)
               throw new IllegalArgumentException("User must have ROLE_STAFF");

          if (staffRepo.existsByUserId(dto.getUserId()))
               throw new IllegalArgumentException("Staff profile already exists for this user");

          Staff staff = new Staff();
          staff.setUser(user);
          staff.setActive(true);

          if (dto.getManagerId() != null) {
               Manager manager = managerRepo.findById(dto.getManagerId())
                       .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id " + dto.getManagerId()));
               staff.setManager(manager);
          }

          return toDTO(staffRepo.save(staff));
     }

     public List<StaffResponseDTO> getAllStaff() {
          return staffRepo.findAll().stream().map(this::toDTO).toList();
     }

     public StaffResponseDTO getStaffById(Long id) {
          return toDTO(staffRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id " + id)));
     }

     public StaffResponseDTO getStaffByUserId(Long userId) {
          return toDTO(staffRepo.findByUserId(userId)
                  .orElseThrow(() -> new ResourceNotFoundException("Staff not found for userId " + userId)));
     }

     public List<StaffResponseDTO> getStaffByManager(Long managerId) {
          return staffRepo.findByManagerId(managerId).stream().map(this::toDTO).toList();
     }

     @Transactional
     public StaffResponseDTO assignManager(Long staffId, Long managerId) {
          Staff staff = staffRepo.findById(staffId)
                  .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id " + staffId));

          Manager manager = managerRepo.findById(managerId)
                  .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id " + managerId));

          staff.setManager(manager);
          return toDTO(staffRepo.save(staff));
     }

     @Transactional
     public StaffResponseDTO updateStaff(Long id, StaffRequestDTO dto) {
          Staff existing = staffRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id " + id));

          if (dto.getManagerId() != null) {
               Manager manager = managerRepo.findById(dto.getManagerId())
                       .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id " + dto.getManagerId()));
               existing.setManager(manager);
          }

          return toDTO(staffRepo.save(existing));
     }

     @Transactional
     public StaffResponseDTO setActive(Long id, boolean active) {
          Staff staff = staffRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

          staff.setActive(active);

          if (staff.getUser() != null) {
               staff.getUser().setActive(active);
               userRepo.save(staff.getUser());
          }

          return toDTO(staffRepo.save(staff));
     }

     @Transactional
     public void deleteStaff(Long id) {
          Staff staff = staffRepo.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id " + id));
          staffRepo.delete(staff);
     }

     private StaffResponseDTO toDTO(Staff s) {
          StaffResponseDTO dto = new StaffResponseDTO();
          dto.setId(s.getId());
          dto.setActive(s.isActive());
          dto.setCreatedAt(s.getCreatedAt());
          if (s.getUser() != null) {
               dto.setUserId(s.getUser().getId());
               dto.setUserName(s.getUser().getName());
               dto.setUserEmail(s.getUser().getEmail());
          }
          if (s.getManager() != null) {
               dto.setManagerId(s.getManager().getId());
               dto.setManagerName(s.getManager().getUser() != null
                       ? s.getManager().getUser().getName() : null);
          }
          return dto;
     }
}
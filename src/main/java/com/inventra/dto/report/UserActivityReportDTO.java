package com.inventra.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActivityReportDTO {
     private Long totalUsers;
     private Long activeUsers;
     private Long inactiveUsers;
     private Long totalAdmins;
     private Long totalSuppliers;
     private Long totalCustomers;
     private List<UserSummaryDTO> users;
}
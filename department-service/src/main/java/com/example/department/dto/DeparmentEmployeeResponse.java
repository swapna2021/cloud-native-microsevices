package com.example.department.dto;

import java.util.List;

import com.example.department.entity.Department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeparmentEmployeeResponse {
	
	private Department department;
	private List<EmployeeDto> employees;

}

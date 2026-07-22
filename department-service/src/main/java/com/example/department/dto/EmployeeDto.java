package com.example.department.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto {
	private Integer eid;
	private String ename;
	private double salary;
	private String mobile;
	private String email;
	private Integer did;

}

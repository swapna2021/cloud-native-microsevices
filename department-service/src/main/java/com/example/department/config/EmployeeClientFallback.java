package com.example.department.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.department.dto.EmployeeDto;

@Component
public class EmployeeClientFallback  implements EmployeeClient{

	@Override
	public List<EmployeeDto> getEmployeesByDid(Integer did) {
		
		System.out.println("Employee Service is down ");
		return new ArrayList<EmployeeDto>();
	}
	
	

}

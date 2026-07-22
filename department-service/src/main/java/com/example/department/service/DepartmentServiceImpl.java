package com.example.department.service;

import com.example.department.config.EmployeeClient;
import com.example.department.dao.DepartmentDao;
import com.example.department.dto.DepartmentRequestDto;
import com.example.department.dto.DepartmentResponseDto;
import com.example.department.dto.EmployeeDto;
import com.example.department.entity.Department;
import com.example.department.exception.DepartmentNotFoundException;
import com.example.department.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDao departmentDao;
    //private final RestTemplate restTemplate;
    private final EmployeeClient employeeClient;

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto dto) {
        if (departmentDao.existsByName(dto.dname())) {
            throw new DuplicateResourceException(
                    "Department name already exists: " + dto.dname());
        }

        Department department = Department.builder()
                .dname(dto.dname())
                .location(dto.location())
                .build();

        return mapToResponse(departmentDao.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentDao.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDto getDepartmentById(Integer did) {
        return mapToResponse(findDepartment(did));
    }

    @Override
    public DepartmentResponseDto updateDepartment(
            Integer did, DepartmentRequestDto dto) {

        Department department = findDepartment(did);

        if (departmentDao.existsByNameAndIdNot(dto.dname(), did)) {
            throw new DuplicateResourceException(
                    "Department name already exists: " + dto.dname());
        }

        department.setDname(dto.dname());
        department.setLocation(dto.location());

        return mapToResponse(departmentDao.save(department));
    }

    @Override
    public void deleteDepartment(Integer did) {
        Department department = findDepartment(did);
        departmentDao.delete(department);
    }

    private Department findDepartment(Integer did) {
        return departmentDao.findById(did)
                .orElseThrow(() ->
                        new DepartmentNotFoundException(
                                "Department not found with id: " + did));
    }

    private DepartmentResponseDto mapToResponse(Department department) {
        return new DepartmentResponseDto(
                department.getDid(),
                department.getDname(),
                department.getLocation()
        );
    }

	@Override
	@Transactional
	public Department getDepartmentWithEmployees(Integer did) {
		
		Department department=new Department();
		department=departmentDao.findById(did).get();
		List<EmployeeDto> empList=employeeClient.getEmployeesByDid(did);
		
		if(empList==null)
			empList=new ArrayList<EmployeeDto>();
		department.setEmployees(empList);
		return department;
	}

//	@Override
//	public Department getDepartmentWithEmployees(Integer did) {
//		
//		//get departement of the given id
//		Department department=departmentDao.findById(did).get();
//		System.out.println(department);
//		
//		//url to call employeeList retrieving api from EmployeeService
//		String url="http://employee-service/api/employees/department/"+did;
//		
//		//restTemplate will call the above URL and set the response to empDto array
//		//embedded with responseEntity
//		ResponseEntity<EmployeeDto[]> empDtos= restTemplate.getForEntity(url, EmployeeDto[].class);
//		
//		
//		List<EmployeeDto> empList=new ArrayList<EmployeeDto>();
//		
//		//fetch employeeDtos from ResponseEntity Body
//		EmployeeDto[] dtos=empDtos.getBody();//fetch emp array from responseentity
//		
//		//convert array to list
//		empList=Arrays.asList(dtos)	;
//		
//		//set list of employees data to empList in the department
//		department.setEmployees(empList);
//		
//	
//		return department;
//	}
    
    
    
    
}

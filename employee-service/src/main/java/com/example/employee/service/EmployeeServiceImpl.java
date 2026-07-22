package com.example.employee.service;

import com.example.employee.dao.EmployeeDao;
import com.example.employee.dto.EmployeeRequestDto;
import com.example.employee.dto.EmployeeResponseDto;
import com.example.employee.entity.Employee;
import com.example.employee.exception.DuplicateResourceException;
import com.example.employee.exception.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

	
    private final EmployeeDao employeeDao;

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        validateUniqueFields(dto.email(), dto.mobile(), null);

        Employee employee = Employee.builder()
                .ename(dto.ename())
                .salary(dto.salary())
                .mobile(dto.mobile())
                .email(dto.email())
                .build();

        return mapToResponse(employeeDao.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeDao.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Integer eid) {
        return mapToResponse(findEmployee(eid));
    }

    @Override
    public EmployeeResponseDto updateEmployee(Integer eid, EmployeeRequestDto dto) {
        Employee employee = findEmployee(eid);
        validateUniqueFields(dto.email(), dto.mobile(), eid);

        employee.setEname(dto.ename());
        employee.setSalary(dto.salary());
        employee.setMobile(dto.mobile());
        employee.setEmail(dto.email());

        return mapToResponse(employeeDao.save(employee));
    }

    @Override
    public void deleteEmployee(Integer eid) {
        Employee employee = findEmployee(eid);
        employeeDao.delete(employee);
    }

    private Employee findEmployee(Integer eid) {
        return employeeDao.findById(eid)
                .orElseThrow(() ->
                        new EmployeeNotFoundException("Employee not found with id: " + eid));
    }

    private void validateUniqueFields(String email, String mobile, Integer eid) {
        boolean emailExists = eid == null
                ? employeeDao.existsByEmail(email)
                : employeeDao.existsByEmailAndEidNot(email, eid);

        boolean mobileExists = eid == null
                ? employeeDao.existsByMobile(mobile)
                : employeeDao.existsByMobileAndEidNot(mobile, eid);

        if (emailExists) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }

        if (mobileExists) {
            throw new DuplicateResourceException("Mobile number already exists: " + mobile);
        }
    }

    private EmployeeResponseDto mapToResponse(Employee employee) {
        return new EmployeeResponseDto(
                employee.getEid(),
                employee.getEname(),
                employee.getSalary(),
                employee.getMobile(),
                employee.getEmail()
        );
    }

	@Override
	@Transactional(readOnly = true)
	public List<EmployeeResponseDto> findByDid(Integer did) {
		List<Employee>  employees = employeeDao.findByDid(did);
		
		List<EmployeeResponseDto> employeeRespList=new ArrayList<EmployeeResponseDto>();
		
		for(Employee employee:employees)
		{
			EmployeeResponseDto responseDto=mapToResponse(employee);
			employeeRespList.add(responseDto);
		}
		return employeeRespList;
	}
	
}

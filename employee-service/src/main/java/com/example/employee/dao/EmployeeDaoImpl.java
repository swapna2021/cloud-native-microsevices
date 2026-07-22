package com.example.employee.dao;

import com.example.employee.entity.Employee;
import com.example.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmployeeDaoImpl implements EmployeeDao {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> findById(Integer eid) {
        return employeeRepository.findById(eid);
    }

    @Override
    public void delete(Employee employee) {
        employeeRepository.delete(employee);
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByMobile(String mobile) {
        return employeeRepository.existsByMobile(mobile);
    }

    @Override
    public boolean existsByEmailAndEidNot(String email, Integer eid) {
        return employeeRepository.existsByEmailAndEidNot(email, eid);
    }

    @Override
    public boolean existsByMobileAndEidNot(String mobile, Integer eid) {
        return employeeRepository.existsByMobileAndEidNot(mobile, eid);
    }

	@Override
	public List<Employee> findByDid(Integer did) {
		
		return employeeRepository.findByDid(did);
	}
}

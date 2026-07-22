package com.example.employee.repository;

import com.example.employee.entity.Employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    boolean existsByEmailAndEidNot(String email, Integer eid);
    boolean existsByMobileAndEidNot(String mobile, Integer eid);
    
    List<Employee> findByDid(Integer did);
}

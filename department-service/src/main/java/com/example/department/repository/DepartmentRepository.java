package com.example.department.repository;

import com.example.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    boolean existsByDnameIgnoreCase(String dname);
    boolean existsByDnameIgnoreCaseAndDidNot(String dname, Integer did);
}

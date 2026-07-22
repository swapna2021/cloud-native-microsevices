package com.example.department.dao;

import com.example.department.entity.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentDao {
    Department save(Department department);
    List<Department> findAll();
    Optional<Department> findById(Integer did);
    void delete(Department department);
    boolean existsByName(String dname);
    boolean existsByNameAndIdNot(String dname, Integer did);
}

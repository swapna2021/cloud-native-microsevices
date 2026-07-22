package com.example.department.dao;

import com.example.department.entity.Department;
import com.example.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepartmentDaoImpl implements DepartmentDao {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Optional<Department> findById(Integer did) {
        return departmentRepository.findById(did);
    }

    @Override
    public void delete(Department department) {
        departmentRepository.delete(department);
    }

    @Override
    public boolean existsByName(String dname) {
        return departmentRepository.existsByDnameIgnoreCase(dname);
    }

    @Override
    public boolean existsByNameAndIdNot(String dname, Integer did) {
        return departmentRepository.existsByDnameIgnoreCaseAndDidNot(dname, did);
    }
}

package com.example.department.service;

import com.example.department.dto.DepartmentRequestDto;
import com.example.department.dto.DepartmentResponseDto;
import com.example.department.entity.Department;

import java.util.List;

public interface DepartmentService {
    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);
    List<DepartmentResponseDto> getAllDepartments();
    DepartmentResponseDto getDepartmentById(Integer did);
    DepartmentResponseDto updateDepartment(Integer did, DepartmentRequestDto requestDto);
    void deleteDepartment(Integer did);
    
    
    Department getDepartmentWithEmployees(Integer did);
}

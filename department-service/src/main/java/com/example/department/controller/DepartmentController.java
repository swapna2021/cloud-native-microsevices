package com.example.department.controller;

import com.example.department.dto.DepartmentRequestDto;
import com.example.department.dto.DepartmentResponseDto;
import com.example.department.entity.Department;
import com.example.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponseDto> createDepartment(
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{did}")
    public ResponseEntity<Department> getDepartmentById(
            @PathVariable Integer did) {
        return ResponseEntity.ok(departmentService.getDepartmentWithEmployees(did));
    }

    @PutMapping("/{did}")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable Integer did,
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        return ResponseEntity.ok(
                departmentService.updateDepartment(did, requestDto));
    }

    @DeleteMapping("/{did}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Integer did) {
        departmentService.deleteDepartment(did);
        return ResponseEntity.noContent().build();
    }
}

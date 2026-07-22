package com.example.employee.dto;

public record EmployeeResponseDto(
        Integer eid,
        String ename,
        Double salary,
        String mobile,
        String email
) {
}

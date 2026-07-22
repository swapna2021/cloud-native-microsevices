package com.example.employee.dto;

import jakarta.validation.constraints.*;

public record EmployeeRequestDto(

        @NotBlank(message = "Employee name is required")
        @Size(min = 2, max = 100, message = "Employee name must contain 2 to 100 characters")
        String ename,

        @NotNull(message = "Salary is required")
        @Positive(message = "Salary must be greater than zero")
        Double salary,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[6-9][0-9]{9}$",
                 message = "Mobile number must be a valid 10-digit Indian mobile number")
        String mobile,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email
) {
}

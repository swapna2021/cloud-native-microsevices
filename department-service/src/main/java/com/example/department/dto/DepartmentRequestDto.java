package com.example.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequestDto(

        @NotBlank(message = "Department name is required")
        @Size(min = 2, max = 100,
              message = "Department name must contain 2 to 100 characters")
        String dname,

        @NotBlank(message = "Location is required")
        @Size(min = 2, max = 100,
              message = "Location must contain 2 to 100 characters")
        String location
) {
}

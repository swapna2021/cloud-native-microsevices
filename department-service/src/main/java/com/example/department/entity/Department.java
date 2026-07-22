package com.example.department.entity;

import java.util.List;

import com.example.department.dto.EmployeeDto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer did;

    @Column(nullable = false, unique = true, length = 100)
    private String dname;

    @Column(nullable = false, length = 100)
    private String location;
    
    @Transient
    private List<EmployeeDto> employees;
}

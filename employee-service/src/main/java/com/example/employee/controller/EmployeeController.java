package com.example.employee.controller;

import com.example.employee.dto.EmployeeRequestDto;
import com.example.employee.dto.EmployeeResponseDto;
import com.example.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    
    
    @Value("${server.port}")
    private String serverPort;

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(
            @Valid @RequestBody EmployeeRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{eid}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(
            @PathVariable Integer eid) {
        return ResponseEntity.ok(employeeService.getEmployeeById(eid));
    }

    @PutMapping("/{eid}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Integer eid,
            @Valid @RequestBody EmployeeRequestDto requestDto) {
        return ResponseEntity.ok(employeeService.updateEmployee(eid, requestDto));
    }

    @DeleteMapping("/{eid}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Integer eid) {
        employeeService.deleteEmployee(eid);
        return ResponseEntity.noContent().build();
    }
    
    
//    @GetMapping("/department/{did}")
//    public ResponseEntity<Map<String , Object>> findByDid(@PathVariable Integer did){
//    	
//    	
//    	List<EmployeeResponseDto> employeeDtos =employeeService.findByDid(did);
//    	Map<String,Object> responseMap=new HashMap<String, Object>();
//    	responseMap.put("Port", serverPort);
//    	responseMap.put("employeeDtos", employeeDtos);
//    	System.out.println("Port handled : "+serverPort);
//    	return ResponseEntity.ok( responseMap);
//    }
    
    @GetMapping("/department/{did}")
    public ResponseEntity<List<EmployeeResponseDto>>  findByDid(@PathVariable Integer did){
    	List<EmployeeResponseDto> employeeDtos =employeeService.findByDid(did);
    	return ResponseEntity.ok(employeeDtos);
    	
    }
    
}

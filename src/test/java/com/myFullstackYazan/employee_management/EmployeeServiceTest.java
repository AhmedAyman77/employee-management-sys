package com.myFullstackYazan.employee_management;

import com.myFullstackYazan.employee_management.dtos.EmployeeCreate;
import com.myFullstackYazan.employee_management.entities.Department;
import com.myFullstackYazan.employee_management.entities.Employee;
import com.myFullstackYazan.employee_management.repositpories.DepartmentRepo;
import com.myFullstackYazan.employee_management.repositpories.EmployeeRepo;
import com.myFullstackYazan.employee_management.services.EmailService;
import com.myFullstackYazan.employee_management.services.EmployeeServiceImpl;
import com.myFullstackYazan.employee_management.shared.CustomResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
  @Mock
  private DepartmentRepo departmentRepo;

  @Mock
  private EmployeeRepo employeeRepo;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private EmployeeServiceImpl employeeService;

  private Department testDepartment;
  private UUID departmentId;
  private EmployeeCreate employeeCreate;

  @BeforeEach
  void setUp() {
    departmentId = UUID.randomUUID();
    testDepartment = new Department(departmentId, "IT");
    employeeCreate = new EmployeeCreate(
        "John",
        "Doe",
        "john@example.com",
        "1234567890",
        LocalDate.now(),
        "Developer",
        departmentId
    );
  }

  @Test
  @DisplayName("createOne should create employee successfully")
  void createOne_shouldCreateEmployeeSuccessfully() {
    // ARRANGE
    when(departmentRepo.findById(departmentId))
        .thenReturn(Optional.of(testDepartment));

    when(employeeRepo.save(any(Employee.class)))
        .thenAnswer(i -> i.getArgument(0));

    doNothing().when(emailService)
        .sendAccountCreationEmail(
            any(String.class), any(String.class));

    // ACT
    Employee result = employeeService.createOne(employeeCreate);

    // ASSERT
    assertEquals("John", result.getFirstName());

    // Verify
    verify(emailService, times(1))
        .sendAccountCreationEmail(eq("john@example.com"), any(String.class));
  }

  @Test
  @DisplayName("createOne should throw exception when department not found")
  void createOne_shouldThrowExceptionWhenDepNotFound() {
    when(departmentRepo.findById(departmentId)).thenReturn(Optional.empty());

    CustomResponseException exception = assertThrows(
        CustomResponseException.class,
        () -> employeeService.createOne(employeeCreate)
    );

    assertTrue(exception.getMessage().contains("Department with id " + departmentId + " not found"));

    verify(emailService, never())
        .sendAccountCreationEmail(any(String.class), any(String.class));
    verify(employeeRepo, never())
        .save(any(Employee.class));
  }
}

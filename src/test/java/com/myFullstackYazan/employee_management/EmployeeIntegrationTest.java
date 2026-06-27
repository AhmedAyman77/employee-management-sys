package com.myFullstackYazan.employee_management;

import com.myFullstackYazan.employee_management.dtos.LoginRequest;
import com.myFullstackYazan.employee_management.entities.Department;
import com.myFullstackYazan.employee_management.entities.Employee;
import com.myFullstackYazan.employee_management.entities.UserAccount;
import com.myFullstackYazan.employee_management.repositpories.DepartmentRepo;
import com.myFullstackYazan.employee_management.repositpories.EmployeeRepo;
import com.myFullstackYazan.employee_management.repositpories.UserAccountRepo;
import com.myFullstackYazan.employee_management.services.EmailService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeIntegrationTest {
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine");

  @LocalServerPort
  private Integer port;

  @MockitoBean
  private EmailService emailService;

  @Autowired
  private DepartmentRepo departmentRepo;

  @Autowired
  private EmployeeRepo employeeRepo;

  @Autowired
  private UserAccountRepo userAccountRepo;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private Department department;
  private Employee employeeAdmin;
  private String adminToken;
  private String userToken;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @BeforeEach
  void beforeEach() {
    RestAssured.baseURI = "http://localhost:" + port;
    userAccountRepo.deleteAll();
    employeeRepo.deleteAll();
    departmentRepo.deleteAll();


    department = new Department(null, "IT");
    departmentRepo.save(department);

    employeeAdmin = createEmployee("John", "Doe", "john@gmail.com");
    Employee emp1 = createEmployee("Smith", "DSA", "smith@gmail.com");
    Employee emp2 = createEmployee("Sky", "ASD", "sky@gmail.com");
    Employee emp3 = createEmployee("Jessie", "WER", "jessie@gmail.com");

    employeeRepo.saveAll(List.of(employeeAdmin, emp1, emp2, emp3));
    UserAccount adminAccount = createUserAccount(
        employeeAdmin,
        "admin",
        "password",
        "ADMIN"
    );
    UserAccount userAccount = createUserAccount(
        emp1,
        "user",
        "password",
        "USER"
    );
    userAccountRepo.saveAll(List.of(adminAccount, userAccount));
    adminToken = getToken("admin", "password");
    userToken = getToken("user", "password");
  }

  @Test
  void shouldGetAllEmployeesAsAdmin() {
    given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .get("/employees?page=1&size=100")
        .then()
        .statusCode(200)
        .body("data.content", hasSize(4))
        .body("data.content[0].firstName", notNullValue());
  }

  @Test
  void shouldNotGetAllEmployeesAsUser() {
    given()
        .header("Authorization", "Bearer " + userToken)
        .when()
        .get("/employees?page=1&size=100")
        .then()
        .statusCode(403);
  }

  private Employee createEmployee(String firstName, String lastName, String email) {
    Employee employee = new Employee();
    employee.setFirstName(firstName);
    employee.setLastName(lastName);
    employee.setEmail(email);
    employee.setPhoneNumber("1234567890");
    employee.setHireDate(LocalDate.now());
    employee.setPosition("Developer");
    employee.setVerified(true);
    employee.setDepartment(department);
    return employee;
  }

  private UserAccount createUserAccount(Employee employee, String username,
                                        String password, String role) {
    UserAccount account = new UserAccount();
    account.setUsername(username);
    account.setPassword(passwordEncoder.encode(password));
    account.setEmployee(employee);
    account.setRole(role);
    return account;
  }

  private String getToken(String username, String password) {
    LoginRequest loginRequest = new LoginRequest(username, password);
    return given()
        .contentType("application/json")
        .body(loginRequest)
        .when()
        .post("/auth/login")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getString("data");
  }
}

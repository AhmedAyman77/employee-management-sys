-- ============================================
-- Employee Management System - Database Schema
-- ============================================
-- Generated SQL DDL script for PostgreSQL/MySQL
-- Compatible with the JPA entity definitions
-- ============================================

-- Drop tables in reverse dependency order (if they exist)
DROP TABLE IF EXISTS password_rest_token CASCADE;
DROP TABLE IF EXISTS leave_request CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;
DROP TABLE IF EXISTS employee CASCADE;
DROP TABLE IF EXISTS department CASCADE;

-- ============================================
-- Table: department
-- ============================================
CREATE TABLE department (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================
-- Table: employee
-- ============================================
CREATE TABLE employee (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(25),
    hire_date DATE NOT NULL,
    position VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    account_creation_token VARCHAR(255),
    department_id UUID NOT NULL,
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id)
        REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================
-- Table: user_account
-- ============================================
CREATE TABLE user_account (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    employee_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_user_account_employee FOREIGN KEY (employee_id)
        REFERENCES employee(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================
-- Table: leave_request
-- ============================================
CREATE TABLE leave_request (
    id UUID PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL,
    employee_id UUID NOT NULL,
    CONSTRAINT fk_leave_request_employee FOREIGN KEY (employee_id)
        REFERENCES employee(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================
-- Table: password_rest_token
-- ============================================
CREATE TABLE password_rest_token (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id)
        REFERENCES user_account(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================
-- Indexes for Performance Optimization
-- ============================================

-- Department indexes
CREATE INDEX idx_department_name ON department(name);

-- Employee indexes
CREATE INDEX idx_employee_email ON employee(email);
CREATE INDEX idx_employee_department_id ON employee(department_id);
CREATE INDEX idx_employee_hire_date ON employee(hire_date);
CREATE INDEX idx_employee_is_verified ON employee(is_verified);
CREATE INDEX idx_employee_account_token ON employee(account_creation_token);

-- User account indexes
CREATE INDEX idx_user_account_username ON user_account(username);
CREATE INDEX idx_user_account_employee_id ON user_account(employee_id);

-- Leave request indexes
CREATE INDEX idx_leave_request_employee_id ON leave_request(employee_id);
CREATE INDEX idx_leave_request_status ON leave_request(status);
CREATE INDEX idx_leave_request_dates ON leave_request(start_date, end_date);

-- Password reset token indexes
CREATE INDEX idx_password_reset_token ON password_rest_token(token);
CREATE INDEX idx_password_reset_expiry ON password_rest_token(expiry_date);
-- ============================================

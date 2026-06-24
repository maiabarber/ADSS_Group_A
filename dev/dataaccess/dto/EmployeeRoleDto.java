package dataaccess.dto;

import employee.domain.Role;

public final class EmployeeRoleDto {
    private final String employeeId;
    private final Role role;

    public EmployeeRoleDto(String employeeId, Role role) {
        this.employeeId = employeeId;
        this.role = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public Role getRole() {
        return role;
    }
}
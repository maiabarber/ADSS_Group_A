package dataaccess.mapper;


import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;

import dataaccess.dto.EmployeeDto;
import employee.domain.Employee;
import employee.domain.EmploymentType;

public class EmployeeMapper {

    public static EmployeeDto toDto(Employee employee) {
        if (employee == null) return null;
        
        return new EmployeeDto(
            employee.getId(),
            employee.getPassword(),
            BankAccountMapper.toDto(employee.getBankAccount()),
            employee.getName(),
            SalaryMapper.toDto(employee.getSalary()),
            employee.getEmploymentType(),
            EmploymentTermsMapper.toDto(employee.getEmploymentTerms()),
            employee.getRoles(),
            employee.canManageShift(),
            employee.isFired(),
            employee.getFixedDayOff(),
            WeeklyAvailabilityRequestMapper.toDto(employee.getWeeklyAvailabilityRequest()),
            BranchMapper.toDto(employee.getBranch())
        );
    }

    public static Employee toDomain(EmployeeDto dto) {
        if (dto == null) return null;
        
        return new Employee(
            dto.getEmployeeId(),
            dto.getPassword(),
            BankAccountMapper.toDomain(dto.getBankAccount()),
            dto.getName(),
            SalaryMapper.toDomain(dto.getSalary()),
            dto.getEmploymentType(),
            EmploymentTermsMapper.toDomain(dto.getEmploymentTerms()),
            dto.getRoles(),
            dto.canManageShift(),
            dto.isFired(),
            dto.getFixedDayOff(),
            WeeklyAvailabilityRequestMapper.toDomain(dto.getWeeklyAvailabilityRequest()),
            BranchMapper.toDomain(dto.getBranch())
        );
    }

}
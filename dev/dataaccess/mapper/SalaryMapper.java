package dataaccess.mapper;

import java.sql.ResultSet;

import dataaccess.dto.SalaryDto;
import employee.domain.Salary;

public class SalaryMapper {

    public static Salary toDomain(SalaryDto salary) {
        if (salary == null) return null;
        
        return new Salary(
            salary.getGlobalSalary(),
            salary.getHourlySalary(),
            salary.getWorkedHours(),
            salary.getOvertimeHours(),
            salary.getFinalSalary(),
            salary.getEmploymentScope()
        );
    }

    public static SalaryDto toDto(Salary salary) {
        if (salary == null) return null;
        
        return new SalaryDto(
            salary.getGlobalSalary(),
            salary.getHourlySalary(),
            salary.getWorkedHours(),
            salary.getOvertimeHours(),
            salary.getFinalSalary(),
            salary.getEmploymentScope()
        );
    }

    public static Salary mapResultSetToSalary(ResultSet rs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapResultSetToSalary'");
    }

}


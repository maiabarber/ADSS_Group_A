package dataaccess.mapper;

import java.sql.ResultSet;

import dataaccess.dto.EmploymentTermsDto;
import employee.domain.EmploymentTerms;

public class EmploymentTermsMapper {
    public static EmploymentTerms toDomain(EmploymentTermsDto employmentTerms) {
        return new EmploymentTerms(
            employmentTerms.getStartDate(),
            employmentTerms.getEmploymentScope(),
            employmentTerms.getGlobalSalary(),
            employmentTerms.getHourlySalary(),
            employmentTerms.getVacationDays()
        );
    }

    public static EmploymentTermsDto toDto(EmploymentTerms employmentTerms) {
        return new EmploymentTermsDto(
            employmentTerms.getStartDate(),
            employmentTerms.getEmploymentScope(),
            employmentTerms.getGlobalSalary(),
            employmentTerms.getHourlySalary(),
            employmentTerms.getVacationDays()
        );
    }

    public static EmploymentTerms mapResultSetToEmploymentTerms(ResultSet rs) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mapResultSetToEmploymentTerms'");
    }

}

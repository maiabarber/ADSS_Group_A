package dataaccess.dto;

import employee.domain.EmploymentScope;

import java.time.LocalDate;

public final class EmploymentTermsDto {
    private final LocalDate startDate;
    private final EmploymentScope employmentScope;
    private final double globalSalary;
    private final double hourlySalary;
    private final int vacationDays;

    public EmploymentTermsDto(
            LocalDate startDate,
            EmploymentScope employmentScope,
            double globalSalary,
            double hourlySalary,
            int vacationDays) {
        this.startDate = startDate;
        this.employmentScope = employmentScope;
        this.globalSalary = globalSalary;
        this.hourlySalary = hourlySalary;
        this.vacationDays = vacationDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public EmploymentScope getEmploymentScope() {
        return employmentScope;
    }

    public double getGlobalSalary() {
        return globalSalary;
    }

    public double getHourlySalary() {
        return hourlySalary;
    }

    public int getVacationDays() {
        return vacationDays;
    }
}
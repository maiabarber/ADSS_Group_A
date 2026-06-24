package dataaccess.dto;

import employee.domain.EmploymentScope;

public final class SalaryDto {
    private final double globalSalary;
    private final double hourlySalary;
    private final double workedHours;
    private final EmploymentScope employmentScope;

    public SalaryDto(double globalSalary, double hourlySalary, double workedHours, EmploymentScope employmentScope) {
        this.globalSalary = globalSalary;
        this.hourlySalary = hourlySalary;
        this.workedHours = workedHours;
        this.employmentScope = employmentScope;
    }

    public double getGlobalSalary() {
        return globalSalary;
    }

    public double getHourlySalary() {
        return hourlySalary;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public EmploymentScope getEmploymentScope() {
        return employmentScope;
    }
}
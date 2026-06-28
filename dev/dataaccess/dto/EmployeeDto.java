package dataaccess.dto;

public final class EmployeeDto {
    private final String employeeId;
    private final String name;
    private final String bankNumber;
    private final String bankBranchNumber;
    private final String bankAccountNumber;
    private final String employmentType;
    private final String employmentScope;
    private final double hourlySalary;
    private final double globalSalary;
    private final String startDate;
    private final boolean fired;
    private final int vacationDays;
    private final Integer branchId;

    public EmployeeDto(String employeeId, String name, String bankNumber, String bankBranchNumber, String bankAccountNumber, String employmentType, String employmentScope, double hourlySalary, double globalSalary, String startDate, boolean fired, int vacationDays, Integer branchId) {
        this.employeeId = employeeId;
        this.name = name;
        this.bankNumber = bankNumber;
        this.bankBranchNumber = bankBranchNumber;
        this.bankAccountNumber = bankAccountNumber;
        this.employmentType = employmentType;
        this.employmentScope = employmentScope;
        this.hourlySalary = hourlySalary;
        this.globalSalary = globalSalary;
        this.startDate = startDate;
        this.fired = fired;
        this.vacationDays = vacationDays;
        this.branchId = branchId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public String getBankBranchNumber() {
        return bankBranchNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public String getEmploymentScope() {
        return employmentScope;
    }

    public double getHourlySalary() {
        return hourlySalary;
    }

    public double getGlobalSalary() {
        return globalSalary;
    }

    public String getStartDate() {
        return startDate;
    }

    public boolean isFired() {
        return fired;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    public Integer getBranchId() {
        return branchId;
    }

}
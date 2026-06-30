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
    private final boolean canManageShift;
    private final String fixedDayOff;
    private final String weeklyWeekStartDate;
    private final String weeklySubmissionDeadline;
    private final String weeklyConstraints;
    private final String weeklyPreferences;

    public EmployeeDto(String employeeId, String name, String bankNumber, String bankBranchNumber, String bankAccountNumber, String employmentType, String employmentScope, double hourlySalary, double globalSalary, String startDate, boolean fired, int vacationDays, Integer branchId, boolean canManageShift) {
        this(employeeId, name, bankNumber, bankBranchNumber, bankAccountNumber, employmentType, employmentScope,
                hourlySalary, globalSalary, startDate, fired, vacationDays, branchId, canManageShift,
                null, null, null, null, null);
    }

    public EmployeeDto(String employeeId, String name, String bankNumber, String bankBranchNumber, String bankAccountNumber, String employmentType, String employmentScope, double hourlySalary, double globalSalary, String startDate, boolean fired, int vacationDays, Integer branchId, boolean canManageShift, String fixedDayOff, String weeklyWeekStartDate, String weeklySubmissionDeadline, String weeklyConstraints, String weeklyPreferences) {
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
        this.canManageShift = canManageShift;
        this.fixedDayOff = fixedDayOff;
        this.weeklyWeekStartDate = weeklyWeekStartDate;
        this.weeklySubmissionDeadline = weeklySubmissionDeadline;
        this.weeklyConstraints = weeklyConstraints;
        this.weeklyPreferences = weeklyPreferences;
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

    public boolean canManageShift() {
        return canManageShift;
    }

    public String getFixedDayOff() {
        return fixedDayOff;
    }

    public String getWeeklyWeekStartDate() {
        return weeklyWeekStartDate;
    }

    public String getWeeklySubmissionDeadline() {
        return weeklySubmissionDeadline;
    }

    public String getWeeklyConstraints() {
        return weeklyConstraints;
    }

    public String getWeeklyPreferences() {
        return weeklyPreferences;
    }

}

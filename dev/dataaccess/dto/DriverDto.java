package dataaccess.dto;

public final class DriverDto {
    private final String employeeId;
    private final String driverName;

    public DriverDto(String employeeId, String driverName) {
        this.employeeId = employeeId;
        this.driverName = driverName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getDriverName() {
        return driverName;
    }

}
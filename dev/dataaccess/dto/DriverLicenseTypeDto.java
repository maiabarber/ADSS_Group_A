package dataaccess.dto;

public final class DriverLicenseTypeDto {
    private final String employeeId;
    private final String licenseType;

    public DriverLicenseTypeDto(String employeeId, String licenseType) {
        this.employeeId = employeeId;
        this.licenseType = licenseType;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getLicenseType() {
        return licenseType;
    }

}
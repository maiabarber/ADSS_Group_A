package dataaccess.dto;

import transportation.domain.LicenseType;

public final class DriverLicenseTypeDto {
    private final String employeeId;
    private final LicenseType licenseType;

    public DriverLicenseTypeDto(String employeeId, LicenseType licenseType) {
        this.employeeId = employeeId;
        this.licenseType = licenseType;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }
}
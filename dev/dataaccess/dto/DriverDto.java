package dataaccess.dto;

import transportation.domain.LicenseType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DriverDto {
    private final String employeeId;
    private final String driverName;
    private final Set<LicenseType> licenseTypes;

    public DriverDto(String employeeId, String driverName, Set<LicenseType> licenseTypes) {
        this.employeeId = employeeId;
        this.driverName = driverName;
        this.licenseTypes = licenseTypes == null ? new HashSet<>() : new HashSet<>(licenseTypes);
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getDriverName() {
        return driverName;
    }

    public Set<LicenseType> getLicenseTypes() {
        return Collections.unmodifiableSet(licenseTypes);
    }
}
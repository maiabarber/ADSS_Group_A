package domain;

import java.util.HashSet;
import java.util.Set;

public class Driver {

    private String driverName;
    private Set<LicenseType> licenseTypes;

    public Driver(String driverName, Set<LicenseType> licenseTypes) {
        validateDriverName(driverName);

        if (licenseTypes == null) {
            throw new IllegalArgumentException("licenseTypes cannot be null");
        }
        if (licenseTypes.isEmpty()) {
            throw new IllegalArgumentException("licenseTypes cannot be empty");
        }

        this.driverName = driverName;
        this.licenseTypes = new HashSet<>(licenseTypes);
    }

    public String getDriverName() {
        return driverName;
    }

    public Set<LicenseType> getLicenseTypes() {
        return new HashSet<>(licenseTypes);
    }

    public boolean hasLicenseType(LicenseType licenseType) {
        if (licenseType == null) {
            throw new IllegalArgumentException("licenseType cannot be null");
        }
        return licenseTypes.contains(licenseType);
    }

    public boolean canDrive(Truck truck) {
        if (truck == null) {
            throw new IllegalArgumentException("truck cannot be null");
        }
        return hasLicenseType(truck.getRequiredLicenseType());
    }

    public void addLicenseType(LicenseType licenseType) {
        if (licenseType == null) {
            throw new IllegalArgumentException("licenseType cannot be null");
        }
        licenseTypes.add(licenseType);
    }

    public void removeLicenseType(LicenseType licenseType) {
        if (licenseType == null) {
            throw new IllegalArgumentException("licenseType cannot be null");
        }
        if (!licenseTypes.contains(licenseType)) {
            throw new IllegalArgumentException("driver does not have this license type");
        }
        if (licenseTypes.size() == 1) {
            throw new IllegalStateException("driver must have at least one license type");
        }
        licenseTypes.remove(licenseType);
    }

    private void validateDriverName(String driverName) {
        if (driverName == null || driverName.isBlank()) {
            throw new IllegalArgumentException("driverName cannot be empty");
        }
    }

    @Override
    public String toString() {
        return "Driver{" +
                "driverName='" + driverName + '\'' +
                ", licenseTypes=" + licenseTypes +
                '}';
    }
}
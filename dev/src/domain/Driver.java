package domain;

import java.util.HashSet;
import java.util.Set;

public class Driver{
    private String driverName;
    private Set<LicenseType> licenseTypes;

    public Driver(String driverName, Set<LicenseType> licenseTypes){
        if (driverName == null || driverName.isBlank()) {
            throw new IllegalArgumentException("driverName cannot be empty");
        }
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
        return new HashSet<>(licenseTypes); // copy and not original
    }

    public boolean hasLicenseType(LicenseType licenseType) {
        return licenseTypes.contains(licenseType);
    }



}

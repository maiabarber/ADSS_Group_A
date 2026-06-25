package dataaccess.dto;

import transportation.domain.LicenseType;

public class TruckDto {
    private final String licenseNumber;
    private final String model;
    private final double netWeight;
    private final double maxAllowedWeight;
    private final LicenseType requiredLicenseType;

    public TruckDto(
            String licenseNumber,
            String model,
            double netWeight,
            double maxAllowedWeight,
            LicenseType requiredLicenseType) {
        this.licenseNumber = licenseNumber;
        this.model = model;
        this.netWeight = netWeight;
        this.maxAllowedWeight = maxAllowedWeight;
        this.requiredLicenseType = requiredLicenseType;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getModel() {
        return model;
    }

    public double getNetWeight() {
        return netWeight;
    }

    public double getMaxAllowedWeight() {
        return maxAllowedWeight;
    }

    public LicenseType getRequiredLicenseType() {
        return requiredLicenseType;
    }
}
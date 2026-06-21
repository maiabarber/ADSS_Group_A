package domain;

public class Truck {

    private String licenseNumber;
    private String model;
    private double netWeight;
    private double maxAllowedWeight;
    private LicenseType requiredLicenseType;

    public Truck(String licenseNumber, String model, double netWeight, double maxAllowedWeight,
                 LicenseType requiredLicenseType) {
        validateLicenseNumber(licenseNumber);
        validateModel(model);
        validateWeights(netWeight, maxAllowedWeight);

        if (requiredLicenseType == null) {
            throw new IllegalArgumentException("requiredLicenseType cannot be null");
        }

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

    public boolean canCarryWeight(double actualWeight) {
        if (actualWeight < 0) {
            throw new IllegalArgumentException("actualWeight cannot be negative");
        }
        return actualWeight <= maxAllowedWeight;
    }

    private void validateLicenseNumber(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            throw new IllegalArgumentException("licenseNumber cannot be empty");
        }
    }

    private void validateModel(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model cannot be empty");
        }
    }

    private void validateWeights(double netWeight, double maxAllowedWeight) {
        if (netWeight < 0 || maxAllowedWeight < 0) {
            throw new IllegalArgumentException("weights cannot be negative");
        }
        if (maxAllowedWeight < netWeight) {
            throw new IllegalArgumentException("maxAllowedWeight cannot be smaller than netWeight");
        }
    }

    @Override
    public String toString() {
        return "Truck{" +
                "licenseNumber='" + licenseNumber + '\'' +
                ", model='" + model + '\'' +
                ", netWeight=" + netWeight +
                ", maxAllowedWeight=" + maxAllowedWeight +
                ", requiredLicenseType=" + requiredLicenseType +
                '}';
    }
}
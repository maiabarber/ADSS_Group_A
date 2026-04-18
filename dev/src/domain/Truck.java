package domain;

public class Truck {
    
    private String licenseNumber;
    private String model;
    private double netWeight;
    private double maxAllowedWeight;
    private LicenseType requiredLicenseType;

    public Truck(String licenseNumber, String model, double netWeight, double maxAllowedWeight, LicenseType requiredLicenseType){
        if (licenseNumber == null || licenseNumber.isBlank()){
            throw new IllegalArgumentException("licenseNumber cannot be empty");
        }
        if (model == null || model.isBlank()){
            throw new IllegalArgumentException("model cannot be empty");
        }
        if (netWeight < 0 || maxAllowedWeight < 0){
            throw new IllegalArgumentException("weights cannot be negative");
        }
        if (maxAllowedWeight < netWeight){
            throw new IllegalArgumentException("maxAllowedWeight cannot be smaller than netWeight");
        }
        if (requiredLicenseType == null){
            throw new IllegalArgumentException("requiredLicenseType cannot be null");
        }

        this.licenseNumber = licenseNumber;
        this.model = model;
        this.netWeight = netWeight;
        this.maxAllowedWeight = maxAllowedWeight;
        this.requiredLicenseType = requiredLicenseType;
    }

    public String getLicenseNumber(){
        return this.licenseNumber;
    }

    public String getModel(){
        return this.model;
    }

    public double getNetWeight(){
        return this.netWeight;
    }

    public double getMaxAllowedWeight(){
        return this.maxAllowedWeight;
    }

    public LicenseType getRequiredLicenseType(){
        return this.requiredLicenseType;
    }

}

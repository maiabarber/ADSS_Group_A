package domain;

import java.util.ArrayList;
import java.util.List;

public class DeliveryForm {

    private List<Double> weightMeasurements;

    public DeliveryForm() {
        this.weightMeasurements = new ArrayList<>();
    }

    public DeliveryForm(List<Double> weightMeasurements) {
        if (weightMeasurements == null) {
            throw new IllegalArgumentException("weightMeasurements cannot be null");
        }

        this.weightMeasurements = new ArrayList<>();
        for (Double measurement : weightMeasurements) {
            validateMeasurement(measurement);
            this.weightMeasurements.add(measurement);
        }
    }

    public List<Double> getWeightMeasurements() {
        return new ArrayList<>(weightMeasurements);
    }

    public void addWeightMeasurement(double weight) {
        validateMeasurement(weight);
        weightMeasurements.add(weight);
    }

    public double getLatestWeightMeasurement() {
        if (weightMeasurements.isEmpty()) {
            throw new IllegalStateException("there are no weight measurements");
        }
        return weightMeasurements.get(weightMeasurements.size() - 1);
    }

    public boolean hasMeasurements() {
        return !weightMeasurements.isEmpty();
    }

    private void validateMeasurement(Double measurement) {
        if (measurement == null) {
            throw new IllegalArgumentException("weight measurement cannot be null");
        }
        if (measurement < 0) {
            throw new IllegalArgumentException("weight measurement cannot be negative");
        }
    }
}
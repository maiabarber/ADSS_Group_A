package dataaccess.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DeliveryFormDto {
    private final List<Double> weightMeasurements;

    public DeliveryFormDto(List<Double> weightMeasurements) {
        this.weightMeasurements = weightMeasurements == null ? new ArrayList<>() : new ArrayList<>(weightMeasurements);
    }

    public List<Double> getWeightMeasurements() {
        return Collections.unmodifiableList(weightMeasurements);
    }
}
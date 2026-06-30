package dataaccess.dto;

public final class DeliveryFormMeasurementDto {
    private final int measurementId;
    private final int deliveryId;
    private final double measuredWeight;

    public DeliveryFormMeasurementDto(int measurementId, int deliveryId, double measuredWeight) {
        this.measurementId = measurementId;
        this.deliveryId = deliveryId;
        this.measuredWeight = measuredWeight;
    }

    public int getMeasurementId() {
        return measurementId;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public double getMeasuredWeight() {
        return measuredWeight;
    }

}
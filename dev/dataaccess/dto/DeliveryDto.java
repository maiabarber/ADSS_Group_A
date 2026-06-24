package dataaccess.dto;

import transportation.domain.DeliveryStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DeliveryDto {
    private final int deliveryId;
    private final LocalDate deliveryDate;
    private final SiteDto source;
    private final List<DeliveryStopDto> stops;
    private final LocalTime departureTime;
    private final double finalMeasuredWeightBeforeDeparture;
    private final TruckDto truck;
    private final DriverDto driver;
    private final ShippingZoneDto shippingZone;
    private final DeliveryStatus status;
    private final DeliveryFormDto deliveryForm;

    public DeliveryDto(
            int deliveryId,
            LocalDate deliveryDate,
            SiteDto source,
            List<DeliveryStopDto> stops,
            LocalTime departureTime,
            double finalMeasuredWeightBeforeDeparture,
            TruckDto truck,
            DriverDto driver,
            ShippingZoneDto shippingZone,
            DeliveryStatus status,
            DeliveryFormDto deliveryForm) {
        this.deliveryId = deliveryId;
        this.deliveryDate = deliveryDate;
        this.source = source;
        this.stops = stops == null ? new ArrayList<>() : new ArrayList<>(stops);
        this.departureTime = departureTime;
        this.finalMeasuredWeightBeforeDeparture = finalMeasuredWeightBeforeDeparture;
        this.truck = truck;
        this.driver = driver;
        this.shippingZone = shippingZone;
        this.status = status;
        this.deliveryForm = deliveryForm;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public SiteDto getSource() {
        return source;
    }

    public List<DeliveryStopDto> getStops() {
        return Collections.unmodifiableList(stops);
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public double getFinalMeasuredWeightBeforeDeparture() {
        return finalMeasuredWeightBeforeDeparture;
    }

    public TruckDto getTruck() {
        return truck;
    }

    public DriverDto getDriver() {
        return driver;
    }

    public ShippingZoneDto getShippingZone() {
        return shippingZone;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public DeliveryFormDto getDeliveryForm() {
        return deliveryForm;
    }
}
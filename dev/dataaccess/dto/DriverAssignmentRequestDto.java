package dataaccess.dto;

import employee.domain.ShiftType;

import java.time.LocalDateTime;

public final class DriverAssignmentRequestDto {
    private final String driverId;
    private final int deliveryId;
    private final LocalDateTime deliveryDateTime;
    private final ShiftType shiftType;
    private final boolean handled;
    private final String statusMessage;

    public DriverAssignmentRequestDto(
            String driverId,
            int deliveryId,
            LocalDateTime deliveryDateTime,
            ShiftType shiftType,
            boolean handled,
            String statusMessage) {
        this.driverId = driverId;
        this.deliveryId = deliveryId;
        this.deliveryDateTime = deliveryDateTime;
        this.shiftType = shiftType;
        this.handled = handled;
        this.statusMessage = statusMessage;
    }

    public String getDriverId() {
        return driverId;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public LocalDateTime getDeliveryDateTime() {
        return deliveryDateTime;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public boolean isHandled() {
        return handled;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
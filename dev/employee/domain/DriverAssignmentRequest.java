package employee.domain;

import java.time.LocalDateTime;

public class DriverAssignmentRequest {
    private final String driverId;
    private final int deliveryId;
    private final LocalDateTime deliveryDateTime;
    private final ShiftType shiftType;
    private boolean handled;
    private String statusMessage;

    public DriverAssignmentRequest(
            String driverId,
            int deliveryId,
            LocalDateTime deliveryDateTime,
            ShiftType shiftType) {
        this.driverId = driverId;
        this.deliveryId = deliveryId;
        this.deliveryDateTime = deliveryDateTime;
        this.shiftType = shiftType;
        this.handled = false;
        this.statusMessage = "Waiting for HR manager to assign driver to shift";
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

    public String getStatusMessage() {
    return statusMessage;
}

public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
}

    public boolean isHandled() {
        return handled;
    }

    public void markHandled() {
    this.handled = true;
    this.statusMessage = "Driver was assigned to the required shift";
}
    
}
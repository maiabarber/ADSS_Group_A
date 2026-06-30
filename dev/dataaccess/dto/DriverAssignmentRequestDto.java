package dataaccess.dto;

public final class DriverAssignmentRequestDto {
    private final int requestId;
    private final String driverId;
    private final int deliveryId;
    private final String deliveryDateTime;
    private final String shiftType;
    private final boolean handled;
    private final String statusMessage;

    public DriverAssignmentRequestDto(int requestId, String driverId, int deliveryId, String deliveryDateTime, String shiftType, boolean handled, String statusMessage) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.deliveryId = deliveryId;
        this.deliveryDateTime = deliveryDateTime;
        this.shiftType = shiftType;
        this.handled = handled;
        this.statusMessage = statusMessage;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getDriverId() {
        return driverId;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public String getDeliveryDateTime() {
        return deliveryDateTime;
    }

    public String getShiftType() {
        return shiftType;
    }

    public boolean isHandled() {
        return handled;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}
package dataaccess.dto;

public final class DeliveryDocumentDto {
    private final int documentNumber;
    private final int stopId;

    public DeliveryDocumentDto(int documentNumber, int stopId) {
        this.documentNumber = documentNumber;
        this.stopId = stopId;
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public int getStopId() {
        return stopId;
    }

}
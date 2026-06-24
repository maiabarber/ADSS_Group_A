package dataaccess.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DeliveryDocumentDto {
    private final int documentNumber;
    private final List<DeliveryItemDto> items;

    public DeliveryDocumentDto(int documentNumber, List<DeliveryItemDto> items) {
        this.documentNumber = documentNumber;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public List<DeliveryItemDto> getItems() {
        return Collections.unmodifiableList(items);
    }
}
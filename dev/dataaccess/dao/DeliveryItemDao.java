package dataaccess.dao;

import dataaccess.dto.DeliveryDocumentDto;
import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DeliveryItemDto;
import dataaccess.dto.DeliveryStopDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeliveryItemDao {
    private final TransportationDataAccess transportationDataAccess = new TransportationDataAccess();

    public List<DeliveryItemDto> findAll() throws SQLException {
        List<DeliveryItemDto> items = new ArrayList<>();
        for (DeliveryDto delivery : transportationDataAccess.listDeliveries()) {
            for (DeliveryStopDto stop : delivery.getStops()) {
                DeliveryDocumentDto document = stop.getDocument();
                if (document != null) {
                    items.addAll(document.getItems());
                }
            }
        }
        return items;
    }
}
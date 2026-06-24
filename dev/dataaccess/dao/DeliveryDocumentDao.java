package dataaccess.dao;

import dataaccess.dto.DeliveryDocumentDto;
import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DeliveryStopDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeliveryDocumentDao {
    private final TransportationDataAccess transportationDataAccess = new TransportationDataAccess();

    public List<DeliveryDocumentDto> findAll() throws SQLException {
        Map<Integer, DeliveryDocumentDto> documents = new LinkedHashMap<>();
        for (DeliveryDto delivery : transportationDataAccess.listDeliveries()) {
            for (DeliveryStopDto stop : delivery.getStops()) {
                if (stop.getDocument() != null) {
                    documents.putIfAbsent(stop.getDocument().getDocumentNumber(), stop.getDocument());
                }
            }
        }
        return new ArrayList<>(documents.values());
    }
}
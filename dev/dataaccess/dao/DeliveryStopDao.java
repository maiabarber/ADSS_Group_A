package dataaccess.dao;

import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DeliveryStopDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeliveryStopDao {
    private final TransportationDataAccess transportationDataAccess = new TransportationDataAccess();

    public List<DeliveryStopDto> findAll() throws SQLException {
        List<DeliveryStopDto> stops = new ArrayList<>();
        for (DeliveryDto delivery : transportationDataAccess.listDeliveries()) {
            stops.addAll(delivery.getStops());
        }
        return stops;
    }
}
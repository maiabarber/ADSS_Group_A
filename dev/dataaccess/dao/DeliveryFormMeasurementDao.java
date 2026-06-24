package dataaccess.dao;

import dataaccess.dto.DeliveryFormMeasurementDto;

import java.sql.SQLException;
import java.util.List;

public class DeliveryFormMeasurementDao {
    private final TransportationDataAccess transportationDataAccess = new TransportationDataAccess();

    public List<DeliveryFormMeasurementDto> findAll() throws SQLException {
        return transportationDataAccess.listDeliveryMeasurements();
    }
}
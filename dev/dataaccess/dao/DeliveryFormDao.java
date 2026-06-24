package dataaccess.dao;

import dataaccess.dto.DeliveryDto;
import dataaccess.dto.DeliveryFormDto;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeliveryFormDao {
    private final TransportationDataAccess transportationDataAccess = new TransportationDataAccess();

    public List<DeliveryFormDto> findAll() throws SQLException {
        return transportationDataAccess.listDeliveries().stream()
                .map(DeliveryDto::getDeliveryForm)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
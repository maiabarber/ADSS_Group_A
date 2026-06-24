package dataaccess.dao;

import dataaccess.dto.DriverLicenseTypeDto;
import transportation.domain.LicenseType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DriverLicenseTypeDao {
    private final TransportationDataAccess transportationDataAccess = new TransportationDataAccess();

    public List<DriverLicenseTypeDto> findAll() throws SQLException {
        List<DriverLicenseTypeDto> licenseTypes = new ArrayList<>();
        transportationDataAccess.listDrivers().forEach(driver -> {
            for (LicenseType licenseType : driver.getLicenseTypes()) {
                licenseTypes.add(new DriverLicenseTypeDto(driver.getEmployeeId(), licenseType));
            }
        });
        return licenseTypes;
    }
}
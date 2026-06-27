package dataaccess.mapper;

import dataaccess.dto.BranchDto;
import employee.domain.Branch;

public class BranchMapper {

    public static BranchDto toDto(Branch branch) {
        if (branch == null) return null;
        
        return new BranchDto(
            branch.getBranchId(),
            branch.getBranchName(),
            branch.getLocation(),
            null // כאן תבצע מיפוי של ה-DeliveryStop אם קיים
        );
    }

    public static Branch toDomain(BranchDto dto) {
        if (dto == null) return null;
        
        return new Branch(
            dto.getBranchId(),
            dto.getBranchName(),
            dto.getLocation()
        );
    }
}
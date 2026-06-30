package dataaccess.mapper;

import dataaccess.dto.BranchDto;
import employee.domain.Branch;

public final class BranchMapper {
    private BranchMapper() {}

    public static BranchDto toDto(Branch branch) {
        if (branch == null) {
            return null;
        }
        return new BranchDto(
                parseBranchId(branch.getBranchId()),
                branch.getBranchName(),
                branch.getLocation()
        );
    }

    public static Branch toDomain(BranchDto dto) {
        if (dto == null) {
            return null;
        }
        return new Branch(
                String.valueOf(dto.getBranchId()),
                dto.getBranchName(),
                dto.getAddress()
        );
    }

    private static int parseBranchId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (Exception e) {
            return 0;
        }
    }
}

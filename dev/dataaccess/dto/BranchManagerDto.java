package dataaccess.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BranchManagerDto {
    private final List<BranchDto> branches;

    public BranchManagerDto(List<BranchDto> branches) {
        this.branches = branches == null ? new ArrayList<>() : new ArrayList<>(branches);
    }

    public List<BranchDto> getBranches() {
        return Collections.unmodifiableList(branches);
    }
}
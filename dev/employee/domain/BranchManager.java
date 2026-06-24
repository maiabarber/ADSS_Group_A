package employee.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BranchManager class manages all branches in the system.
 * The HR Manager can use this to create, retrieve, and manage branches.
 */
public class BranchManager {
    private Map<String, Branch> branches = new HashMap<>();

    public void addBranch(Branch branch) {
        if (branch == null) {
            throw new IllegalArgumentException("Branch cannot be null");
        }
        if (branches.containsKey(branch.getBranchId())) {
            throw new IllegalArgumentException("Branch with ID " + branch.getBranchId() + " already exists");
        }
        branches.put(branch.getBranchId(), branch);
    }

    public Branch getBranchById(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch ID cannot be empty");
        }
        return branches.get(branchId);
    }

    public List<Branch> getAllBranches() {
        return new ArrayList<>(branches.values());
    }

    public void removeBranch(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch ID cannot be empty");
        }
        if (!branches.containsKey(branchId)) {
            throw new IllegalArgumentException("Branch with ID " + branchId + " does not exist");
        }
        branches.remove(branchId);
    }

    public boolean branchExists(String branchId) {
        return branches.containsKey(branchId);
    }

    public int getBranchCount() {
        return branches.size();
    }
}

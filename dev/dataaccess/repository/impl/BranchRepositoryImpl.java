package dataaccess.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import dataaccess.mapper.BranchMapper;
import dataaccess.dao.BranchDAOImpl;
import dataaccess.dto.BranchDto;
import dataaccess.repository.BranchRepository;
import dataaccess.repository.RepositoryException;
import employee.domain.Branch;

public class BranchRepositoryImpl implements BranchRepository {
    private final BranchDAOImpl dao;
    private final Map<String, Branch> identityMap = new ConcurrentHashMap<>();

    public BranchRepositoryImpl(BranchDAOImpl dao) {
        this.dao = dao;
    }

    @Override
    public BranchDto save(BranchDto branchDto) throws RepositoryException {
        try {
            dao.createOrUpdate(branchDto);
            identityMap.put(branchDto.getBranchId(), BranchMapper.toDomain(branchDto));
            return branchDto;
        } catch (Exception e) {
            throw new RepositoryException("Error saving branch", e);
        }
    }

    @Override
    public Optional<BranchDto> findById(String id) throws RepositoryException {
        if (identityMap.containsKey(id)) {
            return Optional.of(BranchMapper.toDto(identityMap.get(id)));
        }
        try {
            BranchDto branchDto = dao.findbyId(id);
            if (branchDto != null) {
                identityMap.put(id, BranchMapper.toDomain(branchDto));
                return Optional.of(branchDto);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RepositoryException("Error finding branch by ID", e);
        }
    }

    @Override
    public List<BranchDto> findAll() throws RepositoryException {
        try {
            List<BranchDto> branchDtos = dao.findAll();
            for (BranchDto branchDto : branchDtos) {
                identityMap.put(branchDto.getBranchId(), BranchMapper.toDomain(branchDto));
            }
            return branchDtos;
        } catch (Exception e) {
            throw new RepositoryException("Error finding all branches", e);
        }
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        try {
            dao.delete(id);
            identityMap.remove(id);
        } catch (Exception e) {
            throw new RepositoryException("Error deleting branch by ID", e);
        }
    }

}
package employees.repository.impl;

import employees.domain.Shift;
import employees.repository.RepositoryException;
import employees.repository.ShiftRepository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileShiftRepository implements ShiftRepository {
    private final Path filePath;

    public FileShiftRepository(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Shift save(Shift shift) throws RepositoryException {
        if (shift == null) {
            throw new RepositoryException("Shift cannot be null");
        }
        if (shift.getDate() == null || shift.getShiftType() == null) {
            throw new RepositoryException("Shift date and shift type cannot be null");
        }

        String id = buildShiftId(shift);
        List<Shift> shifts = loadShifts();
        shifts = shifts.stream()
            .filter(existing -> !buildShiftId(existing).equals(id))
            .collect(Collectors.toCollection(ArrayList::new));
        shifts.add(shift);
        writeShifts(shifts);
        return shift;
    }

    @Override
    public Optional<Shift> findById(String id) throws RepositoryException {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return loadShifts().stream()
            .filter(shift -> buildShiftId(shift).equals(id))
            .findFirst();
    }

    @Override
    public List<Shift> findAll() throws RepositoryException {
        return new ArrayList<>(loadShifts());
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null || id.isBlank()) {
            return;
        }

        List<Shift> shifts = loadShifts();
        List<Shift> filtered = shifts.stream()
            .filter(shift -> !buildShiftId(shift).equals(id))
            .collect(Collectors.toCollection(ArrayList::new));

        if (filtered.size() != shifts.size()) {
            writeShifts(filtered);
        }
    }

    private List<Shift> loadShifts() throws RepositoryException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {
            Object data = in.readObject();
            if (data instanceof List) {
                @SuppressWarnings("unchecked")
                List<Shift> shifts = (List<Shift>) data;
                return new ArrayList<>(shifts);
            }
            throw new RepositoryException("Shift repository file contains invalid data");
        } catch (IOException | ClassNotFoundException e) {
            throw new RepositoryException("Failed to load shifts", e);
        }
    }

    private void writeShifts(List<Shift> shifts) throws RepositoryException {
        try {
            Files.createDirectories(filePath.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(filePath)))) {
                out.writeObject(shifts);
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to save shifts", e);
        }
    }

    private static String buildShiftId(Shift shift) {
        return shift.getDate() + ":" + shift.getShiftType();
    }
}

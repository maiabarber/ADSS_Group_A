package employees.repository;

import java.time.LocalDate;
import java.util.Optional;

public interface SubmissionDeadlineRepository {
    void save(LocalDate deadline) throws RepositoryException;
    Optional<LocalDate> findCurrent() throws RepositoryException;
}

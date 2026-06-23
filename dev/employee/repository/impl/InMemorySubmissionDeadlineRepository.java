package employee.repository.impl;

import employee.repository.RepositoryException;
import employee.repository.SubmissionDeadlineRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * InMemorySubmissionDeadlineRepository class provides an in-memory implementation of the SubmissionDeadlineRepository interface.
 * It maintains a single LocalDate field to store the current submission deadline and allows for saving and retrieving it.
 */
public class InMemorySubmissionDeadlineRepository implements SubmissionDeadlineRepository {
    private LocalDate currentDeadline;

    @Override
    public void save(LocalDate deadline) throws RepositoryException {
        if (deadline == null) {
            throw new RepositoryException("Submission deadline cannot be null");
        }
        currentDeadline = deadline;
    }

    @Override
    public Optional<LocalDate> findCurrent() throws RepositoryException {
        return Optional.ofNullable(currentDeadline);
    }
}

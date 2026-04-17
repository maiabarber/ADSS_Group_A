package employees.repository.impl;

import employees.repository.RepositoryException;
import employees.repository.SubmissionDeadlineRepository;

import java.time.LocalDate;
import java.util.Optional;

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

package employees.service;

import employees.domain.SubmissionDeadlinePolicy;
import employees.repository.RepositoryException;
import employees.repository.SubmissionDeadlineRepository;

import java.time.LocalDate;

/**
 * SubmissionDeadlineService contains business rules for weekly submission deadlines.
 */
public class SubmissionDeadlineService {
    private final SubmissionDeadlinePolicy deadlinePolicy = new SubmissionDeadlinePolicy();

    public void setWeeklySubmissionDeadline(LocalDate newDeadline, SubmissionDeadlineRepository repository)
        throws RepositoryException {
        deadlinePolicy.validateManagerDeadline(newDeadline, LocalDate.now());

        repository.save(newDeadline);
    }
}

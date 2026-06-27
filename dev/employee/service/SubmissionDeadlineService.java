package employee.service;

import employee.domain.SubmissionDeadlinePolicy;

import java.time.LocalDate;

import dataaccess.repository.RepositoryException;
import dataaccess.repository.SubmissionDeadlineRepository;

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

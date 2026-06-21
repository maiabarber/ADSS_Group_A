package service;

import domain.SubmissionDeadlinePolicy;
import repository.RepositoryException;
import repository.SubmissionDeadlineRepository;

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

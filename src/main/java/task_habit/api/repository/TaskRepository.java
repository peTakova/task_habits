package task_habit.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.TaskStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Optional<TaskEntity> findByIdAndUserId(Long id, Long userId);
    List<TaskEntity> findByUserId(Long userId);

    List<TaskEntity> findByStatus(TaskStatus status);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, TaskStatus status);
    Page<TaskEntity> findByUserId(Long userId, Pageable pageable);
    List<TaskEntity> findByUserIdAndStatus(Long userId, TaskStatus status);
    List<TaskEntity> findAllByDueDateBetweenAndStatusNot(Date start, Date end, TaskStatus taskStatus);

    /**
     * Nastaví len pripomienku – a iba ak úloha medzitým nebola dokončená ani jej termín zmenený.
     * Neprepisuje ostatné stĺpce, takže nepokazí súbežnú úpravu úlohy používateľom.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            UPDATE TaskEntity t SET t.reminderMessage = :message
            WHERE t.id = :id AND t.status <> :completed AND t.dueDate = :dueDate
              AND (t.reminderMessage IS NULL OR t.reminderMessage <> :message)
            """)
    int setReminderIfStillDue(@Param("id") Long id,
                              @Param("dueDate") Date dueDate,
                              @Param("message") String message,
                              @Param("completed") TaskStatus completed);
}

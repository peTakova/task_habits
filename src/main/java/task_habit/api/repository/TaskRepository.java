package task_habit.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
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
    long countByUserIdAndStatus(Long userId, TaskStatus status);
    Page<TaskEntity> findAll(Pageable pageable);

    List<TaskEntity> findAllByDueDateBetweenAndStatusNot(Date start, Date end, TaskStatus taskStatus);
}

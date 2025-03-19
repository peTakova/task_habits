package task_habit.api.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import task_habit.api.model.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}

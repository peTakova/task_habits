package task_habit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import task_habit.api.model.Frequency;
import task_habit.api.model.HabitEntity;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.User;

import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<HabitEntity, Long> {
    Optional<HabitEntity> findByIdAndUserId(Long taskId, Long userId);
    List<HabitEntity> findByUserId(Long userId);
    List<HabitEntity> findByFrequency(Frequency frequency);
    long countByUserId(Long userId);
}

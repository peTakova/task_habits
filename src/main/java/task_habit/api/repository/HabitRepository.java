package task_habit.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import task_habit.api.model.HabitEntity;

public interface HabitRepository extends JpaRepository<HabitEntity, Long> {
}

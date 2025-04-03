package task_habit.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import task_habit.api.model.Frequency;
import task_habit.api.model.HabitEntity;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<HabitEntity, Long> {
    Optional<HabitEntity> findByIdAndUserId(Long taskId, Long userId);
    List<HabitEntity> findByUserId(Long userId);
    List<HabitEntity> findByFrequency(Frequency frequency);
    long countByUserId(Long userId);
    Page<HabitEntity> findByUserId(Long userId, Pageable pageable);
    List<HabitEntity> findByUserIdAndFrequency(Long userId, Frequency frequency);
}

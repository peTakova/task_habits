package task_habit.api.service;

import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Service;
import task_habit.api.model.*;
import task_habit.api.repository.HabitRepository;
import task_habit.api.repository.UserRepository;

import java.util.Date;
import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private UserRepository userRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public List<HabitEntity> getAllHabits() {
        return this.habitRepository.findAll();
    }

    public HabitEntity createHabit(HabitEntity habit) {
        return this.habitRepository.save(habit);
    }

    public HabitEntity getHabit(Long userId, Long habitId) {
        return this.habitRepository.findByIdAndUserId(userId, habitId)
                .orElseThrow(() -> new IllegalArgumentException("Habit with ID " + habitId + " not found for user " + userId));
    }

    public List<HabitEntity> getUserHabits(Long userId) {
        return this.userRepository.findById(userId)
                .map(User::getHabits)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteHabitById(Long userId, Long taskId) {
        HabitEntity habit = this.habitRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Habit with ID " + taskId + " not found for user " + userId));
        this.habitRepository.delete(habit);
    }

    public void updateHabit(Long userId, Long taskId, String name, String description, Frequency frequency) {
        HabitEntity habit = this.habitRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Habit with ID " + taskId + " not found for user " + userId));
        if (name != null && !name.isBlank()) habit.setName(name);
        if (description != null) habit.setDescription(description);
        if (frequency != null) habit.setFrequency(frequency);
        this.habitRepository.save(habit);
    }

    public void markCompletedToday(Long userId, Long habitId) {
        HabitEntity habit = this.habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Habit with ID " + habitId + " not found for user " + userId));

        habit.setLastCompletedDate(Date());
        this.habitRepository.save(habit);
    }
}

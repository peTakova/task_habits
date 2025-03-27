package task_habit.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Service;
import task_habit.api.dto.HabitDTO;
import task_habit.api.model.*;
import task_habit.api.repository.HabitRepository;
import task_habit.api.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HabitService {

    @Autowired
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

    public HabitDTO createUserHabit(Long userId, HabitDTO habitDTO) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        HabitEntity habit = new HabitEntity();
        habit.setName(habitDTO.getName());
        habit.setDescription(habitDTO.getDescription());
        habit.setFrequency(Frequency.valueOf(habitDTO.getDueDate().toString()));
        habit.setLastCompletedDate(habitDTO.getLastCompletedDate());
        habit.setUser(user);

        HabitEntity savedHabit = this.habitRepository.save(habit);
        return new HabitDTO(
                savedHabit.getId(),
                savedHabit.getName(),
                savedHabit.getDescription(),
                savedHabit.getFrequency(),
                savedHabit.getLastCompletedDate(),
                savedHabit.getUser().getId()
        );
    }

    public HabitEntity getHabit(Long userId, Long habitId) {
        return this.habitRepository.findByIdAndUserId(userId, habitId)
                .orElseThrow(() -> new IllegalArgumentException("Habit with ID " + habitId + " not found for user " + userId));
    }

    public List<HabitDTO> getUserHabits(Long userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        List<HabitEntity> habits = this.habitRepository.findByUserId(userId);
        return habits.stream()
                .map(task -> new HabitDTO(
                        task.getId(),
                        task.getName(),
                        task.getDescription(),
                        task.getFrequency(),
                        task.getLastCompletedDate(),
                        task.getUser().getId()))
                .collect(Collectors.toList());
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

        habit.setLastCompletedDate(new Date());
        this.habitRepository.save(habit);
    }
}

package task_habit.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public HabitService(HabitRepository habitRepository, UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    public List<HabitEntity> getAllHabits() {
        return this.habitRepository.findAll();
    }

    public HabitEntity createHabit(HabitEntity habit) {
        return this.habitRepository.save(habit);
    }

    @Transactional
    public HabitDTO createUserHabit(Long userId, HabitDTO habitDTO) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        HabitEntity habit = new HabitEntity();
        habit.setName(habitDTO.getName());
        habit.setDescription(habitDTO.getDescription());
        habit.setFrequency(Frequency.valueOf(habitDTO.getFrequency().toString()));
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

    public List<HabitDTO> getAllWeeklyHabits() {
        List<HabitEntity> habits = this.habitRepository.findByFrequency(Frequency.WEEKLY);
        return habits.stream()
                .map(habit -> new HabitDTO(
                        habit.getId(),
                        habit.getName(),
                        habit.getDescription(),
                        habit.getFrequency(),
                        habit.getLastCompletedDate(),
                        habit.getUser().getId()))
                .collect(Collectors.toList());
    }

    public List<HabitDTO> getUserHabits() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " not found"));

        Long userId = user.getId();
        List<HabitEntity> habits = this.habitRepository.findByUserId(userId);
        return habits.stream()
                .map(habit -> new HabitDTO(
                        habit.getId(),
                        habit.getName(),
                        habit.getDescription(),
                        habit.getFrequency(),
                        habit.getLastCompletedDate(),
                        habit.getUser().getId()))
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

    public long getUserHabitsCount(Long userId) {
        return this.habitRepository.countByUserId(userId);
    }
}

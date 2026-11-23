package task_habit.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task_habit.api.dto.HabitDTO;
import task_habit.api.exception.BadRequestException;
import task_habit.api.exception.ResourceNotFoundException;
import task_habit.api.model.Frequency;
import task_habit.api.model.HabitEntity;
import task_habit.api.model.User;
import task_habit.api.repository.HabitRepository;
import task_habit.api.repository.UserRepository;

import java.util.Date;
import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;

    public HabitService(HabitRepository habitRepository, UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.userRepository = userRepository;
    }

    public List<HabitDTO> getUserHabits(Long userId) {
        return this.habitRepository.findByUserId(userId).stream().map(HabitDTO::fromEntity).toList();
    }

    public Page<HabitDTO> getUserHabitsPageable(Long userId, Pageable pageable) {
        return this.habitRepository.findByUserId(userId, pageable).map(HabitDTO::fromEntity);
    }

    public List<HabitDTO> getWeeklyHabits(Long userId) {
        return this.habitRepository.findByUserIdAndFrequency(userId, Frequency.WEEKLY).stream()
                .map(HabitDTO::fromEntity)
                .toList();
    }

    public HabitDTO getHabit(Long userId, Long habitId) {
        return HabitDTO.fromEntity(this.findOwnedHabit(userId, habitId));
    }

    @Transactional
    public HabitDTO createUserHabit(Long userId, HabitDTO habitDTO) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Používateľ neexistuje"));
        if (habitDTO.getName() == null || habitDTO.getName().isBlank()) {
            throw new BadRequestException("Názov návyku je povinný");
        }

        HabitEntity habit = new HabitEntity();
        habit.setName(habitDTO.getName().trim());
        habit.setDescription(habitDTO.getDescription() == null ? "" : habitDTO.getDescription());
        habit.setFrequency(habitDTO.getFrequency() == null ? Frequency.DAILY : habitDTO.getFrequency());
        habit.setLastCompletedDate(habitDTO.getLastCompletedDate());
        habit.setUser(user);

        return HabitDTO.fromEntity(this.habitRepository.save(habit));
    }

    /** Úprava návyku – null polia sa nemenia. */
    @Transactional
    public HabitDTO updateHabit(Long userId, Long habitId, HabitDTO update) {
        HabitEntity habit = this.findOwnedHabit(userId, habitId);
        if (update.getName() != null) {
            if (update.getName().isBlank()) {
                throw new BadRequestException("Názov návyku je povinný");
            }
            habit.setName(update.getName().trim());
        }
        if (update.getDescription() != null) {
            habit.setDescription(update.getDescription());
        }
        if (update.getFrequency() != null) {
            habit.setFrequency(update.getFrequency());
        }
        return HabitDTO.fromEntity(this.habitRepository.save(habit));
    }

    @Transactional
    public HabitDTO markCompletedToday(Long userId, Long habitId) {
        HabitEntity habit = this.findOwnedHabit(userId, habitId);
        habit.setLastCompletedDate(new Date());
        return HabitDTO.fromEntity(this.habitRepository.save(habit));
    }

    @Transactional
    public void deleteHabit(Long userId, Long habitId) {
        this.habitRepository.delete(this.findOwnedHabit(userId, habitId));
    }

    public long getUserHabitsCount(Long userId) {
        return this.habitRepository.countByUserId(userId);
    }

    public long getUserHabitsCount(Long userId, Frequency frequency) {
        return this.habitRepository.countByUserIdAndFrequency(userId, frequency);
    }

    private HabitEntity findOwnedHabit(Long userId, Long habitId) {
        return this.habitRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Návyk s ID " + habitId + " neexistuje"));
    }
}

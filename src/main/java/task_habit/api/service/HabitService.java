package task_habit.api.service;

import org.springframework.stereotype.Service;
import task_habit.api.model.HabitEntity;
import task_habit.api.repository.HabitRepository;

import java.util.List;

@Service
public class HabitService {

    private final HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public List<HabitEntity> getAllHabits() {
        return this.habitRepository.findAll();
    }

    public HabitEntity createHabit(HabitEntity habit) {
        return this.habitRepository.save(habit);
    }
}

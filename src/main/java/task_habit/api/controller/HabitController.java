package task_habit.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task_habit.api.dto.HabitDTO;
import task_habit.api.model.HabitEntity;
import task_habit.api.service.HabitService;

import java.util.List;

@RestController
@RequestMapping("/habit")
public class HabitController {

    @Autowired
    private HabitService habitService;

    @GetMapping("/habits/{usersId}")
    public ResponseEntity<List<HabitDTO>> getAllUserHabits(@PathVariable("userId") Long userId) {
        try {
            List<HabitDTO> tasks = this.habitService.getUserHabits(userId);
            if (tasks.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{userId}/create")
    public ResponseEntity<HabitDTO> createHabit(@PathVariable Long userId, @RequestBody HabitDTO habitDTO) {
        try {
            HabitDTO createdHabit = this.habitService.createUserHabit(userId, habitDTO);
            return new ResponseEntity<>(createdHabit, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{usersId}/get/{habitId}")
    public ResponseEntity<HabitDTO> getHabit(@PathVariable Long userId, @PathVariable Long habitId) {
        try {
            HabitEntity habit = this.habitService.getHabit(userId, habitId);
            HabitDTO habitDTO = new HabitDTO(
                    habit.getId(),
                    habit.getName(),
                    habit.getDescription(),
                    habit.getFrequency(),
                    habit.getLastCompletedDate(),
                    habit.getUser().getId()
            );
            return new ResponseEntity<>(habitDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{usersId}/update/{habitId}")
    public ResponseEntity<String> updateHabit (@PathVariable Long userId, @PathVariable Long taskId, @RequestBody HabitEntity updatedHabit) {
        try {
            this.habitService.updateHabit(userId, taskId, updatedHabit.getName(),updatedHabit.getDescription(),  updatedHabit.getFrequency());
            return new ResponseEntity<>("Task updated successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }


    @DeleteMapping("/{usersId}/delete/{habitId}")
    public ResponseEntity<String> deleteHabit (@PathVariable Long userId, @PathVariable Long habitId) {
        try {
            this.habitService.deleteHabitById(userId, habitId);
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @PatchMapping("/{usersId}/habit/{taskId}/complete")
    public ResponseEntity<String> markHabitCompleted(@PathVariable Long userId, @PathVariable Long taskId) {
        try {
            this.habitService.markCompletedToday(userId, taskId);
            return new ResponseEntity<>("Task marked as completed", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}

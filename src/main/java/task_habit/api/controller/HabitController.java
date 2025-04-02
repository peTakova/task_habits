package task_habit.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/habits")
    public ResponseEntity<List<HabitDTO>> getAllUserHabits() {
        try {
            List<HabitDTO> tasks = this.habitService.getUserHabits();
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

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<HabitDTO>> getAllHabits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
            Page<HabitDTO> habitsPage = this.habitService.getAllTasksPageable(pageable);

            return new ResponseEntity<>(habitsPage, HttpStatus.OK);
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
    public ResponseEntity<HabitDTO> getHabit(@PathVariable Long usersId, @PathVariable Long habitId) {
        try {
            HabitEntity habit = this.habitService.getHabit(usersId, habitId);
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

    @PutMapping("/{userId}/update/{habitId}")
    public ResponseEntity<String> updateHabit (@PathVariable Long userId, @PathVariable Long habitId, @RequestBody HabitEntity updatedHabit) {
        try {
            this.habitService.updateHabit(userId, habitId, updatedHabit.getName(),updatedHabit.getDescription(),  updatedHabit.getFrequency());
            return new ResponseEntity<>("Task updated successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }


    @DeleteMapping("/{userId}/delete/{habitId}")
    public ResponseEntity<String> deleteHabit (@PathVariable Long userId, @PathVariable Long habitId) {
        try {
            this.habitService.deleteHabitById(userId, habitId);
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @PatchMapping("/{userId}/habit/{taskId}/complete")
    public ResponseEntity<String> markHabitCompleted(@PathVariable Long userId, @PathVariable Long taskId) {
        try {
            this.habitService.markCompletedToday(userId, taskId);
            return new ResponseEntity<>("Task marked as completed", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}

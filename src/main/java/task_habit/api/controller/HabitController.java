package task_habit.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task_habit.api.dto.HabitDTO;
import task_habit.api.service.HabitService;
import task_habit.api.service.UserService;

import java.util.List;

/**
 * Návyky prihláseného používateľa.
 *
 * GET    /habit                – všetky návyky
 * GET    /habit/page           – stránkovanie (?page, size, sort=pole,asc|desc)
 * GET    /habit/{id}           – detail
 * POST   /habit                – vytvorenie
 * PUT    /habit/{id}           – úprava (name, description, frequency)
 * PATCH  /habit/{id}/complete  – splnené dnes
 * DELETE /habit/{id}           – zmazanie
 */
@RestController
@RequestMapping("/habit")
public class HabitController {

    private final HabitService habitService;
    private final UserService userService;

    public HabitController(HabitService habitService, UserService userService) {
        this.habitService = habitService;
        this.userService = userService;
    }

    @GetMapping
    public List<HabitDTO> getAllUserHabits() {
        return this.habitService.getUserHabits(this.userService.getCurrentUserId());
    }

    @GetMapping("/page")
    public Page<HabitDTO> getHabitsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return this.habitService.getUserHabitsPageable(this.userService.getCurrentUserId(), PageRequests.of(page, size, sort));
    }

    @GetMapping("/{habitId}")
    public HabitDTO getHabit(@PathVariable Long habitId) {
        return this.habitService.getHabit(this.userService.getCurrentUserId(), habitId);
    }

    @PostMapping
    public ResponseEntity<HabitDTO> createHabit(@RequestBody HabitDTO habitDTO) {
        HabitDTO created = this.habitService.createUserHabit(this.userService.getCurrentUserId(), habitDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{habitId}")
    public HabitDTO updateHabit(@PathVariable Long habitId, @RequestBody HabitDTO habitDTO) {
        return this.habitService.updateHabit(this.userService.getCurrentUserId(), habitId, habitDTO);
    }

    @PatchMapping("/{habitId}/complete")
    public HabitDTO markHabitCompleted(@PathVariable Long habitId) {
        return this.habitService.markCompletedToday(this.userService.getCurrentUserId(), habitId);
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable Long habitId) {
        this.habitService.deleteHabit(this.userService.getCurrentUserId(), habitId);
        return ResponseEntity.noContent().build();
    }
}

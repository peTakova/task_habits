package task_habit.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task_habit.api.dto.TaskDTO;
import task_habit.api.service.TaskService;
import task_habit.api.service.UserService;

import java.util.List;

/**
 * Úlohy prihláseného používateľa.
 *
 * GET    /task                 – všetky úlohy
 * GET    /task/page            – stránkovanie (?page, size, sort=pole,asc|desc)
 * GET    /task/{id}            – detail
 * POST   /task                 – vytvorenie
 * PUT    /task/{id}            – úprava (title, description, dueDate, status)
 * PATCH  /task/{id}/complete   – označenie ako hotová
 * DELETE /task/{id}            – zmazanie
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping
    public List<TaskDTO> getAllUserTasks() {
        return this.taskService.getUserTasks(this.userService.getCurrentUserId());
    }

    @GetMapping("/page")
    public Page<TaskDTO> getTasksPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return this.taskService.getUserTasksPageable(this.userService.getCurrentUserId(), PageRequests.of(page, size, sort));
    }

    @GetMapping("/{taskId}")
    public TaskDTO getTask(@PathVariable Long taskId) {
        return this.taskService.getTask(this.userService.getCurrentUserId(), taskId);
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        TaskDTO created = this.taskService.createUserTask(this.userService.getCurrentUserId(), taskDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{taskId}")
    public TaskDTO updateTask(@PathVariable Long taskId, @RequestBody TaskDTO taskDTO) {
        return this.taskService.updateTask(this.userService.getCurrentUserId(), taskId, taskDTO);
    }

    @PatchMapping("/{taskId}/complete")
    public TaskDTO markTaskCompleted(@PathVariable Long taskId) {
        return this.taskService.markCompleted(this.userService.getCurrentUserId(), taskId);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        this.taskService.deleteTask(this.userService.getCurrentUserId(), taskId);
        return ResponseEntity.noContent().build();
    }
}

/** Pomocník na parsovanie parametra sort ("pole,asc"). */
final class PageRequests {
    private PageRequests() {}

    static Pageable of(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String field = sortParams[0].isBlank() ? "id" : sortParams[0];
        Sort.Direction direction = sortParams.length > 1
                ? Sort.Direction.fromOptionalString(sortParams[1]).orElse(Sort.Direction.ASC)
                : Sort.Direction.ASC;
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, field));
    }
}

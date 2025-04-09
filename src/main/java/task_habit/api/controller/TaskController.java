package task_habit.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import task_habit.api.dto.TaskDTO;
import task_habit.api.model.TaskEntity;
import task_habit.api.service.TaskService;
import task_habit.api.service.UserService;
import task_habit.api.model.User;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;
    private UserService userService;

    @Autowired
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getAllUserTasks() {
        try {
            Long userId = this.getAuthenticatedUserId();
            List<TaskDTO> tasks = this.taskService.getUserTasks();
            if (tasks.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(tasks, HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all/page")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        try {
            Long userId = this.getAuthenticatedUserId();
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
            Page<TaskDTO> tasksPage = this.taskService.getAllTasksPageable(pageable);

            return new ResponseEntity<>(tasksPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) {
        System.out.println("Received Task: " + taskDTO);
        try {
            Long userId = this.getAuthenticatedUserId();
            TaskDTO createdTask = this.taskService.createUserTask(userId, taskDTO);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDTO> getTask(@PathVariable("taskId") Long taskId) {
        try {
            Long userId = this.getAuthenticatedUserId();
            TaskEntity task = this.taskService.getTask(userId, taskId);
            TaskDTO taskDTO = new TaskDTO(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate(),
                    task.getStatus().name(),
                    task.getUser().getId()
            );
            return new ResponseEntity<>(taskDTO, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{userId}/update/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateTask (@PathVariable Long taskId, @RequestBody TaskEntity updatedTask) {
        try {
            Long userId = this.getAuthenticatedUserId();
            this.taskService.updateTask(userId, taskId, updatedTask.getTitle(), updatedTask.getDescription(), updatedTask.getStatus());
            return new ResponseEntity<>("Task updated successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{userId}/delete/{taskId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteTask (@PathVariable Long taskId) {
        try {
            Long userId = this.getAuthenticatedUserId();
            this.taskService.deleteTaskById(userId, taskId);
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/tasks/{taskId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> markTaskCompleted(@PathVariable Long taskId) {
        try {
            Long userId = this.getAuthenticatedUserId();
            this.taskService.markCompleted(taskId, userId);
            return new ResponseEntity<>("Task marked as completed", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private Long getAuthenticatedUserId() {
        String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return this.userService.getUserByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}

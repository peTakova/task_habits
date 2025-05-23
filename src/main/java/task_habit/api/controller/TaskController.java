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
import task_habit.api.dto.TaskDTO;
import task_habit.api.model.TaskEntity;
import task_habit.api.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getAllUserTasks() {
        try {
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
    @PreAuthorize("isAuthenticated()") // Pridaná autentifikácia
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        try {
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

    @PostMapping("/{userId}/create")
    public ResponseEntity<TaskDTO> createTask(@PathVariable Long userId, @RequestBody TaskDTO taskDTO) {
        try {
            TaskDTO createdTask = this.taskService.createUserTask(userId, taskDTO);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}/get/{taskId}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long userId, @PathVariable("taskId") Long taskId) {
        try {
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
    public ResponseEntity<String> updateTask (@PathVariable Long userId, @PathVariable Long taskId, @RequestBody TaskEntity updatedTask) {
        try {
            this.taskService.updateTask(userId, taskId, updatedTask.getTitle(), updatedTask.getDescription(), updatedTask.getStatus());
            return new ResponseEntity<>("Task updated successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{userId}/delete/{taskId}")
    public ResponseEntity<String> deleteTask (@PathVariable Long userId, @PathVariable Long taskId) {
        try {
            this.taskService.deleteTaskById(userId, taskId);
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{usersId}/tasks/{taskId}/complete")
    public ResponseEntity<String> markTaskCompleted(@PathVariable Long usersId, @PathVariable Long taskId) {
        try {
            this.taskService.markCompleted(taskId, usersId);
            return new ResponseEntity<>("Task marked as completed", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}

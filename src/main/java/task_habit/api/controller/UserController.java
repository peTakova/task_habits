package task_habit.api.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import task_habit.api.config.JwtUtil;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.TaskStatus;
import task_habit.api.model.User;
import task_habit.api.service.TaskService;
import task_habit.api.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
@Autowired
    private PasswordEncoder passwordEncoder;
    private final List<User> users = new ArrayList<>();
    private TaskService taskService;

    public UserController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
    }

    //user endpoints------------------------------------------------------------------------------------
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return this.userService.getAllUsers();
    }

    @GetMapping("findById/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = this.userService.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Používateľ s tymto id " + id + " neexistuje.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody User user) {

        if (this.userService.getUserByEmail(user.getEmail()).isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        this.userService.saveUser(user);

        String token = JwtUtil.generateToken(user.getEmail() );
        //return ResponseEntity.ok(Map.of("message", "Registrácia úspešná", "token", token));
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registrácia úspešná");
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = this.userService.deleteUserById(id);

        if (deleted) {
            return ResponseEntity.ok("Používateľ s ID " + id + " bol úspešne vymazaný.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Používateľ s ID " + id + " neexistuje.");
        }
    }
    //------------------------------------------------------------------------------------------------------


    //task endpoints----------------------------------------------------------------------------------------

    @PatchMapping("/{usersId}/tasks/{taskId}/complete")
    public ResponseEntity<String> markTaskCompleted(@PathVariable Long userId,@PathVariable Long taskId) {
        try {
            this.taskService.markCompleted(taskId, userId);
            return new ResponseEntity<>("Task marked as completed", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{usersId}/tasks")
    public ResponseEntity<String> getAllUserTasks(@PathVariable Long userId) {
        try {
            List<TaskEntity> tasks = this.taskService.getUserTasks(userId);
            if (tasks.isEmpty()) {
                return new ResponseEntity<>("No tasks found for user " + userId, HttpStatus.OK);
            }
            return new ResponseEntity<>(tasks.toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving tasks: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/{usersId}/tasks/{tasksId}")
    public ResponseEntity<String> getTask(@PathVariable Long userId, @PathVariable Long taskId) {
        try {
            TaskEntity task = this.taskService.getTask(userId, taskId);
            return new ResponseEntity<>(task.toString(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{usersId}/tasks/{tasksId}")
    public ResponseEntity<String> updateTask (@PathVariable Long userId, @PathVariable Long taskId, @RequestBody TaskEntity updatedTask) {
        try {
            this.taskService.updateTask(userId, taskId, updatedTask.getTitle(), updatedTask.getDescription(), updatedTask.getStatus());
            return new ResponseEntity<>("Task updated successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{usersId}/tasks/{tasksId}")
    public ResponseEntity<String> deleteTask (@PathVariable Long userId, @PathVariable Long taskId) {
        try {
            this.taskService.deleteTaskById(userId, taskId);
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    //-----------------------------------------------------------------------------------------------------

    //habit endpoints

    //-----------------------------------------------------------------------------------------------------


    //statistics-----------------------------------------------------------------------------------------
}

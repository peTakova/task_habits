package task_habit.api.controller;

import jakarta.validation.Valid;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import task_habit.api.config.JwtUtil;
import task_habit.api.dto.HabitDTO;
import task_habit.api.dto.TaskDTO;
import task_habit.api.dto.UserDTO;
import task_habit.api.dto.UserStatsDTO;
import task_habit.api.model.User;
import task_habit.api.service.HabitService;
import task_habit.api.service.TaskService;
import task_habit.api.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final TaskService taskService;
    private final HabitService habitService;
    private final JwtUtil jwtUtil;
@Autowired
    private PasswordEncoder passwordEncoder;

    public UserController(UserService userService, TaskService taskService, HabitService habitService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.taskService = taskService;
        this.habitService = habitService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = this.userService.getAllUsers();
            if (users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(users, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all/page")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        try {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);

            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
            Page<UserDTO> usersPage = this.userService.getAllUsersPageable(pageable);

            return new ResponseEntity<>(usersPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("findById/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = this.userService.getUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody User user) {

        if (this.userService.getUserByEmail(user.getEmail()).isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        this.userService.createUser(user);

        String token = this.jwtUtil.generateToken(user.getEmail() );
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

    //statistics-----------------------------------------------------------------------------------------

    @GetMapping("/completed-tasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDTO>> getAllCompletedTasks() {
        try {
            List<TaskDTO> completedTasks = this.taskService.getAllCompletedTasks();
            if (completedTasks.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(completedTasks, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/weekly-habits")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HabitDTO>> getAllWeeklyHabits() {
        try {
            List<HabitDTO> weeklyHabits = this.habitService.getAllWeeklyHabits();
            if (weeklyHabits.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(weeklyHabits, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()") // Iba autentifikovaní používatelia
    public ResponseEntity<UserStatsDTO> getUserStats() {
        try {
            String email = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            User user = this.userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Long userId = user.getId();
            long completedTasks = this.taskService.getUserCompletedTasksCount(userId);
            long totalHabits = this.habitService.getUserHabitsCount(userId);

            UserStatsDTO stats = new UserStatsDTO(userId, completedTasks, totalHabits);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

package task_habit.api.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task_habit.api.config.JwtUtil;
import task_habit.api.dto.*;
import task_habit.api.exception.ResourceNotFoundException;
import task_habit.api.filter.JwtRequestFilter;
import task_habit.api.model.Frequency;
import task_habit.api.model.User;
import task_habit.api.service.HabitService;
import task_habit.api.service.TaskService;
import task_habit.api.service.UserService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Používatelia, autentifikácia a štatistiky.
 *
 * POST   /users/register, /users/login, /users/logout   – verejné
 * GET    /users/me                 – prihlásený používateľ
 * PUT    /users/me                 – zmena mena / emailu
 * PUT    /users/me/password        – zmena hesla
 * DELETE /users/me                 – zmazanie účtu
 * GET    /users/stats              – súhrnné štatistiky
 * GET    /users/completed-tasks    – dokončené úlohy
 * GET    /users/weekly-habits      – týždenné návyky
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final String USER_ID_COOKIE = "userId";

    private final UserService userService;
    private final TaskService taskService;
    private final HabitService habitService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, TaskService taskService, HabitService habitService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.taskService = taskService;
        this.habitService = habitService;
        this.jwtUtil = jwtUtil;
    }

    // ---------------------------------------------------------------- autentifikácia

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        User user = this.userService.register(request);
        this.writeAuthCookies(user, response);
        return new ResponseEntity<>(UserDTO.fromEntity(user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return this.userService.authenticate(request.getEmail(), request.getPassword())
                .<ResponseEntity<?>>map(user -> {
                    this.writeAuthCookies(user, response);
                    return ResponseEntity.ok(UserDTO.fromEntity(user));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Nesprávny email alebo heslo")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(HttpServletResponse response) {
        this.clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- profil

    @GetMapping("/me")
    public UserDTO getCurrentUser() {
        return UserDTO.fromEntity(this.userService.getCurrentUser());
    }

    @PutMapping("/me")
    public UserDTO updateCurrentUser(@Valid @RequestBody UpdateUserRequest request, HttpServletResponse response) {
        String oldEmail = this.userService.getCurrentUser().getEmail();
        User updated = this.userService.updateCurrentUser(request);
        if (!updated.getEmail().equals(oldEmail)) {
            // subject JWT je email – po jeho zmene treba vydať nový token
            this.writeAuthCookies(updated, response);
        }
        return UserDTO.fromEntity(updated);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        this.userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(HttpServletResponse response) {
        this.userService.deleteCurrentUser();
        this.clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- štatistiky

    @GetMapping("/stats")
    public UserStatsDTO getUserStats() {
        Long userId = this.userService.getCurrentUserId();
        long completed = this.taskService.getUserCompletedTasksCount(userId);
        long total = this.taskService.getUserTasksCount(userId);
        return new UserStatsDTO(
                userId,
                completed,
                total - completed,
                total,
                this.habitService.getUserHabitsCount(userId),
                this.habitService.getUserHabitsCount(userId, Frequency.DAILY),
                this.habitService.getUserHabitsCount(userId, Frequency.WEEKLY),
                this.habitService.getUserHabitsCount(userId, Frequency.MONTHLY)
        );
    }

    @GetMapping("/completed-tasks")
    public List<TaskDTO> getAllCompletedTasks() {
        return this.taskService.getAllCompletedTasks(this.userService.getCurrentUserId());
    }

    @GetMapping("/weekly-habits")
    public List<HabitDTO> getAllWeeklyHabits() {
        return this.habitService.getWeeklyHabits(this.userService.getCurrentUserId());
    }

    // ---------------------------------------------------------------- administrácia (len pre prihlásených)

    @GetMapping("/all")
    public List<UserDTO> getAllUsers() {
        return this.userService.getAllUsers();
    }

    @GetMapping("/all/page")
    public Page<UserDTO> getAllUsersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return this.userService.getAllUsersPageable(PageRequests.of(page, size, sort));
    }

    @GetMapping("/findById/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return this.userService.getUserById(id)
                .map(UserDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Používateľ s ID " + id + " neexistuje"));
    }

    // ---------------------------------------------------------------- cookies

    private void writeAuthCookies(User user, HttpServletResponse response) {
        String token = this.jwtUtil.generateToken(Map.of("userId", user.getId()), user.getEmail());
        Duration maxAge = Duration.ofMillis(JwtUtil.JWT_EXPIRATION_MS);
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(JwtRequestFilter.AUTH_COOKIE, token, maxAge, true));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(USER_ID_COOKIE, user.getId().toString(), maxAge, false));
    }

    private void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(JwtRequestFilter.AUTH_COOKIE, "", Duration.ZERO, true));
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(USER_ID_COOKIE, "", Duration.ZERO, false));
    }

    private static String buildCookie(String name, String value, Duration maxAge, boolean httpOnly) {
        return ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(false) // v produkcii s HTTPS nastaviť na true
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString();
    }
}

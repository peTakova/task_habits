package task_habit.api.controller;

import org.springframework.web.bind.annotation.*;
import task_habit.api.model.User;
import task_habit.api.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return this.userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return this.userService.createUser(user);
    }
}

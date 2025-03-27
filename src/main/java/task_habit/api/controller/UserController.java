package task_habit.api.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import task_habit.api.config.JwtUtil;
import task_habit.api.dto.UserDTO;
import task_habit.api.model.User;
import task_habit.api.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
@Autowired
    private PasswordEncoder passwordEncoder;

    public UserController(UserService userService) {
        this.userService = userService;
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

    //statistics-----------------------------------------------------------------------------------------
}

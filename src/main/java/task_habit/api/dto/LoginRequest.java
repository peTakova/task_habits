package task_habit.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email je povinný")
    private String email;

    @NotBlank(message = "Heslo je povinné")
    private String password;
}

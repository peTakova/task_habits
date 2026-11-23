package task_habit.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Používateľské meno je povinné")
    @Size(min = 3, max = 50, message = "Používateľské meno musí mať 3 až 50 znakov")
    private String username;

    @NotBlank(message = "Email je povinný")
    @Email(message = "Neplatný email")
    private String email;
}

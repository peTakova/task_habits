package task_habit.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Zadajte aktuálne heslo")
    private String currentPassword;

    @NotBlank(message = "Zadajte nové heslo")
    @Size(min = 6, message = "Nové heslo musí mať aspoň 6 znakov")
    private String newPassword;
}

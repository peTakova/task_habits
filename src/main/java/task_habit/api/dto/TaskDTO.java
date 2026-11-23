package task_habit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import task_habit.api.model.TaskEntity;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Date dueDate;
    private String status; // PENDING | COMPLETED
    private Long userId;
    private String reminderMessage;

    public static TaskDTO fromEntity(TaskEntity task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus().name(),
                task.getUser().getId(),
                task.getReminderMessage()
        );
    }
}

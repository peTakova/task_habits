package task_habit.api.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Date dueDate;
    private String status; // Enum ako String
    private Long userId;

//    public TaskDTO(Long id, String title, String description, Date dueDate, String status, Long userId) {
//        this.id = id;
//        this.title = title;
//        this.description = description;
//        this.dueDate = dueDate;
//        this.status = status;
//        this.userId = userId;
//    }
}

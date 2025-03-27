package task_habit.api.dto;

import lombok.Data;
import task_habit.api.model.Frequency;

import java.util.Date;

@Data
public class HabitDTO {
    private Long id;
    private String name;
    private String description;
    private Frequency dueDate;
    private Date lastCompletedDate;
    private Long userId;

    public HabitDTO(Long id, String name, String description, Frequency dueDate, Date lastCompletedDate, Long userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.lastCompletedDate = lastCompletedDate;
        this.userId = userId;
    }

}

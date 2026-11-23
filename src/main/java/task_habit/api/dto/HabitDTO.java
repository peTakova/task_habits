package task_habit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import task_habit.api.model.Frequency;
import task_habit.api.model.HabitEntity;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HabitDTO {
    private Long id;
    private String name;
    private String description;
    private Frequency frequency;
    private Date lastCompletedDate;
    private Long userId;

    public static HabitDTO fromEntity(HabitEntity habit) {
        return new HabitDTO(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getFrequency(),
                habit.getLastCompletedDate(),
                habit.getUser().getId()
        );
    }
}

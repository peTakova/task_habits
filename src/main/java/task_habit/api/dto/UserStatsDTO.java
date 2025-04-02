package task_habit.api.dto;
import lombok.Data;

@Data
public class UserStatsDTO {

    private Long userId;
    private long completedTasks;
    private long totalHabits;

    public UserStatsDTO(Long userId, long completedTasks, long totalHabits) {
        this.userId = userId;
        this.completedTasks = completedTasks;
        this.totalHabits = totalHabits;
    }
}

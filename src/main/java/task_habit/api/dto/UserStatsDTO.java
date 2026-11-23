package task_habit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private Long userId;
    private long completedTasks;
    private long pendingTasks;
    private long totalTasks;
    private long totalHabits;
    private long dailyHabits;
    private long weeklyHabits;
    private long monthlyHabits;
}

package task_habit.api.service;

import org.springframework.stereotype.Service;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.TaskStatus;
import task_habit.api.model.User;
import task_habit.api.repository.TaskRepository;
import task_habit.api.repository.UserRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private UserRepository userRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskEntity createTask(TaskEntity task) {
        return this.taskRepository.save(task);
    }

    public TaskEntity getTask(Long userId, Long taskId) {
        return this.taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task with ID " + taskId + " not found for user " + userId));
    }

    public List<TaskEntity> getUserTasks(Long userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));
        return this.taskRepository.findByUserId(userId);
    }

    public void deleteTaskById(Long userId, Long taskId) {
        TaskEntity task = this.taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task with ID " + taskId + " not found for user " + userId));
        this.taskRepository.delete(task);
    }

    public void updateTask(Long userId, Long taskId, String title, String description, TaskStatus status) {
        TaskEntity task = this.taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task with ID " + taskId + " not found for user " + userId));
        if (title != null && !title.isBlank()) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (status != null) task.setStatus(status);
        this.taskRepository.save(task);
    }

    public void markCompleted(Long userId, Long taskId) {
        TaskEntity task = this.taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task with ID " + taskId + " not found for user " + userId));

        task.setStatus(TaskStatus.COMPLETED);
        this.taskRepository.save(task);
    }

}

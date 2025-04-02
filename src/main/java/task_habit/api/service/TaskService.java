package task_habit.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import task_habit.api.dto.TaskDTO;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.TaskStatus;
import task_habit.api.model.User;
import task_habit.api.repository.TaskRepository;
import task_habit.api.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private final TaskRepository taskRepository;
    private UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskEntity createTask(TaskEntity task) {
        return this.taskRepository.save(task);
    }

    public TaskEntity getTask(Long userId, Long taskId) {
        return this.taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task with ID " + taskId + " not found for user " + userId));
    }

    public List<TaskDTO> getAllCompletedTasks() {
        List<TaskEntity> tasks = this.taskRepository.findByStatus(TaskStatus.COMPLETED);
        return tasks.stream()
                .map(task -> new TaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getDueDate(),
                        task.getStatus().name(),
                        task.getUser().getId()))
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getUserTasks(Long userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        List<TaskEntity> tasks = this.taskRepository.findByUserId(userId);
        return tasks.stream()
                .map(task -> new TaskDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getDueDate(),
                        task.getStatus().name(), // Enum ako String
                        task.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO createUserTask(Long userId, TaskDTO taskDTO) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        TaskEntity task = new TaskEntity();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setDueDate(taskDTO.getDueDate());
        task.setStatus(TaskStatus.valueOf(taskDTO.getStatus().toUpperCase()));
        task.setUser(user);

        TaskEntity savedTask = this.taskRepository.save(task);
        return new TaskDTO(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getDescription(),
                savedTask.getDueDate(),
                savedTask.getStatus().name(),
                savedTask.getUser().getId()
        );
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

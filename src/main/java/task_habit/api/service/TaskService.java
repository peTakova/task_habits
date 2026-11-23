package task_habit.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task_habit.api.dto.TaskDTO;
import task_habit.api.exception.BadRequestException;
import task_habit.api.exception.ResourceNotFoundException;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.TaskStatus;
import task_habit.api.model.User;
import task_habit.api.repository.TaskRepository;
import task_habit.api.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<TaskDTO> getUserTasks(Long userId) {
        return this.taskRepository.findByUserId(userId).stream().map(TaskDTO::fromEntity).toList();
    }

    public Page<TaskDTO> getUserTasksPageable(Long userId, Pageable pageable) {
        return this.taskRepository.findByUserId(userId, pageable).map(TaskDTO::fromEntity);
    }

    public List<TaskDTO> getAllCompletedTasks(Long userId) {
        return this.taskRepository.findByUserIdAndStatus(userId, TaskStatus.COMPLETED).stream()
                .map(TaskDTO::fromEntity)
                .toList();
    }

    public TaskDTO getTask(Long userId, Long taskId) {
        return TaskDTO.fromEntity(this.findOwnedTask(userId, taskId));
    }

    @Transactional
    public TaskDTO createUserTask(Long userId, TaskDTO taskDTO) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Používateľ neexistuje"));
        if (taskDTO.getTitle() == null || taskDTO.getTitle().isBlank()) {
            throw new BadRequestException("Názov úlohy je povinný");
        }

        TaskEntity task = new TaskEntity();
        task.setTitle(taskDTO.getTitle().trim());
        task.setDescription(taskDTO.getDescription());
        task.setDueDate(taskDTO.getDueDate());
        task.setStatus(parseStatus(taskDTO.getStatus(), TaskStatus.PENDING));
        task.setUser(user);

        return TaskDTO.fromEntity(this.taskRepository.save(task));
    }

    /** Úprava úlohy – null polia sa nemenia, dueDate sa vždy prepíše (null = bez termínu). */
    @Transactional
    public TaskDTO updateTask(Long userId, Long taskId, TaskDTO update) {
        TaskEntity task = this.findOwnedTask(userId, taskId);

        if (update.getTitle() != null) {
            if (update.getTitle().isBlank()) {
                throw new BadRequestException("Názov úlohy je povinný");
            }
            task.setTitle(update.getTitle().trim());
        }
        if (update.getDescription() != null) {
            task.setDescription(update.getDescription());
        }
        if (!Objects.equals(task.getDueDate(), update.getDueDate())) {
            task.setDueDate(update.getDueDate());
            task.setReminderMessage(null); // pripomienku vygeneruje scheduler znova podľa nového termínu
        }
        if (update.getStatus() != null) {
            TaskStatus status = parseStatus(update.getStatus(), task.getStatus());
            task.setStatus(status);
            if (status == TaskStatus.COMPLETED) {
                task.setReminderMessage(null);
            }
        }
        return TaskDTO.fromEntity(this.taskRepository.save(task));
    }

    @Transactional
    public TaskDTO markCompleted(Long userId, Long taskId) {
        TaskEntity task = this.findOwnedTask(userId, taskId);
        task.setStatus(TaskStatus.COMPLETED);
        task.setReminderMessage(null);
        return TaskDTO.fromEntity(this.taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        this.taskRepository.delete(this.findOwnedTask(userId, taskId));
    }

    public long getUserCompletedTasksCount(Long userId) {
        return this.taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
    }

    public long getUserTasksCount(Long userId) {
        return this.taskRepository.countByUserId(userId);
    }

    private TaskEntity findOwnedTask(Long userId, Long taskId) {
        return this.taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Úloha s ID " + taskId + " neexistuje"));
    }

    private static TaskStatus parseStatus(String status, TaskStatus fallback) {
        if (status == null || status.isBlank()) {
            return fallback;
        }
        try {
            return TaskStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Neplatný stav úlohy: " + status);
        }
    }
}

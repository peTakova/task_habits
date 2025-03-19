package task_habit.api.service;

import org.springframework.stereotype.Service;
import task_habit.api.model.TaskEntity;
import task_habit.api.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskEntity> getAllTasks() {
        return this.taskRepository.findAll();
    }

    public TaskEntity createTask(TaskEntity task) {
        return this.taskRepository.save(task);
    }
}

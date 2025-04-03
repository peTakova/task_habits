package task_habit.api.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.TaskStatus;
import task_habit.api.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@EnableScheduling
public class TaskReminderService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskReminderService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(fixedRate = 600000)
    public void generateTaskReminders() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        Date tomorrow = calendar.getTime();

        List<TaskEntity> tasks = this.taskRepository.findAllByDueDateBetweenAndStatusNot(
                now,
                tomorrow,
                TaskStatus.COMPLETED
        );

        for (TaskEntity task : tasks) {
            if (task.getDueDate().after(now) && task.getDueDate().before(tomorrow)) {
                this.setReminderMessage(task);
            }
        }
    }

    private void setReminderMessage(TaskEntity task) {
        String message = "Upozornenie: Úloha '" + task.getTitle() + "' má termín do 24 hodín!\n" +
                "Popis: " + (task.getDescription() != null ? task.getDescription() : "Žiadny") + "\n" +
                "Termín: " + task.getDueDate() + "\n" +
                "Dokonči ju čím skôr!";

        task.setReminderMessage(message);
        this.taskRepository.save(task);
    }

    private Date adjustTime(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }
}

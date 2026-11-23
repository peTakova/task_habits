package task_habit.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import task_habit.api.model.TaskEntity;
import task_habit.api.model.TaskStatus;
import task_habit.api.repository.TaskRepository;

import java.util.Date;
import java.util.List;

/**
 * Každých 10 minút nastaví pripomienku úlohám, ktorých termín je do 24 hodín.
 */
@Service
@EnableScheduling
public class TaskReminderService {

    private static final Logger log = LoggerFactory.getLogger(TaskReminderService.class);

    /** Stĺpec reminder_message má 255 znakov. */
    static final int MAX_MESSAGE_LENGTH = 255;
    private static final int MAX_TITLE_LENGTH = 150;
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private final TaskRepository taskRepository;

    public TaskReminderService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(fixedRate = 600000)
    public void generateTaskReminders() {
        Date now = new Date();
        Date tomorrow = new Date(now.getTime() + DAY_MS);

        List<TaskEntity> tasks = this.taskRepository.findAllByDueDateBetweenAndStatusNot(now, tomorrow, TaskStatus.COMPLETED);
        for (TaskEntity task : tasks) {
            try {
                // Mení sa len stĺpec s pripomienkou a len ak sa úloha medzitým nezmenila.
                this.taskRepository.setReminderIfStillDue(task.getId(), task.getDueDate(), buildMessage(task), TaskStatus.COMPLETED);
            } catch (RuntimeException e) {
                // Chyba pri jednej úlohe nesmie zastaviť pripomienky pre ostatné.
                log.warn("Pripomienku pre úlohu {} sa nepodarilo uložiť: {}", task.getId(), e.getMessage());
            }
        }
    }

    static String buildMessage(TaskEntity task) {
        String title = task.getTitle() == null ? "" : task.getTitle().strip();
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH - 1) + "…";
        }
        String message = "Upozornenie: úloha „" + title + "“ má termín do 24 hodín. Dokonči ju čím skôr!";
        return message.length() > MAX_MESSAGE_LENGTH ? message.substring(0, MAX_MESSAGE_LENGTH) : message;
    }
}

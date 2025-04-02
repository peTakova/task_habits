package task_habit.api.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
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
    private final JavaMailSender mailSender;

    @Autowired
    public TaskReminderService(TaskRepository taskRepository, JavaMailSender mailSender) {
        this.taskRepository = taskRepository;
        this.mailSender = mailSender;
    }

    @Scheduled(fixedRate = 3600000)
    public void sendTaskReminders() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();

        List<TaskEntity> tasks = this.taskRepository.findAllByDueDateBetweenAndStatusNot(
                this.adjustTime(tomorrow, -1),
                this.adjustTime(tomorrow, 1),
                TaskStatus.COMPLETED
        );

        for (TaskEntity task : tasks) {
            if (task.getDueDate().after(now) && task.getDueDate().before(adjustTime(tomorrow, 1))) {
                this.sendReminderEmail(task);
            }
        }
    }

    private void sendReminderEmail(TaskEntity task) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(task.getUser().getEmail());
        message.setSubject("Pripomenutie úlohy: " + task.getTitle());
        message.setText("Ahoj " + task.getUser().getUsername() + ",\n\n" +
                "Pripomíname ti úlohu '" + task.getTitle() + "', ktorá má termín zajtra.\n" +
                "Popis: " + (task.getDescription() != null ? task.getDescription() : "Žiadny") + "\n" +
                "Termín: " + task.getDueDate() + "\n\n" +
                "Nezabudni ju dokončiť!\n\n" +
                "Tvoj TaskHabit tím");

        this.mailSender.send(message);
    }

    private Date adjustTime(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }
}

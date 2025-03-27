package task_habit.api.model;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "task_entity")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private Date dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public TaskEntity() {}
    public TaskEntity(User user) {
        this.user = user;
    }
}

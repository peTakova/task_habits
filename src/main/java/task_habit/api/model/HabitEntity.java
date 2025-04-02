package task_habit.api.model;

import lombok.Data;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Data
@Table(name = "habit_entity")
public class HabitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(name = "lastCompletedDate")
    private Date lastCompletedDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

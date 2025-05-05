package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Class Task
 * Entity class representing a task in the system.
 * A task has a title, description, status, priority, creator, executor, and associated comments.
 */
@Entity
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Task {

    /**
     * The unique identifier of the task.
     * This is a UUID that uniquely identifies the task.
     */
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    /**
     * The title of the task.
     * This is a short description of what the task is about.
     */
    private String title;

    /**
     * The detailed description of the task.
     * This is a longer explanation of the task's requirements or context.
     */
    private String description;

    /**
     * The status of the task.
     * Possible statuses: "pending", "in progress", or "completed".
     */
    private String status;

    /**
     * The priority of the task.
     * Possible priorities: "high", "mid", or "low".
     */
    private String priority;

    // And add this field to the Task class:
    /**
     * The due date of the task.
     * This is the date by which the task should be completed.
     */
    private LocalDate dueDate;


    /**
     * The creator of the task.
     * This is a reference to the user who created the task.
     *
     * @see User
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_creator", nullable = false)
    private User creator;

    /**
     * The executor of the task.
     * This is a reference to the user assigned to execute the task.
     *
     * @see User
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_executor", nullable = false)
    private User executor;
}

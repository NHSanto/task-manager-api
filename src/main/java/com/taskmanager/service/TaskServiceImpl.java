package com.taskmanager.service;

import com.taskmanager.dto.IdDto;
import com.taskmanager.dto.TaskDto;
import com.taskmanager.dto.TaskNoIdDto;
import com.taskmanager.dto.UserDto;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.mapper.UserMapper;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.interfaces.TaskService;
import com.taskmanager.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Class TaskServiceImpl
 *
 * Service implementation for managing tasks, including adding, updating, deleting, and retrieving tasks.
 */
@Slf4j
@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    /**
     * Adds a new task.
     *
     * @param taskNoIdDto The DTO containing the task data (without ID).
     * @return A {@link TaskDto} representing the saved task.
     */
    @Override
    public TaskDto addTask(TaskNoIdDto taskNoIdDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator =  userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserDto creatorDto = UserMapper.toDto(creator);
        taskNoIdDto.setCreator(creatorDto);
        taskNoIdDto.setExecutor(creatorDto);
        Task task = taskRepository.save(modelMapper.map(taskNoIdDto, Task.class));
        return modelMapper.map(task, TaskDto.class);
    }

    /**
     * Updates an existing task.
     *
     * @param taskDto The DTO containing the updated task data (with ID).
     * @return A {@link TaskDto} representing the updated task.
     * @throws BadRequestException If the task with the provided ID does not exist.
     */
    @Transactional
    @Override
    public TaskDto updateTask(TaskDto taskDto) {
        Task taskOld = taskRepository.findById(taskDto.getId()).orElseThrow(
                () -> new BadRequestException("Provided task doesn't exist"));
//        List<Comment> comments = taskOld.getComments();
        Task task = modelMapper.map(taskDto, Task.class);
//        task.setComments(comments);
        taskRepository.save(task);
        return modelMapper.map(task, TaskDto.class);
    }

    /**
     * Updates the status of a task.
     *
     * @param taskId The ID of the task to update.
     * @param newStatus The new status to set for the task.
     * @return A {@link TaskDto} representing the updated task.
     * @throws BadRequestException If the task does not exist or if the new status is the same as the current status.
     */
    @Override
    public TaskDto updateTaskStatus(UUID taskId, String newStatus) {
        Task taskDB = taskRepository.findById(taskId)
                .orElseThrow(() -> new BadRequestException("Provided task doesn't exist"));
        if (taskDB.getStatus().equals(newStatus)) {
            throw new BadRequestException("The new status must be different from the current status");
        }
        taskDB.setStatus(newStatus);
        return modelMapper.map(taskRepository.save(taskDB), TaskDto.class);
    }

    /**
     * Deletes a task.
     *
     * @param idDto The DTO containing the ID of the task to delete.
     */
    @Override
    public void deleteTask(IdDto idDto) {
        taskRepository.deleteById(idDto.id());
    }

    /**
     * Retrieves all tasks with pagination support.
     *
     * @param pageable The pagination information (e.g., page number, size).
     * @return A {@link Page} of {@link TaskDto} representing all tasks.
     */
    @Override
    public Page<TaskDto> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable).map(task -> modelMapper.map(task, TaskDto.class));
    }

    /**
     * Retrieves tasks by status with pagination support.
     *
     * @param pageable The pagination information (e.g., page number, size).
     * @param status The status of the tasks.
     * @return A {@link Page} of {@link TaskDto} representing all tasks.
     */
    public Page<TaskDto> findByStatus(String status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable)
                .map(task -> modelMapper.map(task, TaskDto.class));
    }
    /**
     * Retrieves tasks created by a specific user, with pagination support.
     *
     * @param idCreator The ID of the creator of the tasks.
     * @param pageable The pagination information (e.g., page number, size).
     * @return A {@link Page} of {@link TaskDto} representing the tasks created by the specified user.
     */
    public Page<TaskDto> findAllByCreator(UUID idCreator, Pageable pageable) {
        return taskRepository.findAllByCreatorId(idCreator, pageable)
                .map(task -> modelMapper.map(task, TaskDto.class));
    }

    /**
     * Retrieves tasks assigned to a specific executor, with pagination support.
     *
     * @param idExecutor The ID of the executor of the tasks.
     * @param pageable The pagination information (e.g., page number, size).
     * @return A {@link Page} of {@link TaskDto} representing the tasks assigned to the specified executor.
     */
    public Page<TaskDto> findAllByExecutor(UUID idExecutor, Pageable pageable) {
        return taskRepository.findAllByExecutorId(idExecutor, pageable)
                .map(task -> modelMapper.map(task, TaskDto.class));
    }

}
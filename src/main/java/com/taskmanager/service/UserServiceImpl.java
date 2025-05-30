package com.taskmanager.service;

import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Class UserServiceImpl
 *
 * Service implementation for managing users, including retrieving user details by email.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves a user by their email.
     *
     * @param login The email of the user to retrieve.
     * @return An {@link Optional} containing the {@link User} if found, or an empty {@link Optional} if not found.
     */
    @Override
    public Optional<User> getUserByEmail(String login) {
        return userRepository.findByEmail(login);
    }
}

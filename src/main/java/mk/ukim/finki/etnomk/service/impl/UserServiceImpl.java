package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Role;
import mk.ukim.finki.etnomk.model.User;
import mk.ukim.finki.etnomk.repository.UserRepository;
import mk.ukim.finki.etnomk.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Only set default role if not already set
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }

        return userRepository.save(user);
    }
}

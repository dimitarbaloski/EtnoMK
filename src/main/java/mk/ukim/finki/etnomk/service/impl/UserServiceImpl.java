package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.User;
import mk.ukim.finki.etnomk.service.UserService;

import java.util.Optional;

public class UserServiceImpl implements UserService {
    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public User register(User user) {
        return null;
    }
}

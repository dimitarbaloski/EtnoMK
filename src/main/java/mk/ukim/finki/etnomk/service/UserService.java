package mk.ukim.finki.etnomk.service;
import mk.ukim.finki.etnomk.model.User;

import java.util.Optional;


public interface UserService {
    public Optional<User> findByUsername(String username);
    public User register(User user);
}

package musicApp.services;

import musicApp.model.user.User;
import musicApp.repository.UserRepository;
import musicApp.utils.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO handleLogin(UserDTO loginData) {
        User user = userRepository.findByUsername(loginData.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect"));

        if (!passwordEncoder.matches(loginData.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect");
        }

        return new UserDTO(
                user.getId().toString(),
                user.getUsername(),
                null
        );
    }
}
package musicApp.controllers;
import jakarta.validation.Valid;
import musicApp.services.UserService;
import musicApp.utils.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/music/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDTO userDTO) {
        UserDTO responseUser = userService.handleLogin(userDTO);
        return ResponseEntity.ok(responseUser);
    }

}

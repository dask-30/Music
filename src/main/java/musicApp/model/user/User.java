package musicApp.model.user;


import jakarta.persistence.*;
import musicApp.model.BaseEntity;
import musicApp.model.structural.Score;

import java.util.List;

@Entity
@Table(name="users")
public class User extends BaseEntity {

    private String username;
    private String password;

    public User(Long id, String username, String password) {
        super(id);
        this.username = username;
        this.password = password;
    }
    public User() {
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

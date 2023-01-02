package antifraud.business;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table
public class UserParameters {
    @Id
    @JsonIgnore
    @Column
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    @NotNull
    private String name;

    @Column
    @NotNull
    private String username;

    @Column
    @NotNull
    private String password;

    @Column
    @NotNull
    private String role;

    @Column
    private String status;


    public UserParameters(String name, String username, String password, String role, String status) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public UserParameters() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @JsonPropertyOrder({"id", "name", "username", "role"})
    public class UserViewerId {

        public String getName() {
            return UserParameters.this.name;
        }

        public String getUsername() {
            return UserParameters.this.username;
        }

        public Long getId() {
            return UserParameters.this.userId;
        }

        public String getRole() {
            return UserParameters.this.role;
        }

    }

    @JsonPropertyOrder ({"username", "role"})
    public class UserViewer {

        public String getUsername() {
            return UserParameters.this.username;
        }

        public String getRole() {
            return UserParameters.this.role;
        }

    }
}

package antifraud.business;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sun.istack.NotNull;

import javax.persistence.*;

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

    public UserParameters(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
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

    @JsonPropertyOrder({"id", "name", "username"})
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

    }

    @JsonPropertyOrder({"name", "username"})
    public class UserViewer {

        public String getName() {
            return UserParameters.this.name;
        }

        public String getUsername() {
            return UserParameters.this.username;
        }

    }
}

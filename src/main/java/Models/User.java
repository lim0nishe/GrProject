package Models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;


@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String password;

    @ManyToOne
    private FTPServer server;

    public User(Long id, String name, String password, String serverUrl) {
        this.id = id;
        this.name = name;
        this.password = password;
    }
    public User(){}

    public FTPServer getServer() { return server;    }
    public void setServer(FTPServer server) { this.server = server;    }

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}

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

    @Column(unique = true)
    private String name;

    private String password;
    private String quota;

    @ManyToOne()
    @JoinColumn(name = "server_id")
    private FTPServer server;

    public User(String name, String password, FTPServer server, String quota) {
        this.server = server;
        this.name = name;
        this.password = password;
        this.quota = quota;
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

    public String getQuota(){ return quota; }
    public void setQuota(String quota){ this.quota = quota; }
}

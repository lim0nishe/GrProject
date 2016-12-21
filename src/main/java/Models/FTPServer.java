package Models;

import javax.persistence.*;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Lem0n on 19.11.2016.
 */

@Entity
public class FTPServer implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int port;
    private String address;
    private String adminName;
    private String adminPass;

    @OneToMany
    private Set<User> users;

    public FTPServer(){}

    public FTPServer(int port, String address, String adminName, String adminPass){
        this.port = port;
        this.address = address;
        this.adminName = adminName;
        this.adminPass = adminPass;
    }

    public Set<User> getUsers() { return users;    }
    public void setUsers(Set<User> users) { this.users = users;    }

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public int getPort(){
        return port;
    }
    public void setPort(int port){
        this.port = port;
    }

    public String getAddress(){
        return address;
    }
    public void setAddress(String address){
        this.address = address;
    }

    public String getAdminName(){
        return adminName;
    }
    public void setAdminName(String adminName){
        this.adminName = adminName;
    }

    public String getAdminPass(){
        return adminPass;
    }
    public void setAdminPass(String adminPass){
        this.adminPass = adminPass;
    }
}

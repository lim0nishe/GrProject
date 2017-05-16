package Models;


import org.apache.log4j.Logger;

import javax.persistence.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity
@Table(name = "sessions")
public class FrontEndSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long startTime;
    private Long endTime;
    private String address;
    private String hash;

    public FrontEndSession(){}
    public FrontEndSession(Long startTime, Long endTime, String address, String hash){
        this.startTime = startTime;
        this.endTime = endTime;
        this.address = address;
        this.hash = hash;
    }

    public void setId(Long id){
        this.id = id;
    }
    public Long getId(){
        return id;
    }

    public void setStartTime(Long startTime){
        this.startTime = startTime;
    }
    public Long getStartTime(){
        return startTime;
    }

    public void setEndTime(Long endTime){
        this.endTime = endTime;
    }
    public Long getEndTime(){
        return endTime;
    }

    public void setAddress(String address){
        this.address = address;
    }
    public String getAddress(){
        return address;
    }

    public void setHash(String hash){
        this.hash = hash;
    }
    public String getHash(){
        return hash;
    }


}

package Service;


import Models.FrontEndSession;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.codec.digest.Crypt;

public class FrontEndSessionController {

    private static Logger logger = Logger.getLogger("file");
    private static int BASE_LENGTH = 56;
    private static int HASH_LENGTH = 56;

    String crypt(){

        // generate random string
        StringBuilder base = new StringBuilder();
        String alphabet = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
        for (int i = 0; i < BASE_LENGTH; i++){
            base.append(alphabet.charAt(i));
        }
        return (Crypt.crypt(base.toString())).substring(0, HASH_LENGTH);

    }

//    String md5Hash(String st){
//
//        MessageDigest messageDigest;
//        byte[] digest = new byte[0];
//        try{
//            messageDigest = MessageDigest.getInstance("MD5");
//            messageDigest.reset();
//            messageDigest.update(st.getBytes());
//            digest = messageDigest.digest();
//        } catch (NoSuchAlgorithmException e) {
//            logger.error("error in md5 hash algorithm");
//            e.printStackTrace();
//        }
//
//        BigInteger bigInt = new BigInteger(1, digest);
//        String md5Hex = bigInt.toString(16);
//
//        while( md5Hex.length() < 32 ){
//            md5Hex = "0" + md5Hex;
//        }
//        return md5Hex;
//    }

    public boolean isActive(String address, String sessionToken){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Long currentTime = System.currentTimeMillis();

        String hql = "FROM FrontEndSession S WHERE S.address = :s_address";
        Query query = session.createQuery(hql);
        query.setParameter("s_address", address);
        List<FrontEndSession> result = query.list();
        for (FrontEndSession entry : result){
            if ((currentTime < entry.getEndTime()) && (sessionToken.equals(entry.getHash()))
                    && (currentTime >= entry.getStartTime())){
                session.close();
                return true;
            }
        }
        session.close();
        return false;
    }
    public String activateSession(String address, String login, String password){
        try {
            Properties properties = new Properties();


            //Tomcat
            properties.loadFromXML(new FileInputStream("/TomcatFiles/myProperties.properties"));

            //WildFly
            //properties.loadFromXML(new FileInputStream(System.getProperty("jboss.server.data.dir") +
            //        "/myProperties.properties"));

            if((!login.equals(properties.getProperty("frontend.login"))) ||
                    (!password.equals(properties.getProperty("frontend.password"))))
                return JsonResponse.AUTHORIZATION_ERROR;

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            Long startTime = System.currentTimeMillis();
            Long endTime = startTime + Long.parseLong(properties.getProperty("SessionTime"));
            String hash = crypt();

            if (isActive(address, hash))
                return JsonResponse.TOKEN_EXISTS_ERROR;

            FrontEndSession FESession = new FrontEndSession(startTime, endTime, address, hash);


            session.save(FESession);
            session.getTransaction().commit();
            session.close();
            return JsonResponse.createAuthorizationResponse(hash);
        }
        catch (Exception e){
            logger.error("cant find properties file");
            e.printStackTrace();
            return JsonResponse.AUTHORIZATION_ERROR;
        }
    }
    public String deactivateSession(String address, String sessionToken){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        String hql = "FROM FrontEndSession S WHERE S.address = :s_address";
        Query query = session.createQuery(hql);
        query.setParameter("s_address", address);
        FrontEndSession result = (FrontEndSession)query.uniqueResult();

        if (result == null) {
            logger.info("session is already inactive");
            return JsonResponse.DEACTIVATION_SUCCESS;
        }

        if (!sessionToken.equals(result.getHash()))
            return JsonResponse.AUTHORIZATION_ERROR;
        session.delete(result);
        session.getTransaction().commit();
        session.close();
        logger.info("session deactivation completed");
        return JsonResponse.DEACTIVATION_SUCCESS;
    }

    static public void killExpiredSessons(){

        // using for Quartz job
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        Long currentTime = System.currentTimeMillis();

        Query query = session.createQuery("DELETE FrontEndSession S WHERE S.endTime <= :cur_time");
        query.setParameter("cur_time", currentTime);
        query.executeUpdate();

        session.getTransaction().commit();
        session.close();
    }
}

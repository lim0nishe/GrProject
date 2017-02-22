package Service;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StringValidator {

    private Pattern passwordPattern = Pattern.compile("^[a-zA-Z0-9_-]{8,}$");
    private Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{1,}$");

    boolean validateUserName(String username){
        return usernamePattern.matcher(username).matches();
    }

     boolean validatePassword(String password){
        return passwordPattern.matcher(password).matches();
    }
}

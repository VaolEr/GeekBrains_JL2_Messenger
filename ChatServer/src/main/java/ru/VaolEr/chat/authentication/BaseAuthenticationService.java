package ru.VaolEr.chat.authentication;

import ru.VaolEr.chat.User;
import ru.VaolEr.chat.util.DateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseAuthenticationService implements AuthenticationService {

//    private static final Map<String, String> USERS= new HashMap<>(){{
//        put("L1","P1");
//        put("L2","P2");
//        put("L3","P3");
//    }};

    private static final List<User> USERS = List.of(
            new User("Strix","Strix", "Strix"),
            new User("Qwerty","Qwerty", "Qwerty"),
            new User("SieveRT","SieveRT", "SieveRT"),
            new User("Zg","Zg", "ZloyGlagol"),
            new User("Lalka","Lalka", "Lalka")
    );

    @Override
    public void start() {
        System.out.println(DateUtil.getCurrentLocalTime() + " ~ Authentication has been started... ~");
        //TODO connect with database here
    }


    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User user : USERS) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user.getUsername();
            }
        }
        return null; // bad solve
    }

    @Override
    public void stop() {
        System.out.println(DateUtil.getCurrentLocalTime() + " ~ Authentication has been finished. ~");
        //TODO disconnect from database here
    }
}

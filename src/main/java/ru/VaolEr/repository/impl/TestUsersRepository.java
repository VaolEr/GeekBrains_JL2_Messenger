package ru.VaolEr.repository.impl;

import ru.VaolEr.model.User;
import ru.VaolEr.repository.UsersRepository;

import java.util.List;

public class TestUsersRepository implements UsersRepository {
    @Override
    public List<User> getAllData() {
        return List.of(
                new User("Strix","str"),
                new User("Qwerty","str"),
                new User("Lalka","str"),
                new User("SieveRT","str"),
                new User("ZloyGlagol","str")
        );
    }
}

package com.uap.proiv.jobs.service.impl;

import com.uap.proiv.jobs.client.UserApiRepository;
import com.uap.proiv.jobs.dto.User;
import com.uap.proiv.jobs.dto.UserApiResponse;
import com.uap.proiv.jobs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserApiRepository userApiRepository;

    @Autowired
    public UserServiceImpl(UserApiRepository userApiRepository) {
        this.userApiRepository = userApiRepository;
    }


    @Override
    public UserApiResponse search(int page) {
        return userApiRepository.getUsers(page);
    }

    @Override
    public User searchById(int id) {
        return userApiRepository.getUserById(id);
    }

    @Override
    public void update(User user) {
        try {
            userApiRepository.updateUser( user);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage(), e);
        }
    }
}

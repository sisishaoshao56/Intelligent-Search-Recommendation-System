package com.csu.demo.demo.service;

import com.csu.demo.demo.domain.User;

public interface LoginService {

    public String register(User user);
    public String login(String username, String password);
    public void logout(String token);
}
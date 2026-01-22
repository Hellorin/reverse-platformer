package com.example.tempname.config;

import com.example.tempname.application.ports.in.UserUseCase;
import com.example.tempname.application.ports.out.UserRepository;
import com.example.tempname.application.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    public UserUseCase userUseCase(UserRepository userRepository) {
        return new UserService(userRepository);
    }
}

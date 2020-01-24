package com.inspire.development.admin.jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if ("admin".equals(username)) {
                String passwordHash = "";
                try {
                    byte[] encoded = Files.readAllBytes(Paths.get("./config/admin.pw"));
                    passwordHash =  new String(encoded, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    passwordHash = "$2y$10$HRv1s8OTjtj15v0OKbVzwemobM84SfXJml1kT/TDdrqoAGZ/Kw8iS";
                }
                return new User("admin", passwordHash,
                        new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
package com.inventra.security;

import com.inventra.entity.User;
import com.inventra.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = repo.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = user.getRole().name();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return new org.springframework.security.core.userdetails.User(
                username,
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
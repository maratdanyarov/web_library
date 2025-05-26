package com.mdanyarov.weblibrary.security;

import com.mdanyarov.weblibrary.entity.User;
import com.mdanyarov.weblibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Custom UserDetailsService for Spring Security integration.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(@Lazy UserService userService) {
        this.userService = userService;
    }

    /**
     * Loads the user details by username for authentication and authorization purposes.
     *
     * @param username the username identifying the user whose data is required
     * @return a fully populated object containing user credentials and roles
     * @throws UsernameNotFoundException if the user cannot be found or is blocked, or if an error occurs while loading user details
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                throw new UsernameNotFoundException("User not found " + username);
            }

            User user = userOptional.get();

            if (user.getStatus() == User.UserStatus.BLOCKED) {
                throw new UsernameNotFoundException("User account is blocked: " + username);
            }

            return new CustomUserPrincipal(user);
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error loading user " + username, e);
        }
    }

    /**
     * Custom UserDetails implementation
     */
    public record CustomUserPrincipal(User user) implements UserDetails {

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() == User.UserStatus.ACTIVE;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getStatus() == User.UserStatus.ACTIVE;
        }
    }
}

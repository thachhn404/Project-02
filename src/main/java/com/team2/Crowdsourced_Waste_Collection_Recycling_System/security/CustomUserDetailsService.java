package com.team2.Crowdsourced_Waste_Collection_Recycling_System.security;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai UserDetailsService của Spring Security.
 * Cung cấp phương thức để tải thông tin người dùng từ database dựa trên email.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Tìm kiếm người dùng trong database theo email.
     * @param email Email đăng nhập.
     * @return UserDetails đối tượng chứa thông tin bảo mật của người dùng.
     * @throws UsernameNotFoundException Nếu không tìm thấy email.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }
}


package com.ecommerce.authserver.service;

import com.ecommerce.authserver.dto.UserRegistrationDTO;
import com.ecommerce.authserver.model.User;

public interface UserService {
    User registerUser(UserRegistrationDTO registrationDTO);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

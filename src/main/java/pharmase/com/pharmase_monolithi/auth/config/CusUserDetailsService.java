package pharmase.com.pharmase_monolithi.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pharmase.com.pharmase_monolithi.auth.model.employees.ShopEmployee;
import pharmase.com.pharmase_monolithi.auth.model.owner.ShopOwner;
import pharmase.com.pharmase_monolithi.auth.repository.employee.EmployeeRepository;
import pharmase.com.pharmase_monolithi.auth.repository.owner.OwnerRepository;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class CusUserDetailsService implements UserDetailsService {
    @Autowired
    EmployeeRepository employeeRepo;
    @Autowired
    OwnerRepository ownerRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Optional<ShopOwner> owner = ownerRepo.findByEmailIgnoreCase(normalizedEmail);
        if (owner.isPresent()) {
            log.info("Email {} is present in the Owner table.", email);
            return new User(owner.get().getEmail(),
                    owner.get().getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_OWNER")));
        }
        Optional<ShopEmployee> employee = employeeRepo.findByEmailIgnoreCase(normalizedEmail);
        if (employee.isPresent()) {
            log.info("Email {} is present in the Employee table.", email);
            return new User(employee.get().getEmail(),
                    employee.get().getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        }
        log.info("Authentication failed: email {} was not found in any table.", email);
        throw new UsernameNotFoundException("The email (" + email + ") is not valid for authentication.");
    }
}

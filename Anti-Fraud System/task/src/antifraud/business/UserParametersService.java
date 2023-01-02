package antifraud.business;


import antifraud.persistence.UserRepository;
import antifraud.security.SecurityParams;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserParametersService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserParametersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    PasswordEncoder passwordEncoder;

    public UserParameters encodePassword(UserParameters userParameters) {
        userParameters.setPassword(passwordEncoder.encode(userParameters.getPassword()));
        return userParameters;
    }
    public UserParameters save(UserParameters userParameters) {
        UserParameters userParameters1 = userRepository.save(userParameters);
        return userParameters1;
    }

    public UserParameters findByUsername(String name) {
        UserParameters userParameters1 = userRepository.findUserParametersByUsername(name);
        return userParameters1;
    }

    public List<UserParameters> findAll() {
        return userRepository.findAll();
    }

    public String delete(String username) {
        return userRepository.deleteByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserParameters userParameters = userRepository.findUserParametersByUsername(username);
        if (userParameters == null) {
            throw new UsernameNotFoundException(username);
        }
        User user = new User(userParameters.getUsername(), userParameters.getPassword(), SecurityParams.ROLE_USER);
        return user; //new MyUserPrincipal(user);
    }

    public UserParameters saveFirstTime(UserParameters userParameters) {
        List<UserParameters> list = findAll();
        if ( list.size() == 0) {
            userParameters.setRole(SecurityParams.ADMINISTRATOR);
            userParameters.setStatus(SecurityParams.UNLOCKED);
        }
        else {
            userParameters.setRole(SecurityParams.MERCHANT);
            userParameters.setStatus(SecurityParams.LOCKED);
        }
        userParameters.setPassword(passwordEncoder.encode(userParameters.getPassword()));
        return save(userParameters);
    }
}

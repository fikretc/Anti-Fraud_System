package antifraud.business;


import antifraud.persistence.UserRepository;
import antifraud.security.SecurityConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class UserParametersService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final List<GrantedAuthority> ROLE_USER = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList("ROLE_USER"));

    @Autowired
    public UserParametersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    PasswordEncoder passwordEncoder;

    public UserParameters save(UserParameters userParameters) {
        userParameters.setPassword(passwordEncoder.encode(userParameters.getPassword()));
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
        User user = new User(userParameters.getUsername(), userParameters.getPassword(),  ROLE_USER);
        return user; //new MyUserPrincipal(user);

    }







}

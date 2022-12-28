package antifraud.persistence;

import antifraud.business.UserParameters;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserRepository extends CrudRepository <UserParameters, Integer> {
    UserParameters findUserParametersByUserId(Integer userId);

    UserParameters findUserParametersByUsername(String username);

    List<UserParameters> findAll();
    UserParameters save(UserParameters toSave);

    String deleteByUsername(String username);
}

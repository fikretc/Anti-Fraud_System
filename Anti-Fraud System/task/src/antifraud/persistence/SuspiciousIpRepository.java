package antifraud.persistence;

import antifraud.business.SuspiciousIp;
import antifraud.business.UserParameters;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspiciousIpRepository extends CrudRepository <SuspiciousIp, Long> {
    SuspiciousIp findSuspiciousIpByIp (String ip);

    List<SuspiciousIp> findAll();
}

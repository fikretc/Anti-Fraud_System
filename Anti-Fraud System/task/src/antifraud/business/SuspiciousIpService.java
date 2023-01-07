package antifraud.business;

import antifraud.persistence.SuspiciousIpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SuspiciousIpService {

    private final SuspiciousIpRepository suspiciousIpRepository;

    @Autowired
    public SuspiciousIpService(SuspiciousIpRepository suspiciousIpRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
    }

    public SuspiciousIp save(SuspiciousIp ip) {
        return suspiciousIpRepository.save(ip);
    }

    public SuspiciousIp findByIp(String ip) {
        return suspiciousIpRepository.findSuspiciousIpByIp(ip);
    }

    public List<SuspiciousIp> findAll() {
        return suspiciousIpRepository.findAll();
    }

    public void delete(SuspiciousIp ip) {
        suspiciousIpRepository.delete(ip);
    }
}

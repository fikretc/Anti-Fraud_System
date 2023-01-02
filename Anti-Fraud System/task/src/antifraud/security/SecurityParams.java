package antifraud.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.*;

public final class SecurityParams {

    public static final String LOCKED = "locked";
    public static final String UNLOCKED = "unlocked";
    public static final String OP_LOCK = "LOCK";
    public static final String OP_UNLOCK = "UNLOCK";
    private SecurityParams() {}

    public static final String USER = "USER";
    public static final String ANONIMOUS = "ANONIMOUS";
    public static final String MERCHANT = "MERCHANT";
    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String SUPPORT = "SUPPORT";

    public static final List<String> roleList = Arrays.asList (MERCHANT, SUPPORT, ADMINISTRATOR);


    public static final List<GrantedAuthority> ROLE_USER = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList(USER));

    public static final List<GrantedAuthority> ROLE_ANONIMOUS = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList("ANONIMOUS"));

    public static final List<GrantedAuthority> ROLE_MERCHANT = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList("MERCHANT"));

    public static final List<GrantedAuthority> ROLE_ADMINISTRATOR = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList("ADMINISTRATOR"));

    public static final List<GrantedAuthority> ROLE_SUPPORT = Collections
            .unmodifiableList(AuthorityUtils.createAuthorityList("SUPPORT"));

    public static final List<GrantedAuthority> grants ( String role) {
        switch (role) {
            case USER -> {
                return ROLE_USER;
            }
            case ANONIMOUS -> {
                return ROLE_ANONIMOUS;
            }
            case MERCHANT -> {
                return ROLE_MERCHANT;
            }
            case ADMINISTRATOR -> {
                return ROLE_ADMINISTRATOR;
            }
            case SUPPORT -> {
                return ROLE_SUPPORT;
            }
        }
        return null;
    }
}

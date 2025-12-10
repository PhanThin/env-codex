package vn.com.viettel.auth.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public class SecurityContextUtils {

    public static Jwt getJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean isMasterRealm() {
        return "master".equals(getAuthRealm());
    }

    public static String getUserId() {
        return getJwt().getClaimAsString("sub");
    }

    public static String getUsername() {
        return getJwt().getClaimAsString("preferred_username");
    }

    public static String getClientId() {
        return getJwt().getClaimAsString("azp");
    }

    public static String getClaim(String claim) {
        return getJwt().getClaimAsString(claim);
    }
    public static Map<String, Object> getClaims() {
        return getJwt().getClaims();
    }

    public static String getAuthRealm() {
        return getJwt().getClaimAsString("iss").substring(getJwt().getClaimAsString("iss").lastIndexOf("/") + 1);
    }

    public static String getStringToken() {
        return getJwt().getTokenValue();
    }

    public static List<String> getClients() {
        return getJwt().getClaim("aud");
    }

    public static Map<String, Object> getClientRoles() {
        return SecurityContextUtils.getJwt().getClaimAsMap("resource_access");
    }

    public static Map<String, Object> getRealmRoles() {
        return SecurityContextUtils.getJwt().getClaimAsMap("realm_access");
    }

    public static SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }

    public static boolean checkAnonymousAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication().getClass().equals(AnonymousAuthenticationToken.class);
    }

    public static boolean isUsernamePasswordAuthenticationToken() {
        return SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken;
    }
}

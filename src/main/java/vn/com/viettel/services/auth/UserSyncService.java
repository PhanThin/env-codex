package vn.com.viettel.services.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.viettel.constant.UserType;
import vn.com.viettel.entities.SysUser;
import vn.com.viettel.repositories.jpa.SysUserRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final SysUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public SysUser syncUserFromPayload(String username, String accessToken) throws Exception {
        JsonNode payload = decodeJwtPayload(accessToken);

        return userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseGet(() -> createNewUser(username, payload));
    }

    private SysUser createNewUser(String username, JsonNode payload) {
        String fullName = payload.has("name") ? payload.get("name").asText() : username;
        Integer jwtUserType = payload.has("user_type") ? payload.get("user_type").asInt() : UserType.INTERNAL.getValue();

        SysUser newUser = new SysUser();
        newUser.setUsername(username);
        newUser.setIsActive(true);
        newUser.setFullName(fullName);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setType(jwtUserType);
        newUser.setIsDeleted(false);
        return userRepository.save(newUser);
    }

    public JsonNode decodeJwtPayload(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        Map<String, Object> claims = signedJWT.getJWTClaimsSet().getClaims();
        return objectMapper.valueToTree(claims);
    }
}
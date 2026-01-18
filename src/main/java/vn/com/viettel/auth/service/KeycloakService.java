package vn.com.viettel.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.com.viettel.auth.config.ExtendedKeycloakProperties;
import vn.com.viettel.auth.dto.KeycloakTokenResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {

    private final CloseableHttpClient httpClient;
    private final ExtendedKeycloakProperties keycloakProperties;
    private final ObjectMapper objectMapper;
    private final Keycloak keycloak;

    // Hàm dùng chung để thực hiện các yêu cầu POST (Khử lặp mã nguồn)
    private KeycloakTokenResponse executePost(List<NameValuePair> params, String url) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8)); // Nên chỉ định rõ bảng mã

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            // Bước 1: Kiểm tra nếu không có nội dung (thường là 204 Logout thành công)
            if (entity == null || statusCode == 204) {
                return KeycloakTokenResponse.builder()
                        .statusCode(statusCode)
                        .build();
            }

            // Bước 2: Chỉ đọc body khi entity không null
            String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            // Đề phòng trường hợp body rỗng nhưng entity không null
            if (body == null || body.trim().isEmpty()) {
                return KeycloakTokenResponse.builder()
                        .statusCode(statusCode)
                        .build();
            }

            KeycloakTokenResponse tokenResponse = objectMapper.readValue(body, KeycloakTokenResponse.class);
            tokenResponse.setStatusCode(statusCode);
            return tokenResponse;
        }
    }

    public KeycloakTokenResponse login(String username, String password) throws IOException {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloakProperties.getAuthServerUrl(), keycloakProperties.getRealm());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "password"));
        params.add(new BasicNameValuePair("client_id", keycloakProperties.getClientId()));
        params.add(new BasicNameValuePair("client_secret", keycloakProperties.getClientSecret()));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));

        return executePost(params, url);
    }

    public KeycloakTokenResponse logout(String refreshToken) throws IOException {
        String url = String.format("%s/realms/%s/protocol/openid-connect/logout",
                keycloakProperties.getAuthServerUrl(), keycloakProperties.getRealm());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", keycloakProperties.getClientId()));
        params.add(new BasicNameValuePair("client_secret", keycloakProperties.getClientSecret()));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));

        return executePost(params, url);
    }

    public KeycloakTokenResponse refreshToken(String refreshToken) throws IOException {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token",
                keycloakProperties.getAuthServerUrl(), keycloakProperties.getRealm());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("client_id", keycloakProperties.getClientId()));
        params.add(new BasicNameValuePair("client_secret", keycloakProperties.getClientSecret()));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));

        return executePost(params, url);
    }

    /**
     * API Reset Password - Sử dụng Admin Client để đổi mật khẩu cho User
     */
    public void resetPassword(String userId, String newPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false); // Đặt là false để user dùng được ngay không cần đổi lại

        keycloak.realm(keycloakProperties.getRealm())
                .users()
                .get(userId)
                .resetPassword(credential);
    }

    /**
     * Lấy ID người dùng từ username
     */
    public String getUserIdByUsername(String username) {
        List<UserRepresentation> users = keycloak.realm(keycloakProperties.getRealm()).users().search(username, true);
        return users.isEmpty() ? null : users.getFirst().getId();
    }

    /**
     * Yêu cầu cập nhật mật khẩu lần đầu/hết hạn
     */
    public void forcePasswordUpdate(String userId) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setRequiredActions(Collections.singletonList("UPDATE_PASSWORD"));
        keycloak.realm(keycloakProperties.getRealm()).users().get(userId).update(userRep);
    }

    /**
     * Lấy thông tin Brute Force của người dùng
     */
    public Map<String, Object> getBruteForceStatus(String userId) {
        return keycloak.realm(keycloakProperties.getRealm()).attackDetection().bruteForceUserStatus(userId);
    }

    /**
     * Xóa trạng thái Brute Force
     */
    public void clearBruteForce(String userId) {
        keycloak.realm(keycloakProperties.getRealm()).attackDetection().clearBruteForceForUser(userId);
    }
}
package vn.com.viettel.auth.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.com.viettel.core.dto.request.Jwt;

public class AuthUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy token từ request
     *
     * @param request
     * @return
     */
    public static String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    /**
     * Get data user from Jwt
     *
     * @param jwtToken
     * @return
     */
    public static Jwt getDataFromJwt(String jwtToken) {
        try {
            String[] split_string = jwtToken.split("\\.");
            if (split_string.length == 0) {
                return null;
            }
            String base64EncodedBody = split_string[1];
            Base64 base64Url = new Base64(true);
            String body = new String(base64Url.decode(base64EncodedBody));
            return (Jwt) convertJsonToObject(body, Jwt.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param strJsonData
     * @param classOfT
     * @return
     */
    public static Object convertJsonToObject(String strJsonData, Class<?> classOfT) {
        try {
            return objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(strJsonData, classOfT);
        } catch (JsonProcessingException e) {
            LOGGER.error("Has error convertJsonToObject: ", e);
        }
        return null;
    }

    /**
     * Get IP
     * @return
     */
    public static String getClientIpAddress() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getRemoteAddr();
        }
        return null;
    }
}

package vn.com.viettel.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import vn.com.viettel.auth.utils.SecurityContextUtils;

public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        var auth = requestTemplate.headers().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(auth)) return;

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return;

        var token = SecurityContextUtils.getStringToken();
        requestTemplate.header(HttpHeaders.AUTHORIZATION, String.format("%s %s", "Bearer", token));
    }
}

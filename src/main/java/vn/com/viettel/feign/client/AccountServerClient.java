package vn.com.viettel.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.com.viettel.core.dto.response.BaseResponse;
import vn.com.viettel.feign.configuration.FeignConfiguration;


@FeignClient(value = "account-server-client", url = "${feign.client.service.account.url}", configuration = FeignConfiguration.class)
public interface AccountServerClient {
    @GetMapping(value = "/api/v1/groups")
    BaseResponse getAllKeycloakGroups(@RequestHeader(value = "Authorization") String token);

    @GetMapping(value = "/api/v1/groups/children-group/by-token")
    BaseResponse getKeycloakChildrenGroup(@RequestHeader(value = "Authorization") String token);

    @GetMapping(value =  "/api/v1/realms/{realmName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse getRealmByName(@RequestHeader(value = "Authorization") String token,
                            @PathVariable(value = "realmName") String realmName);
}

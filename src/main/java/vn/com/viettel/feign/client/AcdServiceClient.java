package vn.com.viettel.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import vn.com.viettel.core.dto.response.BaseResponse;
import vn.com.viettel.feign.configuration.FeignConfiguration;


@FeignClient(value = "acd-service-client", url = "${feign.client.service.acd.url}", configuration = FeignConfiguration.class)
public interface AcdServiceClient {
    @GetMapping(value = "/api/v1/queue-supervisor/{agentId}")
    BaseResponse getQueuesOfAgent(@RequestHeader(value = "Authorization") String token,
                                  @PathVariable(value = "agentId") String agentId);

    @GetMapping(value = "/api/v1/queue-supervisor/can-supervise")
    BaseResponse canSupervise(@RequestHeader(value = "Authorization") String token,
                          @RequestParam(name = "agentId") String agentId,
                          @RequestParam(name = "queueId") String queueId);

    @GetMapping(value = "/api/v1/agents/can-transfer-to")
    BaseResponse canTransferTicketTo(@RequestHeader(value = "Authorization") String token,
                                 @RequestParam(name = "agentId") String agentId,
                                 @RequestParam(name = "isReply") boolean isReply);
}

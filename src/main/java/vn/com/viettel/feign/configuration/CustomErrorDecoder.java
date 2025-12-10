package vn.com.viettel.feign.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import feign.codec.ErrorDecoder;
import jakarta.ws.rs.BadRequestException;
import vn.com.viettel.core.dto.response.BaseResponse;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, feign.Response response) {
        try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            BaseResponse res = mapper.readValue(CharStreams.toString(reader), BaseResponse.class);
            switch (response.status()) {
                case 400:
                    return new BadRequestException(res.getMessage());
                default:
                    return errorDecoder.decode(methodKey, response);
            }
        } catch (IOException e) {
            return new Exception(e.getMessage());
        }
    }
}

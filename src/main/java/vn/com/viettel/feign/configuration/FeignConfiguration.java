package vn.com.viettel.feign.configuration;

import feign.Client;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.FormEncoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class FeignConfiguration {
    private final ObjectFactory<HttpMessageConverters> messageConverters;
    @Value("${spring.proxy.host:}")
    private String proxyHost;

    @Value("${spring.proxy.port:0}")
    private int proxyPort;

    public FeignConfiguration(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Bean
    public Client feignClient() {
        if (!StringUtils.isEmpty(proxyHost)) {
            return new Client.Proxied(null, null,
                    new Proxy(Proxy.Type.HTTP,
                            new InetSocketAddress(proxyHost, proxyPort)));
        } else {
            return new Client.Default(null, null);
        }
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Decoder feignDecoder() {
        return new SpringDecoder(this.messageConverters);
    }

    @Bean
    @Primary
    @Scope("prototype")
    Encoder feignFormEncoder() {
        return new FormEncoder(new SpringEncoder(this.messageConverters));
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}

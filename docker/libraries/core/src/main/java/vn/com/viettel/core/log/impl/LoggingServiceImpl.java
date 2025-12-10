package vn.com.viettel.core.log.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.com.viettel.core.dto.request.Jwt;
import vn.com.viettel.core.dto.response.BaseResponse;
import vn.com.viettel.core.log.LoggingService;
import vn.com.viettel.core.utils.CoreUtils;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoggingServiceImpl implements LoggingService {

    @Value("${app.code:N/A}")
    String code;

    @Value("${app.service.code:N/A}")
    String serviceCode;

    @Value("${app.system.code:N/A}")
    String systemCode;

    @Value("${logs.kpi-logs.enabled:true}")
    boolean kpiLogEnabled;

    private static final String IP_PORT_SERVICE = getIpAddressAndPort_q();
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingServiceImpl.class);
    private static final Logger KPI_LOGGER = LoggerFactory.getLogger("KPI_LOG");
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS";

    @Override
    public void logRequest(HttpServletRequest httpServletRequest, Object body) {
        if (!kpiLogEnabled) {
            return;
        }

        Map<String, String> parameters = buildParametersMap(httpServletRequest);
        long startTime = System.currentTimeMillis();
        httpServletRequest.setAttribute("startDate", new Date());
        httpServletRequest.setAttribute("startTime", startTime);
        String jwt = CoreUtils.resolveToken(httpServletRequest);
        Jwt authenEntity = null;
        if (jwt != null && jwt.trim().length() > 0) {
            authenEntity = CoreUtils.getDataFromJwt(jwt);
        }

        // Thuc hien ghep cac truong theo  chuan logs tap trung
        StringBuilder strLogs = new StringBuilder();
        strLogs.append("SERVICEAPPLICATION_START|");
        strLogs.append(httpServletRequest.getRequestURI()).append("(").append(httpServletRequest.getMethod()).append(")|");

        // Application Code
        strLogs.append(code).append("|");

        // Service Code
        strLogs.append(serviceCode).append("|");

        // Thread ID
        strLogs.append(Thread.currentThread().getId()).append("|");

        // Request ID
        boolean isRequestId = httpServletRequest.getHeader("requestId") != null;
        if (isRequestId) {
            strLogs.append(httpServletRequest.getHeader("requestId"));
        }
        strLogs.append("|");

        // Session ID
        if (authenEntity != null) {
            strLogs.append(authenEntity.getSessionState());
        }
        strLogs.append("|");

        // IP Port ParentNode
        strLogs.append(fetchClientIpAddr(httpServletRequest)).append("|");

        // IP Port CurrentNode
        strLogs.append(IP_PORT_SERVICE.replace("/", "")).append("|");

        // Request Content Parameter
        String strParams = "";
        if (!parameters.isEmpty()) {
            strParams = parameters + ";";
        }
        if (body != null) {
            try {
                ObjectWriter ow = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                        .writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(body);
                strParams += json.replace("\n", "").replace("\r", "").trim().replaceAll(" +", " ");
            } catch (JsonProcessingException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        String[] arrString = strParams.split("\"");
        if (arrString.length > 0) {
            for (String string : arrString) {
                if (string != null && string.trim().length() > 200) {
                    boolean isBase64 = Base64.isBase64(string.getBytes());
                    if (isBase64 || string.trim().length() > 500) {
                        strParams = strParams.replace(string, "REPLACE CHUOI BASE 64 File");
                    }
                }
            }
        }
        httpServletRequest.setAttribute("requestParams", strParams);
        strLogs.append(strParams).append("|");

        // Start Time
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String strStartTime = sdf.format(new Date());
        strLogs.append(strStartTime).append("|");

        // End Time | Duration | ErrorCode | ErrorDescription | TransactionStatus | ActionName
        strLogs.append("||||||");

        // User Name
        if (authenEntity != null) {
            strLogs.append(authenEntity.getPreferredUsername());
        }
        strLogs.append("|");

        // Account
        strLogs.append("|");

        // Thread Name
        strLogs.append(Thread.currentThread().getName()).append("|");

        // Source.class
        strLogs.append("|");
        // Source.line
        strLogs.append("|");
        // Source.method
        strLogs.append("|");

        // Client Request Id
        boolean isClientMessageId = httpServletRequest.getHeader("clientMessageId") != null;
        if (isClientMessageId) {
            strLogs.append(httpServletRequest.getHeader("clientMessageId"));
        }
        strLogs.append("|");

        // Service Provider
        strLogs.append("|");

        // Client IP
        strLogs.append(fetchClientIpAddr(httpServletRequest)).append("|");

        // Transaction ID
        boolean isTransactionId = httpServletRequest.getHeader("transactionId") != null;
        if (isTransactionId) {
            strLogs.append(httpServletRequest.getHeader("transactionId"));
        }
        strLogs.append("|");

        // ResponseContent
        strLogs.append("|");

        // Transaction Type
        boolean isTransactionType = httpServletRequest.getHeader("transactionType") != null;
        if (isTransactionType) {
            strLogs.append(httpServletRequest.getHeader("transactionType"));
        } else {
            strLogs.append("01");
        }
        strLogs.append("|");

        // System
        strLogs.append(systemCode).append("|");

        // Action Type
        strLogs.append(getActionTypeByMethod(httpServletRequest.getMethod())).append("|");

        // Data Type
        strLogs.append("0|");

        // Num records: Số lượng bản ghi truy vấn
        strLogs.append("|");

        // VSA
        strLogs.append("|");

        // Data Extended
        strLogs.append("|");

        KPI_LOGGER.info(MarkerFactory.getMarker("KPI_LOG"), strLogs.toString());
    }

    @Override
    public void logResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object body) {
        if (!kpiLogEnabled) {
            return;
        }

        String strResponseHeader = buildHeadersMap(httpServletResponse).toString();
        //thuc hien ghep cac truong theo  chuan logs tap trung
        StringBuilder strLogs = new StringBuilder();
        String jwt = CoreUtils.resolveToken(httpServletRequest);
        Jwt authenEntity = null;
        if (jwt != null && jwt.trim().length() > 0) {
            authenEntity = CoreUtils.getDataFromJwt(jwt);
        }
        strLogs.append("SERVICEAPPLICATION_END|");
        strLogs.append(httpServletRequest.getRequestURI()).append("(").append(httpServletRequest.getMethod()).append(")|");

        // Application Code
        strLogs.append(code).append("|");

        // Service Code
        strLogs.append(serviceCode).append("|");

        // Thread ID
        strLogs.append(Thread.currentThread().getId()).append("|");

        // Request ID
        boolean isRequestId = httpServletRequest.getHeader("requestId") != null;
        if (isRequestId) {
            strLogs.append(httpServletRequest.getHeader("requestId"));
        }
        strLogs.append("|");

        // Session ID
        if (authenEntity != null) {
            strLogs.append(authenEntity.getSessionState());
        }
        strLogs.append("|");

        // IP Port ParentNode
        strLogs.append(fetchClientIpAddr(httpServletRequest)).append("|");

        // IP Port CurrentNode
        strLogs.append(IP_PORT_SERVICE.replace("/", "")).append("|");

        // Request content parameter
        if (httpServletRequest.getAttribute("requestParams") != null) {
            String strParams = httpServletRequest.getAttribute("requestParams").toString();
            strLogs.append(strParams).append("|");
        }

        // Start Time
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        if (httpServletRequest.getAttribute("startDate") != null) {
            Date startTime = (Date) httpServletRequest.getAttribute("startDate");
            String strStartTime = sdf.format(startTime);
            strLogs.append(strStartTime).append("|");
        }

        // End time
        String strEndTime = sdf.format(new Date());
        strLogs.append(strEndTime).append("|");

        // Duration
        if (httpServletRequest.getAttribute("startTime") != null) {
            long startTime = (Long) httpServletRequest.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            strLogs.append(endTime - startTime).append("|");
        }

        // ErrorCode | ErrorDescription | TransactionStatus
        if (body != null && body.getClass().equals(BaseResponse.class)) {
            // ErrorCode
            strLogs.append(((BaseResponse) body).getCode()).append("|");

            // ErrorDescription
            strLogs.append(((BaseResponse) body).getMessage()).append("|");

            // TransactionStatus
            strLogs.append("0|");
        } else {
            strLogs.append("|||");
        }

        // ActionName
        strLogs.append("|");

        // Username
        if (authenEntity != null) {
            strLogs.append(authenEntity.getPreferredUsername()).append("|");
        }

        // Account
        strLogs.append("|");

        // Thread Name
        strLogs.append(Thread.currentThread().getName()).append("|");

        // Source.class
        strLogs.append("|");
        // Source.line
        strLogs.append("|");
        // Source.method
        strLogs.append("|");

        // Client Request Id
        boolean isClientMessageId = httpServletRequest.getHeader("clientMessageId") != null;
        if (isClientMessageId) {
            strLogs.append(httpServletRequest.getHeader("clientMessageId"));
        }
        strLogs.append("|");

        // Service Provider
        strLogs.append("|");

        // Client IP
        strLogs.append(fetchClientIpAddr(httpServletRequest)).append("|");

        // Transaction ID
        boolean isTransactionId = httpServletRequest.getHeader("transactionId") != null;
        if (isTransactionId) {
            strLogs.append(httpServletRequest.getHeader("transactionId"));
        }
        strLogs.append("|");

        // ResponseContent
        if (body != null) {
            try {
                ObjectWriter ow = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                        .writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(body);
                String dataResponse;
                if (strResponseHeader.trim().length() > 2) {
                    dataResponse = strResponseHeader + ";" + json.trim().replaceAll(" +", " ");
                } else {
                    dataResponse = json.trim().replaceAll(" +", " ");
                }
                dataResponse = dataResponse.replace("\n", "").replace("\r", "");
                String[] arrString = dataResponse.split("\"");
                if (arrString.length > 0) {
                    for (String string : arrString) {
                        if (string != null && string.trim().length() > 200) {
                            boolean isBase64 = Base64.isBase64(string.getBytes());
                            if (isBase64 || string.trim().length() > 500) {
                                dataResponse = dataResponse.replace(string, "REPLACE CHUOI BASE 64 File");
                            }
                        }
                    }
                }
                strLogs.append(dataResponse).append("|");
            } catch (JsonProcessingException ex) {
                LOGGER.error(ex.getMessage());
            }
        }

        // Transaction Type
        boolean isTransactionType = httpServletRequest.getHeader("transactionType") != null;
        if (isTransactionType) {
            strLogs.append(httpServletRequest.getHeader("transactionType"));
        } else {
            strLogs.append("01");
        }
        strLogs.append("|");

        // System
        strLogs.append(systemCode).append("|");

        // Action Type
        strLogs.append(getActionTypeByMethod(httpServletRequest.getMethod())).append("|");

        // Data Type
        strLogs.append("0|");

        // Num records: Số lượng bản ghi truy vấn
        strLogs.append("|");

        // VSA
        strLogs.append("|");

        // Data Extended
        strLogs.append("|");
        KPI_LOGGER.info(MarkerFactory.getMarker("KPI_LOG"), strLogs.toString());
    }

    /**
     * @param httpServletRequest
     * @return
     */
    private Map<String, String> buildParametersMap(HttpServletRequest httpServletRequest) {
        Map<String, String> resultMap = new HashMap<>();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = httpServletRequest.getParameter(key);
            resultMap.put(key, value);
        }

        return resultMap;
    }

    /**
     * @param response
     * @return
     */
    private Map<String, String> buildHeadersMap(HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        Collection<String> headerNames = response.getHeaderNames();
        for (String header : headerNames) {
            map.put(header, response.getHeader(header));
        }

        return map;
    }

    /**
     * @param request
     * @return
     */
    protected String fetchClientIpAddr(HttpServletRequest request) {
        String ip = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse(request.getRemoteAddr());
        ip = ip.replace(",", "-");
        return ip;
    }

    /**
     * lay dia chi server
     *
     * @return
     */
    private static String getIpAddressAndPort_q() {
        String strIpServer = "";
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();

            Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));

            strIpServer = InetAddress.getLocalHost().getHostAddress();
            if (objectNames != null && objectNames.size() > 0) {
                String port = objectNames.iterator().next().getKeyProperty("port");
                strIpServer += ":" + port;
            }
        } catch (MalformedObjectNameException | UnknownHostException ex) {
            LOGGER.error(ex.getMessage());
        }
        return strIpServer;
    }

    /**
     * Get action type from method
     *
     * @param method
     * @return
     */
    private String getActionTypeByMethod(String method) {
        switch (method) {
            case "GET":
                return "VIEW";
            case "POST":
                return "INSERT";
            case "PUT":
                return "EDIT";
            case "DELETE":
                return "DELETE";
            default:
                return "";
        }
    }

}

package ar.santandertecnologia.transferencias.kitkat.commons.logger.filter;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.constants.Header;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils.*;

@Component
public class LogFilter extends OncePerRequestFilter {

    @Value("${kitkat.logger.endpoints.allowed:}")
    private List<String> allowedEndpoints;
    @Value("${kitkat.logger.endpoints.denied:/health,/metrics}")
    private List<String> deniedEndpoints;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        setCorrelationidInMDC(request, response);
        if (isEndpointAllowed(request.getServletPath()) && !isEndpointDenied(request.getServletPath())) {
            try {
                setPath(request.getServletPath());
                setOriginIp(getClientIpAddress(request));
                LoggerUtils.setMethod(request.getMethod());
                filterChain.doFilter(request, response);
            } finally {
                log();
            }
        } else {
            filterChain.doFilter(request, response);
            clearCache();
        }
    }

    private boolean isEndpointDenied(String servletPath) {
        return deniedEndpoints == null || deniedEndpoints.isEmpty() || deniedEndpoints.contains(servletPath);
    }

    private boolean isEndpointAllowed(String servletPath) {
        return allowedEndpoints == null || allowedEndpoints.isEmpty() || allowedEndpoints.contains(servletPath);
    }

    private static final String[] HEADERS_TO_TRY = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"};

    private String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private String setCorrelationidInMDC(HttpServletRequest request, HttpServletResponse response) {
        String uuid = request.getHeader(Header.CORRELATIONID);
        if (uuid == null) {
            uuid = response.getHeader(Header.CORRELATIONID);
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
                response.setHeader(Header.CORRELATIONID, uuid);
            }
        }
        MDC.put(Header.CORRELATIONID, uuid);
        return uuid;
    }

}

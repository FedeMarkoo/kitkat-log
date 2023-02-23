package ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.model.LoggerExternalData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

import static ar.santandertecnologia.transferencias.kitkat.commons.logger.constants.Header.CORRELATIONID;
import static ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils.*;

@Component
@Slf4j
public class OutgoingCallsInterceptor implements ClientHttpRequestInterceptor {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    @SneakyThrows
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
        ClientHttpResponse response;
        DateTime key = DateTime.now();
        LoggerExternalData data = getLoggerExternalData(key);
        initExternalCall(key);

        setHeader(request);

        logRequest(request, body, data);
        try {
            response = execution.execute(request, body);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream(); InputStream responseBody = response.getBody()) {
                IOUtils.copy(responseBody, out);
                byte[] byteArray = out.toByteArray();
                InputStream inputStream = new ByteArrayInputStream(byteArray);
                logResponse(data, response, byteArray);
                return new LoggedClientHttpResponse(response, inputStream);
            } catch (Exception e) {
                return response;
            }
        } catch (Exception e) {
            logException(data, e);
            throw e;
        } finally {
            endExternalCall(key);
        }
    }

    private void setHeader(HttpRequest request) {
        String correlationalId = MDC.get(CORRELATIONID);
        if (correlationalId == null) {
            correlationalId = UUID.randomUUID().toString();
            MDC.put(CORRELATIONID, correlationalId);
        }
        if (correlationalId != null) {
            request.getHeaders().addIfAbsent(CORRELATIONID, correlationalId);
        }
    }

    private void logException(LoggerExternalData data, Exception e) {
        data.setThrowable(e);
    }

    private void logResponse(LoggerExternalData data, ClientHttpResponse response, byte[] byteArray) {
        try {
            data.setStatusCode(response.getStatusCode().value());
            if (byteArray.length > 0)
                data.setResponse(MAPPER.readValue(byteArray, HashMap.class));
        } catch (Exception e) {
            log.error("Error parseando response");
        }
    }

    private void logRequest(HttpRequest request, byte[] body, LoggerExternalData data) {
        try {
            Object requestBody = getRequestBody(body);
            String path = request.getURI().toString();
            data.setPath(path);
            HttpMethod method = request.getMethod();
            if (method != null)
                data.setMethod(method.name());
            data.setHeader(request.getHeaders());
            data.setRequest(requestBody);
        } catch (Exception e) {
            log.error("Error parseando request");
        }
    }

    private Object getRequestBody(byte[] body) {
        try {
            return MAPPER.readValue(body, HashMap.class);
        } catch (Exception e) {
            return new String(body, StandardCharsets.UTF_8);
        }
    }

    public static class LoggedClientHttpResponse implements ClientHttpResponse {
        ClientHttpResponse response;
        InputStream inputStream;

        public LoggedClientHttpResponse(ClientHttpResponse response, InputStream inputStream) {
            this.response = response;
            this.inputStream = inputStream;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            return inputStream;
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }
}
package ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.model.LoggerExternalData;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpComponentsConnection;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.UUID;

import static ar.santandertecnologia.transferencias.kitkat.commons.logger.constants.Header.CORRELATIONID;


@Log4j2
public class LogClientInterceptor implements ClientInterceptor {
    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        try {
            LoggerUtils.initExternalCall(messageContext);
            LoggerExternalData data = getLoggerExternalData(messageContext);

            TransportContext transportContext = TransportContextHolder.getTransportContext();
            data.setPath(transportContext.getConnection().getUri().toString());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream stream1 = new PrintStream(outputStream);
            WebServiceMessage request = messageContext.getRequest();
            request.writeTo(stream1);
            data.setRequest(outputStream.toString());

            if (transportContext.getConnection() instanceof HttpComponentsConnection) {
                HttpComponentsConnection connection = (HttpComponentsConnection) transportContext.getConnection();
                String correlationalId = MDC.get(CORRELATIONID);
                if (correlationalId == null) {
                    correlationalId = UUID.randomUUID().toString();
                    MDC.put(CORRELATIONID, correlationalId);
                }
                if (correlationalId != null) {
                    connection.addRequestHeader(CORRELATIONID, correlationalId);
                }
                setLogHeaders(connection, data);
            }
        } catch (Exception e) {
            log.error("Error parseando request");
        }
        return true;
    }

    private void setLogHeaders(HttpComponentsConnection connection, LoggerExternalData data) {
        HttpHeaders headers = new HttpHeaders();
        try {
            HttpPost httpPost = connection.getHttpPost();
            Arrays.stream(httpPost.getAllHeaders()).forEach(h -> headers.add(h.getName(), h.getValue()));
        } catch (Exception e) {
            log.error(e);
        } finally {
            data.setHeader(headers);
        }
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception e) throws WebServiceClientException {
        try {
            LoggerUtils.endExternalCall(messageContext);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream stream1 = new PrintStream(outputStream);
            messageContext.getResponse().writeTo(stream1);
            LoggerExternalData data = getLoggerExternalData(messageContext);
            data.setResponse(outputStream.toString());
            data.setThrowable(e);
        } catch (Exception ex) {
            log.error("Error parseando response");
        }
    }

    private LoggerExternalData getLoggerExternalData(MessageContext messageContext) {
        return LoggerUtils.getLoggerExternalData(messageContext);
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }
}

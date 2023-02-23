package ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.server.SmartEndpointInterceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;


@Log4j2
@Component
public class IncomingSOAPInterceptor implements SmartEndpointInterceptor, EndpointExceptionResolver {
    @Override
    public boolean handleRequest(MessageContext messageContext, Object var2 ) {
        try {
            LoggerUtils.adviceRequest(getRequestXml(messageContext));
        } catch (Exception e) {
            log.error("Error parseando request");
        }
        return true;
    }

    private String getRequestXml(MessageContext messageContext) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream stream1 = new PrintStream(outputStream);
        WebServiceMessage request = messageContext.getRequest();
        request.writeTo(stream1);
        return outputStream.toString().replaceAll("\\\\n|\\\\r", "").replaceAll("\\s\\s+", " ");
    }

    private String getResponseXml(MessageContext messageContext) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream stream1 = new PrintStream(outputStream);
        WebServiceMessage request = messageContext.getResponse();
        request.writeTo(stream1);
        return outputStream.toString().replaceAll("\\\\n|\\\\r", "").replaceAll("\\s\\s+", " ");
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object var2, Exception e) {
        try {
            LoggerUtils.adviceResponse(getResponseXml(messageContext));
        } catch (Exception ex) {
            log.error("Error parseando response");
        }
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object var2)  {
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object var2) {
        return true;
    }

    @Override
    public boolean shouldIntercept(MessageContext messageContext, Object o) {
        return true;
    }

    @Override
    public boolean resolveException(MessageContext messageContext, Object o, Exception e) {
        LoggerUtils.setThrowable(e);
        return false;
    }
}

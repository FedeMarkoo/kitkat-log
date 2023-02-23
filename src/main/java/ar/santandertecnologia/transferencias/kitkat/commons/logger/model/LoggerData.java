package ar.santandertecnologia.transferencias.kitkat.commons.logger.model;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LogProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LogProperties.IGNORED_TRACE_PATTERN;
import static java.util.function.Function.identity;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoggerData {
    @JsonIgnore
    private static LogProperties logProperties;
    public static void setProperties(LogProperties properties) {
        if (logProperties == null)
            logProperties = properties;
    }


    private Long duration;
    private String path;
    private String method;
    private String originIp;
    private Object request;
    private Object response;
    @JsonIgnore
    private Instant time;
    @JsonIgnore
    private Map<Object, LoggerExternalData> externalCalls;
    @JsonIgnore
    private Throwable throwable;
    private Map<String, Object> jmsData;

    public boolean isException() {
        return throwable != null;
    }

    public String getExceptionMessage() {
        if (isException()) return getThrowable().toString();
        else return null;
    }

    public List<String> getExceptionStackTrace() {
        if (isException())
            return Arrays.stream(getThrowable().getStackTrace())
                    .map(StackTraceElement::toString)
                    .filter(s -> !s.matches(IGNORED_TRACE_PATTERN))
                    .collect(Collectors.toList());
        else return Collections.emptyList();
    }

    public Map<Object, LoggerExternalData> getExternalCalls() {
        if (externalCalls == null)
            externalCalls = new LinkedHashMap<>();
        return externalCalls;
    }

    public Map<String, LoggerExternalData> getOutgoingCalls() {
        AtomicInteger i = new AtomicInteger();
        return getExternalCalls()
                .values()
                .stream()
                .collect(
                        Collectors.toMap(
                                externalData -> (i.getAndIncrement()) + " - " + externalData.getPath(),
                                identity(),
                                (a1, a2) -> a1,
                                LinkedHashMap::new
                        ));
    }
}
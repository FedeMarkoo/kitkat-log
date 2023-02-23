package ar.santandertecnologia.transferencias.kitkat.commons.logger.util;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.model.LoggerData;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.model.LoggerExternalData;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.model.LoggerMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ar.santandertecnologia.transferencias.kitkat.commons.logger.constants.Header.CORRELATIONID;
import static ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LogProperties.IGNORED_FIELD_NAMES;
import static java.util.regex.Pattern.CASE_INSENSITIVE;


@Slf4j(topic = "kitkat")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerUtils {
    private static final LoggerMap map = new LoggerMap();
    private static final ObjectMapper MAPPER = LogProperties.getObjectMapper();
    public static final Pattern PATTERN = Pattern.compile(IGNORED_FIELD_NAMES, CASE_INSENSITIVE);
    public static final String REPLACEMENT = "$1hidden$3";
    public static final String REPLACEMENT2 = "hidden";

    public static void adviceRequest(Object body) {
        LoggerData loggerData = getLoggerData();
        loggerData.setRequest(body);
    }

    public static void adviceResponse(Object body) {
        LoggerData loggerData = getLoggerData();
        loggerData.setResponse(body);
    }

    public static void initLog() {
        getLoggerData();
    }

    public static void log() {
        LoggerData loggerData = map.remove(Thread.currentThread());
        if(loggerData != null) {
            if (loggerData.isException()) {
                setMillis(loggerData);
                log.error(parse(loggerData));
            } else if (log.isInfoEnabled()) {
                setMillis(loggerData);
                log.info(parse(loggerData));
            }
        }
        clearCache();
    }

    private static void setMillis(LoggerData loggerData) {
        long millis = Instant.now().toEpochMilli() - loggerData.getTime().toEpochMilli();
        loggerData.setDuration(millis);
    }

    public static void clearCache() {
        map.remove(Thread.currentThread());
        MDC.clear();
    }

    private static String parse(Object body) {
        String json = "";
        try {
            json = MAPPER.writeValueAsString(body);
            json = hideSensitiveData(json);
            return json;
        } catch (Exception e) {
            return json;
        }
    }

    private static String hideSensitiveData(String json) {
        Matcher matcher = PATTERN.matcher(json);
        while(matcher.find()) {
            String group = matcher.group(2).replace("\\", "\\\\").replace("\"", "\\\"");
            json = json.replaceAll(group,REPLACEMENT2);
        }
        json = PATTERN.matcher(json).replaceAll(REPLACEMENT);
        return json;
    }

    public static void setPath(String path) {
        getLoggerData().setPath(path);
    }

    private static LoggerData getLoggerData() {
        LoggerData loggerData = map.get(Thread.currentThread());
        if (loggerData == null) {
            loggerData = new LoggerData();
            loggerData.setTime(Instant.now());
            map.put(Thread.currentThread(), loggerData);
            if (MDC.get(CORRELATIONID) == null) {
                String uuid = UUID.randomUUID().toString();
                MDC.put(CORRELATIONID, uuid);
            }
        }
        return loggerData;
    }

    public static void setThrowable(Object throwable) {
        getLoggerData().setThrowable((Throwable) throwable);
    }

    public static void setOriginIp(String ipAddress) {
        getLoggerData().setOriginIp(ipAddress);
    }

    public static LoggerExternalData getLoggerExternalData(Object key) {
        LoggerExternalData data = getLoggerData().getExternalCalls().get(key);
        if (data == null) {
            data = new LoggerExternalData();
            getLoggerData().getExternalCalls().put(key, data);
        }
        return data;
    }

    public static void initExternalCall(Object key) {
        getLoggerExternalData(key).setBeginTime(DateTime.now());
    }

    public static void endExternalCall(Object key) {
        getLoggerExternalData(key).setEndTime(DateTime.now());
    }

    public static void setJMSData(String key, Object args) {
        if (getLoggerData().getJmsData() == null) {
            getLoggerData().setJmsData(new HashMap<>());
        }
        getLoggerData().getJmsData().put(key, args);
    }

    public static void setMethod(String method) {
        getLoggerData().setMethod(method);
    }
}


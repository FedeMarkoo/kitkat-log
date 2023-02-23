package ar.santandertecnologia.transferencias.kitkat.commons.logger.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class LogProperties {
    private static final String DATO = "((?:[^\"]+|\\\\\\\")+)";
    public static final String IGNORED_TRACE_PATTERN =  Stream.of(
                    "java.base"
                    , "javax.servlet"
                    , "org.apache"
                    , "org.springframework"
                    , "kitkat.commons.logger")
            .collect(Collectors.joining("|", ".*(", ").*"));

    public static final String IGNORED_FIELD_NAMES = Stream.of(
            "(?:\\w+-)?authorization"
            , "(?:\\w+-?)?token"
            , "(?:\\w+-?)?clave"
            , "(?:\\w+-?)?pass(?:word)?"
            , "(?:\\w+-?)?api-?key")
            .collect(Collectors.joining("|", "([\"<](?:", ")\"[:>]\\[?\")"+DATO+"(\"(?:\\]|,|})|<)"));

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

}

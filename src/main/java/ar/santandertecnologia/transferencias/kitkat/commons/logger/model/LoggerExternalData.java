package ar.santandertecnologia.transferencias.kitkat.commons.logger.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.joda.time.DateTime;
import org.springframework.http.HttpHeaders;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoggerExternalData {
    private String path;
    private Object request;
    private HttpHeaders header;
    private String method;
    private Integer statusCode;
    private Object response;
    private DateTime beginTime;
    private DateTime endTime;
    @JsonIgnore
    private Throwable throwable;

    public boolean isException() {
        return throwable != null;
    }

    public String getExceptionMessage() {
        if (isException()) return getThrowable().toString();
        else return null;
    }

    public long getDuration() {
        if(endTime == null || beginTime == null ) return 0;
        return endTime.getMillis() - beginTime.getMillis();
    }

    public String getBeginTime() {
        if(beginTime == null ) return "";
        return beginTime.toString();
    }

    public String getEndTime() {
        if(endTime == null ) return "";
        return endTime.toString();
    }
}
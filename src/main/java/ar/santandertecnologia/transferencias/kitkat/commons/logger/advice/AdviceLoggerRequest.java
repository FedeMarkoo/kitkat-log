package ar.santandertecnologia.transferencias.kitkat.commons.logger.advice;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

import static ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils.adviceRequest;

@Log4j2
@RestControllerAdvice
@EnableAutoConfiguration
public class AdviceLoggerRequest extends RequestBodyAdviceAdapter {


  @Override
  public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
    return true;
  }

  @Override
  public Object afterBodyRead(Object body, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
    adviceRequest(body);
    return body;
  }

}

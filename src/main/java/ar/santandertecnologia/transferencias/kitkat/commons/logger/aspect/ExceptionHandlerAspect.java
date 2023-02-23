package ar.santandertecnologia.transferencias.kitkat.commons.logger.aspect;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Log4j2
public class ExceptionHandlerAspect {

    @Before(value = "@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public void setThrowable(JoinPoint joinPoint) {
        Arrays.stream(joinPoint.getArgs())
            .filter(Throwable.class::isInstance)
            .forEach(LoggerUtils::setThrowable);
    }
}

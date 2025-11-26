package com.example.financery.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.financery..*(..)) "
        + "&& !execution(* com.example.financery.mapper.TransactionMapper.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        if (logger.isDebugEnabled()) {
            logger.info("Началось выполнение: {}", joinPoint.getSignature().toShortString());
        }
    }

    @AfterReturning(pointcut = "execution(* com.example.financery..*(..)) "
            + "&& !execution(* com.example.financery.mapper.TransactionMapper.*(..))",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (logger.isDebugEnabled()) {
            String resultString = safeToString(result); // Используем безопасный метод
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    logger.warn("Закончилось выполнение: {} с результатом: {}",
                            joinPoint.getSignature().toShortString(), resultString);
                    return;
                }
            }
            logger.info("Закончилось выполнение: {} с результатом: {}",
                    joinPoint.getSignature().toShortString(), resultString);
        }
    }

    private String safeToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            // Избегаем вызова toString() на сущностях Hibernate
            if (obj.getClass().getPackageName().startsWith("com.example.financery.model")) {
                return obj.getClass().getSimpleName() + "@"
                        + Integer.toHexString(System.identityHashCode(obj));
            }
            if (obj instanceof Collection) {
                return "Collection[size=" + ((Collection<?>) obj).size() + "]";
            }
            if (obj instanceof Optional) {
                Optional<?> optional = (Optional<?>) obj;
                return "Optional["
                        + (optional.isPresent()
                        ? safeToString(optional.get())
                        : "empty") + "]";
            }
            return String.valueOf(obj);
        } catch (Exception e) {
            return "FAILED toString(): " + e.getMessage();
        }
    }

    @AfterThrowing(pointcut = "execution(* com.example.financery..*(..))", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        if (logger.isDebugEnabled()) {
            logger.error("Исключение в: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }
}
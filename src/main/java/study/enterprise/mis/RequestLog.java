package study.enterprise.mis;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.*;

@Aspect
public class RequestLog {
    private static final Logger LOG = LoggerFactory.getLogger(RequestLog.class);

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object record(ProceedingJoinPoint point) throws Throwable {
        long start = System.nanoTime();
        String action = point.getSignature().toShortString();
        try {
            Object result = point.proceed();
            LOG.info("{} completed in {} ms", action, (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Throwable error) {
            LOG.warn("{} failed: {}", action, error.getClass().getSimpleName());
            throw error;
        }
    }
}

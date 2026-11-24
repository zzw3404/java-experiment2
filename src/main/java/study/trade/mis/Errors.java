package study.trade.mis;

import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class Errors {
    private static final Logger LOG = LoggerFactory.getLogger(Errors.class);

    private ResponseEntity<Result<Void>> response(int status, String message) {
        return ResponseEntity.status(status).body(new Result<>(status, message, null));
    }

    @ExceptionHandler(Failure.class)
    public ResponseEntity<Result<Void>> business(Failure e) {
        return response(e.status, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> invalid(MethodArgumentNotValidException e) {
        return response(400, e.getBindingResult().getFieldError().getField() + " 参数无效");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result<Void>> invalidRequest(Exception e) {
        return response(400, "请求格式或参数类型错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> badMethod(Exception e) {
        return response(405, "不支持该请求方法");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> conflict(Exception e) {
        return response(409, "数据存在关联或违反约束");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> unexpected(Exception e) {
        LOG.error("请求处理异常", e);
        return response(500, "系统执行失败");
    }
}

package study.enterprise.mis;

public class Result<T> {
    public final int code;
    public final String message;
    public final T data;

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "成功", data);
    }
}

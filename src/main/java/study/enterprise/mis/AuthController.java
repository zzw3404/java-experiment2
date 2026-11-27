package study.enterprise.mis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.http.*;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    static final String SESSION_USER = "loginUser";
    private final Environment environment;

    public AuthController(Environment environment) {
        this.environment = environment;
    }

    public static class Login {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody Login login, HttpServletRequest request) {
        String username = environment.getProperty("enterprise.auth.user", "admin");
        String password = environment.getProperty("enterprise.auth.password");
        if (password == null || password.isEmpty()) {
            throw new Failure(503, "请配置实验账号口令");
        }
        if (!username.equals(login.username) || login.password == null ||
                !MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                        login.password.getBytes(StandardCharsets.UTF_8))) {
            throw new Failure(401, "用户名或口令错误");
        }
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute(SESSION_USER, username);
        return Result.ok(username);
    }

    @GetMapping("/me")
    public Result<Object> current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(SESSION_USER);
        if (user == null) {
            throw new Failure(401, "请先登录");
        }
        return Result.ok(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.ok(null);
    }
}

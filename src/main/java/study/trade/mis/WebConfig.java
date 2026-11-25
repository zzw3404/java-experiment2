package study.trade.mis;

import javax.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@Import({ProductController.class, AuthController.class, Errors.class, RequestLog.class})
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/demo/**")
                .addResourceLocations("classpath:/demo/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws java.io.IOException {
                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute(AuthController.SESSION_USER) != null) {
                    return true;
                }
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
                return false;
            }
        }).addPathPatterns("/products", "/products/**");
    }
}

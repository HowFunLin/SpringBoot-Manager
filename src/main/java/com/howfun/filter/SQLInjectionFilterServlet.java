package com.howfun.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 防止SQL注入攻击
 */
@Configuration // 配置类，作为Bean加入容器
@WebFilter(urlPatterns = "/*", filterName = "SQLInjection")
public class SQLInjectionFilterServlet implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SQLInjectionFilterServlet.class);

    private String regularExpression;

    public SQLInjectionFilterServlet() {

    }

    public void init(FilterConfig filterConfig) {
        regularExpression = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|" + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
        log.info("######### regularExpression={}", regularExpression);
    }


    /**
     * 如果输入“ ' ”，“ ; ”，“ -- ”这些字符，可以考虑将这些字符转义为html转义字符 “'”转义字符为：&#39; “;”转义字符为：&#59;
     * “--”转义字符为：&#45;&#45;
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        //获取HTTP请求数据
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String[] value = entry.getValue();

            for (String aValue : value) {
                if (null != aValue && aValue.matches(regularExpression)) {
                    log.info("*******疑似SQL注入攻击！参数名称：{}；录入信息:{}", entry.getKey(), aValue);

                    // 设置错误信息
                    servletRequest.setAttribute("err", "您输入的参数有非法字符，请输入正确的参数！");
                    servletRequest.setAttribute("pageUrl", req.getRequestURI());

                    return;
                }
            }
        }

        //将请求转发给过滤器链下一个filter
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}

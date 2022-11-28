package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.common.Result;
import com.itheima.common.utils.MyThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Auther: zhangTZ
 * @Date: 2022/10/5 - 10 - 05 - 23:31
 */

/**
 *登录的过滤器
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符的写法
    public static final AntPathMatcher ANT_PATH_MATCHER= new AntPathMatcher();

    /**
     * 1、获取本次请求的URL。
     * 2、判断本次请求是否需要处理
     * 3、如果不需要处理，则直接放行
     * 4、判断登录状态，如果己登录，则直接放行。
     * 5、如果未登录则返回未登录结果
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 1、获取本次请求的URL
        String requestURI = request.getRequestURI();
        log.info("拦截到请求: {}",requestURI);
        /**
         *  这里对backend和front下面的静态资源进行了放行，重点控制页面数据
         *  登录和退出功能进行了放行
         */
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"


        };
        // 2、写一个专门的方法判断本次请求是否需要处理
        Boolean checkURLs = checkURLs(urls, requestURI);
        // 3、如果不需要处理，则直接放行
        if (checkURLs){
//            log.info("本次请求不需要处理");
            filterChain.doFilter(request,response);
            return;
        }
        // 4-1、判断后台用户登录状态，如果己登录，则直接放行。
        if (request.getSession().getAttribute("employee")!=null){
            log.info("本次请求已处理--用户已经登录id为"+request.getSession().getAttribute("employee"));
            /**
             * 由于无法直接从MetaObjectHandler获取session来获取id
             * 所以把id存入ThreadLocal的线程副本
             * 再使用ThreadLocal的线程副本获取
             */
            Long empId = (Long) request.getSession().getAttribute("employee");
            MyThreadLocalUtil.setId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        // 4-2、判断移动端用户登录状态，如果己登录，则直接放行。
        if (request.getSession().getAttribute("user")!=null){
            log.info("本次请求已处理--用户已经登录id为"+request.getSession().getAttribute("user"));
            /**
             * 由于无法直接从MetaObjectHandler获取session来获取id
             * 所以把id存入ThreadLocal的线程副本
             * 再使用ThreadLocal的线程副本获取
             */
            Long userId = (Long) request.getSession().getAttribute("user");
            MyThreadLocalUtil.setId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        //5、如果未登录则返回未登录结果 通过输出流像前端响应数据，前端完成页面跳转
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        log.info("本次请求已处理--用户没有登录"+requestURI);
        return;
    }

    /**
     * 用来判断本次请求是否放行的方法
     * @return
     */
    public static Boolean checkURLs(String[] urls,String requestURI){

        for (String url : urls) {
            boolean match = ANT_PATH_MATCHER.match(url, requestURI);

            if (match){

                return true;
            }
        }
        return false;
    }
}

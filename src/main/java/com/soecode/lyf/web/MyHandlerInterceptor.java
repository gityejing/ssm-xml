package com.soecode.lyf.web;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class MyHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.debug(" 在请求目标方法前调用，返回false，请求结束。");
        if(handler instanceof HandlerMethod){
            HandlerMethod hm = (HandlerMethod) handler;
            log.debug("==="+hm.getMethod()); // 返回调用的那个类的方法的签名
            log.debug("==="+hm.getBean());// controller 的一个具体的对象
            log.debug("==="+hm.getBeanType());// controller 的全类名
        }
        request.setCharacterEncoding("UTF-8");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.debug(" 在目标方法调用完后执行，前提是preHandle返回true");
        log.debug("==="+modelAndView.getViewName());// 返回视图的逻辑名
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}

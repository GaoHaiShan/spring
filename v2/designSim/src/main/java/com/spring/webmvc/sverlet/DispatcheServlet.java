package com.spring.webmvc.sverlet;

import com.spring.annotation.MyController;
import com.spring.annotation.RequestMapping;
import com.spring.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcheServlet extends HttpServlet {
    private List<HandlerMapping> handlerMappings = new LinkedList<>();
    private Map<HandlerMapping,HandlerAdapert> handlerAdapert = new HashMap<>();
    private ViewResolver viewResolvers;
    //get 请求处理
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
    //post 请求处理
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req,resp);
        } catch (Exception e) {
            processDispatcher(req,resp,new ModelAndView("500"));
            e.printStackTrace();
        }
    }

    //客户请求具体处理方法
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp){
        //拿到对应的handlermapping,如果为空则返回404页面
        HandlerMapping handlerMapping = getHandler(req);
        if(null == handlerMapping){
            processDispatcher(req,resp,new ModelAndView("404"));
            return;
        }
        //得到对应的 handlerAdapert
        HandlerAdapert handlerAdapert = this.handlerAdapert.get(handlerMapping);
        ModelAndView mv = null;
        try {
            //得到对应的 methodAndView 对象
            mv = handlerAdapert.handler(req,resp,handlerMapping);
        } catch (Exception e) {
            //异常则返回500页面
            processDispatcher(req,resp,new ModelAndView("500"));
            e.printStackTrace();
        }
        //返回对应视图
        processDispatcher(req,resp,mv);
    }

    private void processDispatcher(HttpServletRequest req, HttpServletResponse resp, ModelAndView mv) {
        if(null == mv){return;}
        //
        View view =  this.viewResolvers.resolveViewName(mv.getViewName());
        try {
            view.render(req,resp,mv.getModel());

        } catch (Exception e) {
            processDispatcher(req,resp,new ModelAndView("500"));
           e.printStackTrace();
        }
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){return null;}
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        for (HandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher= handlerMapping.getUrl().matcher(url);
            if(!matcher.matches()){continue;}
            return handlerMapping;
        }
        return null;
    }
    //入口 一步步往下点
    @Override
    public void init(ServletConfig config) throws ServletException {
        //aop ioc di 初始化
        ApplicationContext context = new ApplicationContext(config.getInitParameter("contextConfigLocation").split(":")[1]);
        //mvc 初始化
        initStrategies(context);
    }

    protected void initStrategies(ApplicationContext context) {
        //初始化 handlerMapping
        this.initHandlerMappings(context);
        //初始化 handlerAdapters
        this.initHandlerAdapters(context);
        //初始化 viewResolvers
        this.initViewResolvers(context);
    }

    private void initViewResolvers(ApplicationContext context) {
        //获取配置信息
        Properties properties = context.getBeanDefinitionReader().getResource();
        String templateRoot = properties.getProperty("templateRoot");
        //初始化 viewResolvers
        this.viewResolvers = new ViewResolver(templateRoot);
    }

    private void initHandlerAdapters(ApplicationContext context) {
        //为每一个 handlermapping 创造一个 HandlerAdapert对象
        for (HandlerMapping handlerMapping : handlerMappings) {
            handlerAdapert.put(handlerMapping,new HandlerAdapert());
        }
    }

    private void initHandlerMappings(ApplicationContext context) {
        Map<String,Object> map =  context.getBeanFactory();
        //循环 beanFactory 中带有 @MyController 注解的对象
        for (Object o : map.values()) {
            String parentUrl = "";
            if(o.getClass().isAnnotationPresent(RequestMapping.class)){
                parentUrl = o.getClass().getAnnotation(RequestMapping.class).value();
            }
            if(!o.getClass().isAnnotationPresent(MyController.class)){continue;}
            //循环类中带有 @RequestMapping 注解的方法，保存到 handlerMappings 中。
            for (Method method : o.getClass().getMethods()) {
                if(method.isAnnotationPresent(RequestMapping.class)) {
                    String url = ("/" + parentUrl + "/"
                            + method.getAnnotation(RequestMapping.class).value().replaceAll("\\*", ".*"))
                            .replaceAll("/+", "/");
                    HandlerMapping mapping = new HandlerMapping(Pattern.compile(url), method, o);
                    this.handlerMappings.add(mapping);
                }
            }
        }
    }
}

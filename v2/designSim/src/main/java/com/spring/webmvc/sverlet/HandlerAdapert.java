package com.spring.webmvc.sverlet;

import com.spring.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HandlerAdapert {

    public ModelAndView handler(HttpServletRequest req, HttpServletResponse resp, HandlerMapping hanlder) throws Exception {
        //浏览器传入参数
        Map<String,String[]> reqParam = req.getParameterMap();
        //调用的方法
        Method m = hanlder.getMethod();
        //储存 参数名称以及对应下标
        Map<String,Integer> paramIndex = getParamIndex(m);
        //储存实参列表
        Object[] o = new Object[paramIndex.size()];
        //形参列表
        Class<?>[] paramTypes = m.getParameterTypes();
        //赋值实参列表
        for (Map.Entry<String, Integer> entry : paramIndex.entrySet()) {
            if(entry.getKey().equals(HttpServletRequest.class.getName())){
                o[entry.getValue()] = req;
            }else if(entry.getKey().equals(HttpServletResponse.class.getName())){
                o[entry.getValue()] = resp;
            }else {
                //类型处理
                String value = Arrays.toString((String[])reqParam.get(entry.getKey()))
                        .replaceAll("\\[|\\]","")
                        .replaceAll("\\s+",",");
                if (int.class.equals(paramTypes[entry.getValue()])) {
                    o[entry.getValue()] = Integer.valueOf(value);
                } else if (String.class.equals(paramTypes[entry.getValue()])) {
                    o[entry.getValue()] = value;
                } else if (double.class.equals(paramTypes[entry.getValue()])) {
                    o[entry.getValue()] = Double.valueOf(value);
                } else if (long.class.equals(paramTypes[entry.getValue()])) {
                    o[entry.getValue()] = Long.valueOf(value);
                } else if (float.class.equals(paramTypes[entry.getValue()])) {
                    o[entry.getValue()] = Float.valueOf(value);
                } else if (short.class.equals(paramTypes[entry.getValue()])) {
                    o[entry.getValue()] = Short.valueOf(value);
                } else {
                    o[entry.getValue()] = value;
                }
            }
        }
        //反射调用
        Object result = m.invoke(hanlder.getController(),o);
        if(result==null||result instanceof Void){ return null;}
        if (m.getReturnType() == ModelAndView.class){
            return (ModelAndView) result;
        }else {
            resp.getWriter().write((String) result);
            return null;
        }
    }

    public Map<String,Integer> getParamIndex(Method m){
        Class<?>[] paramTypes = m.getParameterTypes();
        Map<String,Integer> paramIndex = new HashMap<>();
        Annotation[][] annotations = m.getParameterAnnotations();

        for(int i = 0;i < annotations.length;i++){
            for (Annotation annotation : annotations[i]) {
                if(annotation instanceof RequestParam){
                    String value = ((RequestParam) annotation).value();
                    paramIndex.put(value,i);
                }
            }
        }
        for(int i = 0;i < paramTypes.length;i++){
            if(paramTypes[i] == HttpServletRequest.class||paramTypes[i] == HttpServletResponse.class){
                paramIndex.put(paramTypes[i].getName(),i);
            }
        }
        return paramIndex;
    }
}

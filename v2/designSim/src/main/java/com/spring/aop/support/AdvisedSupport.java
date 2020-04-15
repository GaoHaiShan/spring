package com.spring.aop.support;

import com.spring.aop.aspect.Advice;
import com.spring.aop.config.AopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvisedSupport {
    private Class<?> targetClass;
    private Object target;
    private AopConfig config;
    private Pattern pointCutClassPattern;
    private Map<Method, Map<String, Advice>> methodCache;

    public AdvisedSupport(AopConfig config) {
        this.config = config;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        try {
            pares();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void pares() throws ClassNotFoundException {
        //把Spring的Excpress变成Java能够识别的正则表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));
        Pattern pointCutPattern = Pattern.compile(pointCut);
        //享元的共享池
        methodCache = new HashMap<Method, Map<String, Advice>>();
        Class<?> clazz = Class.forName(config.getAspectClass());
        Map<String,Advice> map = null;
        try {
            map  = init(clazz);
        } catch (Exception e){
            e.printStackTrace();
        }
        for (Method method : targetClass.getMethods()) {
            String methodString = method.toString();
            if(methodString.contains("throws")){
                methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
            }
            Matcher matcher = pointCutPattern.matcher(methodString);
            if(matcher.matches()){
                methodCache.put(method,map);
            }
        }
    }

    public Map<String,Advice> getAdvices(Method method) throws Exception {
        //享元设计模式的应用
        Map<String,Advice> cache = methodCache.get(method);

        return cache;
    }

    private Map<String, Advice> init(Class<?> clazz) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        Map<String,Advice> advices = new HashMap<String, Advice>();

        if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
            advices.put("before",new Advice(clazz.newInstance(),clazz.getMethod(config.getAspectBefore())));
        }
        if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
            advices.put("after",new Advice(clazz.newInstance(),clazz.getMethod(config.getAspectAfter())));
        }
        if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
            Advice advice = new Advice(clazz.newInstance(),clazz.getMethod(config.getAspectAfterThrow()));
            advice.setThrowName(config.getAspectAfterThrowingName());
            advices.put("afterThrow",advice);
        }
        return advices;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public boolean pointCutMath() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }
}

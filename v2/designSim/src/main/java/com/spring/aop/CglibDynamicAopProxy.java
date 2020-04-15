package com.spring.aop;

import com.spring.aop.aspect.Advice;
import com.spring.aop.support.AdvisedSupport;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class CglibDynamicAopProxy implements MethodInterceptor {
    private AdvisedSupport config ;

    public CglibDynamicAopProxy(AdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        //调用方法前
        Map<String, Advice> conf = config.getAdvices(method);
        Advice beforeAdvice =  conf.get("before");
        Method beforeAdviceMethod = beforeAdvice.getAdviceMethod();
        beforeAdviceMethod.invoke(beforeAdvice.getAspect(),null);
        //调用目标方法
        Object res = null;
        try {
            res = methodProxy.invokeSuper(o,args);
        }catch (Exception e){
            //抛出异常调用
            Advice exAdvice = conf.get("afterThrow");
            Method exAdviceMethod = exAdvice.getAdviceMethod();
            exAdviceMethod.invoke(exAdvice.getAspect(),null);
            e.printStackTrace();
        }
        //调用方法后
        Advice afterAdvice =  conf.get("after");
        Method afterAdviceMethod = afterAdvice.getAdviceMethod();
        afterAdviceMethod.invoke(afterAdvice.getAspect(),null);
        return res;
    }
}

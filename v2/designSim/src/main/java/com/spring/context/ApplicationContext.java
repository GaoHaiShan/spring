package com.spring.context;

import com.spring.annotation.Autowired;
import com.spring.annotation.MyController;
import com.spring.annotation.MyService;
import com.spring.aop.CglibDynamicAopProxy;
import com.spring.aop.config.AopConfig;
import com.spring.aop.support.AdvisedSupport;
import com.spring.beans.BeanDefinition;
import com.spring.beans.BeanDefinitionReader;
import com.spring.beans.BeanWarper;
import com.spring.util.SpringUtil;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Field;
import java.util.*;

public class ApplicationContext {
    private BeanDefinitionReader beanDefinitionReader;
    private Map<String, BeanDefinition> definition = new HashMap<>();
    private Map<String,Object> beanFactoryObjectCache = new HashMap<>();
    private Map<String,BeanWarper> beanFactoryInstanceCache = new HashMap<>();
    public ApplicationContext(String... resource) {
        //读取配置文件，
        beanDefinitionReader = new BeanDefinitionReader(resource[0]);
        //根据配置文件 生成每个类对应的的 BeanDefinition 对象
        //List<BeanDefinition> 转成 map 如果已存在则抛出异常
        doRegisterBeanDefinition(beanDefinitionReader.loadBeanDefinition());
        //将实时加载的类进行初始化
        doAutowrited();
    }

    private void doAutowrited() {
        //延时加载默认开启  设置 isLazy = false
        for (Map.Entry<String, BeanDefinition> entry : definition.entrySet()) {
            if(!entry.getValue().isLazy()){
                getBean(entry.getKey());
            }
        }
    }

    private List<BeanDefinition> doRegisterBeanDefinition(List<BeanDefinition> definitionList) {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        for (BeanDefinition beanDefinition : definitionList) {
            if(definition.containsKey(beanDefinition.getBeanName())){
                try {
                    throw new Exception("beanDefinition is early load");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            definition.put(beanDefinition.getBeanName(),beanDefinition);

        }
        return beanDefinitions;
    }
    //实例化对象
    public Object getBean(String beanName){
        //判断是否已存在目标对象
        if (beanFactoryObjectCache.containsKey(beanName)){
            return beanFactoryObjectCache.get(beanName);
        }
        //获取 Class 所对应的 beanDefinition
        BeanDefinition beanDefinition = definition.get(beanName);
        //反射得到目标对象
        Object o = initBean(beanName,beanDefinition);
        BeanWarper beanWarper = new BeanWarper(o);
        beanFactoryInstanceCache.put(beanName,beanWarper);
        //DI 操作
        populateBean(beanWarper);
        return beanWarper.getWapperInstance();
    }

    private Object isInterface(BeanDefinition beanDefinition) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(beanDefinition.getClassName());
        if(clazz.isAnnotationPresent(MyController.class)){
            return null;
        }else if(clazz.isAnnotationPresent(MyService.class)){
            MyService myService = clazz.getAnnotation(MyService.class);
            String beanName = "";
            if("".equals(myService.value())){
                beanName = SpringUtil.changeName(clazz.getSimpleName());
            }else {
                beanName = myService.value();
            }
            if(beanName.equals(beanDefinition.getBeanName())){
                return null;
            }else {
               return getBean(beanName);
            }
        }else {
            return getBean(Class.forName(beanDefinition.getClassName()));
        }
    }

    private AdvisedSupport initAopConfog(BeanDefinitionReader beanDefinition) {
        AopConfig aopConfig = new AopConfig();
        Properties properties = beanDefinition.getResource();
        aopConfig.setAspectAfter(properties.getProperty("aspectAfter"));
        aopConfig.setAspectAfterThrow(properties.getProperty("aspectAfterThrow"));
        aopConfig.setAspectAfterThrowingName(properties.getProperty("aspectAfterThrowingName"));
        aopConfig.setAspectBefore(properties.getProperty("aspectBefore"));
        aopConfig.setAspectClass(properties.getProperty("aspectClass"));
        aopConfig.setPointCut(properties.getProperty("pointCut"));
        if(aopConfig.isAop()) {
            return new AdvisedSupport(aopConfig);
        }else {
            return null;
        }
    }

    private void populateBean(BeanWarper beanWarper) {
        Class<?> clazz = beanWarper.getWapperClass();
        //循环目标类所有 属性 找出带有 @Autowride 的属性并赋值
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if(!field.isAnnotationPresent(Autowired.class)){continue;}
            Autowired autowired = field.getAnnotation(Autowired.class);
            String value;
            Class<?> fieldClass = field.getType();
            if("".equals(autowired.name())){
                value = SpringUtil.changeName(fieldClass.getSimpleName());
            }else {
                value = autowired.name();
            }
            Object o ;
            if(beanFactoryInstanceCache.containsKey(value)){
                o = beanFactoryInstanceCache.get(value).getWapperInstance();
            }else {
                o = getBean(value);
            }
            try {
                field.set(beanWarper.getWapperInstance(),o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object initBean(String beanName, BeanDefinition beanDefinition) {
        Object o = null;
        try {
            //判断这个类的beanName 是否与 class 根据规则得到的 beanName 相符
            //如果不相符则执行 getBaen(clazz) 方法 返回所得对象
            //如果相符则返回 null
            o = isInterface(beanDefinition);
            Class<?> clazz = Class.forName(beanDefinition.getClassName());
            //如果为返回为 null 则进行反射实例化对象。不为 null则直接返回。
            if(null==o) {
                o = clazz.newInstance();
                //执行 aop
                //判断是否符合 aop 定义规则
                AdvisedSupport aopConfig = initAopConfog(beanDefinitionReader);
                if (aopConfig != null) {
                    aopConfig.setTarget(o);
                    aopConfig.setTargetClass(clazz);
                    if (aopConfig.pointCutMath()) {
                        Enhancer enhancer = new Enhancer();
                        enhancer.setSuperclass(clazz);
                        enhancer.setCallback(new CglibDynamicAopProxy(aopConfig));
                        o = enhancer.create();
                    }
                }
            }
            beanFactoryObjectCache.put(beanName,o);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    public Object getBean(Class<?> clazz){
        return getBean(SpringUtil.changeName(clazz.getSimpleName()));
    }

    public Map<String, Object> getBeanFactory() {
        return this.beanFactoryObjectCache;
    }

    public BeanDefinitionReader getBeanDefinitionReader() {
        return beanDefinitionReader;
    }
}

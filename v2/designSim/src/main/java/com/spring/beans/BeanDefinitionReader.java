package com.spring.beans;

import com.spring.annotation.MyController;
import com.spring.annotation.MyService;
import com.spring.util.SpringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class BeanDefinitionReader {

    private Properties properties = new Properties();
    private List<String> regitryBeanClasses = new ArrayList<>();

    public BeanDefinitionReader(String resource) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //扫描包下面的所有。class文件 并添加到 regitryBeanClasses 中。
        scan(properties.getProperty("scan"));
    }
    //扫面带有 @MyController @MyService 注解的类
    //接口的 BeanDefinition 中的 className字段则为对应的实现
    public List<BeanDefinition> loadBeanDefinition(){
        List<BeanDefinition> beanDefinitions = new LinkedList<>();
        for (String className:regitryBeanClasses){
            try {
                //获取反射对象
                Class<?> clazz = Class.forName(className);
                BeanDefinition b = new BeanDefinition();
                b.setClassName(className);
                //根据规则回去对应类的 beanName
                //如果既不是 @MyService @MyController注解的类则直接跳过。这种类我们不做管理。
                //如果是接口也直接跳过，如果接口所对应的实现类加有@MyService @MyController注解 则在对应的实现类进行 bean 定义的时候进行定义
                if(clazz.isAnnotationPresent(MyService.class)){
                    MyService myService = clazz.getAnnotation(MyService.class);
                    if("".equals(myService.value())){
                        b.setBeanName(SpringUtil.changeName(clazz.getSimpleName()));
                    }else {
                        b.setBeanName(myService.value());
                    }
                    b.setLazy(myService.isLazy());
                }else if(clazz.isAnnotationPresent(MyController.class)){
                    MyController myController = clazz.getAnnotation(MyController.class);
                    b.setBeanName(SpringUtil.changeName(clazz.getSimpleName()));
                    b.setLazy(myController.isLazy());
                }else {
                    continue;
                }
                beanDefinitions.add(b);
                //将类对应的接口进行定义
                for (Class<?> anInterface : clazz.getInterfaces()) {
                    BeanDefinition bInterfaca = new BeanDefinition();
                    bInterfaca.setBeanName(SpringUtil.changeName(anInterface.getSimpleName()));
                    bInterfaca.setClassName(className);
                    bInterfaca.setLazy(true);
                    beanDefinitions.add(bInterfaca);
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return beanDefinitions;
    }
    private void scan(String scanPath) {
        List<BeanDefinition> beanDefinitions = new LinkedList<>();
        URL url = this.getClass().getClassLoader().getResource( scanPath.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file: classPath.listFiles() ){
            if(file.isDirectory()){
                scan(scanPath+"."+file.getName());
            }else {
                if(!file.getName().endsWith(".class")){continue;}
                String className = scanPath + "." + file.getName().replace(".class","");
                regitryBeanClasses.add(className);
            }
        }
    }

    public Properties getResource() {
        return properties;
    }
}

package servlet.dispatcher;

import servlet.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispatchServlet extends HttpServlet {
    private Properties contextConfig = new Properties();
    private List<String> className = new LinkedList<>();
    private Map<String,Object> ioc = new HashMap<>();
    private Map<String,Object> hanlder =  new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
       int count = 0;
        String url = req.getRequestURI();

        if(!hanlder.containsKey(url)){resp.getWriter().write("404 not found "+url); return;}
        Map reqParam = req.getParameterMap();
        Method m = (Method) hanlder.get(url);
        Class<?>[] paramType = m.getParameterTypes();
        Object[] param = new Object[paramType.length];
        for (int i = 0;i < paramType.length; i++){
            if(paramType[i]==HttpServletRequest.class){
                param[i] = req;
            }else if(paramType[i]==HttpServletResponse.class){
                param[i] = resp;
            }else {
               Annotation[][] p =  m.getParameterAnnotations();
               Annotation annotations = p[count++][0];
               if(!(annotations instanceof RequestParam)){continue;}
               RequestParam requestParam = (RequestParam) annotations;
               if(!reqParam.containsKey(requestParam.value())){
                   resp.getWriter().write("400 param is not found");
               }
               if(paramType[i]==int.class){
                   param[i] = Integer.valueOf(Arrays.toString((String[])reqParam.get(requestParam.value()))
                           .replaceAll("\\[|\\]","")
                           .replaceAll("\\s+",","));
               }else {
                   param[i] = Arrays.toString((String[])reqParam.get(requestParam.value()))
                           .replaceAll("\\[|\\]","")
                           .replaceAll("\\s+",",");
               }
            }
        }
        m.getParameterAnnotations();
        Class<?> clazz = m.getDeclaringClass();
       resp.getWriter().write((String) m.invoke(ioc.get(changeName(clazz.getSimpleName())),param));
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //加载配置文件
        initWeb(config);
        //扫描类
        scan(contextConfig.getProperty("scan"));
        //初始化ioc容器
        initIoc();
        //依赖注入
        initDi();
        //初始化servlet
        initServlet();
    }

    private void scan(String scanPath) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPath.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file: classPath.listFiles() ){
            if(file.isDirectory()){
                scan(scanPath+"."+file.getName());
            }else {
                if(!file.getName().endsWith(".class")){continue;}
                String className = scanPath + "." + file.getName().replace(".class","");
                this.className.add(className);
            }
        }
    }

    private void initServlet() {
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MyController.class)){continue;}
            String parentMapping = "" ;
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                parentMapping = requestMapping.value();
            }
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(RequestMapping.class)){continue;}
                RequestMapping requestMapping =method.getAnnotation(RequestMapping.class);
                    String url = ("/"+parentMapping+"/"+requestMapping.value()).replaceAll("/+","/");
                if(hanlder.containsKey(url)){
                    try {
                        throw new Exception("url is alertly init");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                hanlder.put(url,method);
                System.out.println(url+"--------->"+method.getName());
            }
        }
    }

    private void initDi() {
        if(ioc.isEmpty()){return;}
        //遍历 ioc 中所有类的所有属性值 ，找到带有 @Autowried 的属性，并为其赋值
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] field = entry.getValue().getClass().getDeclaredFields();
            for (Field fieldOne : field) {
                if (!fieldOne.isAnnotationPresent(Autowired.class)){ continue; }
                Autowired autowired = fieldOne.getAnnotation(Autowired.class);
                String name;
                if("".equals(autowired.name())){
                    name = changeName(fieldOne.getType().getSimpleName());

                }else {
                    name =  autowired.name();
                }
                if(ioc.containsKey(name)){
                    fieldOne.setAccessible(true);
                    try {
                        fieldOne.set(entry.getValue(),ioc.get(name));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        throw new Exception("Class is not found");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void initIoc() {
        if(className.size()==0){return;}
        try {
            for(String className : this.className){
                Class<?> clazz = Class.forName(className);
                if (clazz.isInterface()){continue;}


                //装载 controller
                if (clazz.isAnnotationPresent(MyController.class)){
                    Object o = clazz.newInstance();
                    ioc.put(changeName(clazz.getSimpleName()),o);
                    initInterface(clazz,o);
                    //装载service
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    MyService myService = clazz.getAnnotation(MyService.class);
                    Object o = clazz.newInstance() ;
                    if("".equals(myService.value())){
                        ioc.put(changeName(clazz.getSimpleName()),o);
                    }else {
                        ioc.put(myService.value(),o);
                    }
                    initInterface(clazz,o);
                }else {
                    continue;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void initInterface(Class clazz,Object o ) {
        //将接口装载到ioc 容器中
        Class<?>[] clazzs = clazz.getInterfaces();
        for(Class clazzOne:clazzs){
            if(ioc.containsKey(changeName(clazzOne.getSimpleName()))){
                try {
                    throw new Exception("500 Impl Class is List");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ioc.put(changeName(clazzOne.getSimpleName()),o);
        }
    }

    private String changeName(String simpleName) {
        char[] chars = simpleName.toCharArray();
        if(chars[0]<='Z'&& chars[0] >= 'A'){
            chars[0] = (char) (chars[0] + 32);
        }
        return String.copyValueOf(chars);
    }

    private void initWeb(ServletConfig config) {
        String name =config.getInitParameter("contextConfigLocation").split(":")[1];
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

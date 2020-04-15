package com.spring.webmvc.sverlet;

import java.io.File;

public class ViewResolver {
    private final String DEFAULT = ".html";
    private File tempateRootDir;

    public ViewResolver(String tempateRootDir) {
       String path = this.getClass().getClassLoader().getResource(tempateRootDir).getFile();
       this.tempateRootDir = new File(path);
    }
    public ViewResolver(File tempateRootDir) {
        this.tempateRootDir = tempateRootDir;
    }

    /**
     * 获取对应的 html 文件 并封装成 view 对象
     */
    public View resolveViewName(String viewName){
        if(null==viewName||"".equals(viewName)){
            return null;
        }
        String name = viewName.endsWith(DEFAULT)? viewName : viewName+".html";
        File file = new File((tempateRootDir.getPath()+"/"+name).replaceAll("/+","/"));
        return new View(file);
    }
}

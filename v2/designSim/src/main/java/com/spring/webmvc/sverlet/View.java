package com.spring.webmvc.sverlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class View {
    private File viewFile;
    public View(File viewFile) {
        this.viewFile = viewFile;
    }

    public File getViewFile() {
        return viewFile;
    }

    public void render(HttpServletRequest req, HttpServletResponse resp, Map<String,?> model) throws IOException {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.viewFile,"r");
        String line = null;
        //读取文件内容，并用正则表达式解析模板语言
        while (null != (line=ra.readLine())){
            line = new String(line.getBytes("ISO-8859-1"),"utf-8");
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()){
                String key = matcher.group();
                key = key.replaceAll("￥\\{[^\\}]+\\}","");
                Object value = model.get(key);
                line = matcher.replaceFirst(makeStringForRegExp(value.toString()));
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }
        resp.setCharacterEncoding("utf-8");
        //返回到客户浏览器
        resp.getWriter().write(sb.toString());
    }
    //处理特殊字符
    public static String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}

package com.demo.controlller;


import com.demo.Service.TestService;
import com.demo.Service.TestServiceTwo;
import com.spring.annotation.Autowired;
import com.spring.annotation.MyController;
import com.spring.annotation.RequestMapping;
import com.spring.annotation.RequestParam;
import com.spring.webmvc.sverlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MyController
@RequestMapping("/demo")
public class TestDemo {
    @Autowired
    private TestService service;

    @Autowired
    private TestServiceTwo testServiceTwo;

    @RequestMapping("/view")
    public ModelAndView view(){
        return new ModelAndView("404");
    }
    @RequestMapping("/testOne")
    public String testOne(HttpServletResponse response, @RequestParam("name") String name, HttpServletRequest request,@RequestParam("age") int age){
        return service.testOne(name,age);
    }
    @RequestMapping("/testTwo")
    public String testTwo(@RequestParam("name") String name,@RequestParam("age") int age){
        return testServiceTwo.testTwo(name,age);
    }
}

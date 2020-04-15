package com.demo.aspect;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AopLogger {
    public void before(){
        log.debug("执行目标方法开始");
    }
    public void after(){
        log.debug("执行目标方法结束");
    }
    public void afterThrow(){
        log.debug("执行目标方法异常");
    }

}

package com.spring.aop.config;

public class AopConfig {
    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
    public Boolean isAop(){
        if (aspectClass==null||pointCut==null){
            return false;
        }else {
            return true;
        }
    }
    public AopConfig() {
    }
    public AopConfig(String pointCut, String aspectClass, String aspectBefore,
                     String aspectAfter, String aspectAfterThrow, String aspectAfterThrowingName) {
        this.pointCut = pointCut;
        this.aspectClass = aspectClass;
        this.aspectBefore = aspectBefore;
        this.aspectAfter = aspectAfter;
        this.aspectAfterThrow = aspectAfterThrow;
        this.aspectAfterThrowingName = aspectAfterThrowingName;
    }

    public String getPointCut() {
        return pointCut;
    }

    public void setPointCut(String pointCut) {
        this.pointCut = pointCut;
    }

    public String getAspectClass() {
        return aspectClass;
    }

    public void setAspectClass(String aspectClass) {
        this.aspectClass = aspectClass;
    }

    public String getAspectBefore() {
        return aspectBefore;
    }

    public void setAspectBefore(String aspectBefore) {
        this.aspectBefore = aspectBefore;
    }

    public String getAspectAfter() {
        return aspectAfter;
    }

    public void setAspectAfter(String aspectAfter) {
        this.aspectAfter = aspectAfter;
    }

    public String getAspectAfterThrow() {
        return aspectAfterThrow;
    }

    public void setAspectAfterThrow(String aspectAfterThrow) {
        this.aspectAfterThrow = aspectAfterThrow;
    }

    public String getAspectAfterThrowingName() {
        return aspectAfterThrowingName;
    }

    public void setAspectAfterThrowingName(String aspectAfterThrowingName) {
        this.aspectAfterThrowingName = aspectAfterThrowingName;
    }
}

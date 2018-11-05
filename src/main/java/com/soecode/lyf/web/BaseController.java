package com.soecode.lyf.web;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseController {

    // 第一种自定义数据绑定
    @InitBinder // 只针对当前的controller或继承的子controller
    public void initBinder(WebDataBinder webDataBinder){

        // 将页面传递的日期字符串数据，绑定成日期时间类数据
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));

        //
    }
}

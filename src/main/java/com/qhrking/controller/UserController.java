package com.qhrking.controller;

import com.jfinal.core.Controller;

/**
 * Created by qianhao on 2014/7/1.
 */
public class UserController extends Controller {
    public void index() {
        render("/user/list.vm");
    }
}

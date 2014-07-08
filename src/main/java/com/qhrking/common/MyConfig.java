package com.qhrking.common;

import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.RenderFactory;
import com.jfinal.render.ViewType;
import com.qhrking.controller.IndexController;
import com.qhrking.controller.UserController;
import com.qhrking.model.User;
import com.qhrking.render.VelocityLayoutRenderFactory;

import java.io.File;
import java.util.Properties;

/**
 * Created by qianhao on 2014/7/1.
 */
public class MyConfig extends JFinalConfig {

    @Override
    public void configConstant(Constants me) {
        Properties conf = loadPropertyFile("classes" + File.separator + "config.properties");
        Properties velocityConf = loadPropertyFile("classes" + File.separator + "velocity.properties");
        MyConstants.VELOCITY_TEMPLETE_PATH = getProperty("velocity_templete_path");
        System.out.println("configConstant");
        //设置开发模式
        me.setDevMode(getPropertyToBoolean("devMode", false));
        //指定开发页面的默认视图（jsp , freemarker ,velocity）
        me.setViewType(ViewType.OTHER);

        RenderFactory.setMainRenderFactory(new VelocityLayoutRenderFactory());

    }

    @Override
    public void configRoute(Routes me) {
        System.out.println("configRoute");
        me.add("/", IndexController.class);
        me.add("/user", UserController.class);
    }

    @Override
    public void configPlugin(Plugins me) {
        System.out.println("configPlugin");
        //数据库连接池插件
        DruidPlugin druidPlugin = new DruidPlugin(getProperty("jdbc.url"), getProperty("jdbc.username"), getProperty("jdbc.password"),  getProperty("jdbc.driver"));
        druidPlugin.setInitialSize(3).setMaxActive(10);
        me.add(druidPlugin);

        // 配置ActiveRecord插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.addMapping("user", User.class);
    }

    @Override
    public void configInterceptor(Interceptors me) {
        me.add(new SessionInViewInterceptor());
    }

    @Override
    public void configHandler(Handlers me) {

    }


    public static void main(String[] args) throws Exception {
        JFinal.start("E:\\Workspaces\\IDEA\\qhrking\\src\\main\\webapp", 2020, "/", 5);
    }
}

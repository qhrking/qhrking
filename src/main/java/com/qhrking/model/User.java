package com.qhrking.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by qianhao on 2014/7/1.
 */
public class User extends Model<User> {
    public static final User dao = new User();

    public User getByNameAndPassword(String email,String pwd){

        return dao.findFirst("select userId, realname, nickname, email, pwd from leap_user where email=? and pwd =?",email,pwd);

    }
}

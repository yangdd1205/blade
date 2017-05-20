package com.blade.demo;

import com.blade.mvc.annotation.PathParam;

/**
 * Created by biezhi on 2017/3/6.
 */
public class C {

    public void a(@PathParam String name) {
        System.out.println("aaaa:" + name);
    }

}

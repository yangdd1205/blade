package com.blade.demo;

import com.blade.Blade;

/**
 * Created by biezhi on 2017/3/2.
 */
public class App {

    public static void main(String[] args) {

        Blade.$().get("/hello/:hello", ((request, response) -> {
            String hello = request.pathString("hello");
            System.out.println("hello :" + hello);
            Blade.$().embedServer().addStatic("/test/a.txt");
        })).get("/hello/abc", ((request, response) -> System.out.println("abc..."))).get("/hello/:hello.html", ((request, response) -> {
            String hello = request.pathString("hello");
            System.out.println("hello.html :" + hello);
            Blade.$().route("/a/:name.html", C.class, "a");
            Blade.$().routeMatcher().update();
        })).get("/users/1", ((request, response) -> System.out.println("/users/1")))
                .get("/users/info/1", ((request, response) -> System.out.println("/users/info/1")))
                .before("/.*", ((request, response) -> System.out.println("全局before")))
                .before("/users/.*", ((request, response) -> System.out.println("users before")))
                .before("/users/info/.*", ((request, response) -> System.out.println("users info before")))
                .start(App.class, "/qa");

    }

}

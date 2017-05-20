package com.blade.demo.controller;

import com.blade.demo.model.Article;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.RestController;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author biezhi
 *         2017/5/20
 */
@RestController("/")
public class IndexController {

    @Route("hello")
    public void hello(@QueryParam Article article,
                      String username, Response response) {

        System.out.println(article);
        System.out.println(username);
        String value = null != article ? article.toString() : "no article";
        response.text(value);
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Method method = IndexController.class.getMethod("hello", Article.class, String.class, Response.class);
        System.out.println(method);

        Class<?>[] types = method.getParameterTypes();
        System.out.println(Arrays.toString(types));

        System.out.println(method.getParameters()[0].getAnnotations()[0]);
        System.out.println(method.getParameters()[0].getType());
        System.out.println(method.getParameters()[1].getAnnotations().length);

    }
}

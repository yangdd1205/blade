package com.blade.demo.controller;

import com.blade.demo.model.Article;
import com.blade.mvc.annotation.GetRoute;
import com.blade.mvc.annotation.QueryParam;
import com.blade.mvc.annotation.RestController;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.Response;

/**
 * @author biezhi
 *         2017/5/20
 */
@RestController("/")
public class IndexController {

    @GetRoute(values = "hello")
    public void hello(@QueryParam Article article,
                      String username, Response response) {

        System.out.println(article);
        System.out.println(username);
        String value = null != article ? article.toString() : "no article";
        response.text(value);
    }

}

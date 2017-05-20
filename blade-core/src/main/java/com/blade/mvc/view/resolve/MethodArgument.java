/**
 * Copyright (c) 2016, biezhi 王爵 (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blade.mvc.view.resolve;

import com.blade.exception.BladeException;
import com.blade.exception.RouteException;
import com.blade.kit.AsmKit;
import com.blade.kit.StringKit;
import com.blade.kit.reflect.ConvertKit;
import com.blade.kit.reflect.ReflectKit;
import com.blade.mvc.annotation.*;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.blade.mvc.http.wrapper.Session;
import com.blade.mvc.multipart.FileItem;
import com.blade.mvc.view.ModelAndView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public final class MethodArgument {

    public static Object[] getArgs(Request request, Response response, Method actionMethod) throws Exception {

        actionMethod.setAccessible(true);

        Parameter[] parameters = actionMethod.getParameters();
        Object[] args = new Object[parameters.length];
        String[] paramaterNames = AsmKit.getMethodParamNames(actionMethod);

        for (int i = 0, len = parameters.length; i < len; i++) {
            Parameter parameter = parameters[i];
            String paramName = paramaterNames[i];
            int annoLen = parameter.getAnnotations().length;
            Class<?> argType = parameter.getType();

            if (annoLen > 0) {
                QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
                if (null != queryParam) {

                    String name = StringKit.isBlank(queryParam.name()) ? paramName : queryParam.name();

                    if (ReflectKit.isBasicType(argType)) {
                        String val = request.query(name);
                        boolean required = queryParam.required();
                        if (StringKit.isBlank(val)) {
                            val = queryParam.defaultValue();
                        }
                        if (required && StringKit.isBlank(val)) {
                            throw new RouteException("query param [" + paramName + "] not is empty.");
                        }
                        args[i] = getRequestParam(argType, val);
                    } else {
                        try {
                            Field[] fields = argType.getDeclaredFields();
                            if (null == fields || fields.length == 0) {
                                continue;
                            }
                            Object obj = ReflectKit.newInstance(argType);
                            for (Field field : fields) {
                                field.setAccessible(true);
                                if (field.getName().equals("serialVersionUID")) {
                                    continue;
                                }
                                // article[title] => hello
                                String fieldName = name + "[" + field.getName() + "]";
                                String fieldValue = request.query(fieldName);
                                if (StringKit.isNotBlank(fieldValue)) {
                                    Object value = ConvertKit.convert(field.getType(), fieldValue);
                                    field.set(obj, value);
                                }
                            }
                            args[i] = obj;
                        } catch (NumberFormatException | IllegalAccessException | SecurityException e) {
                            throw new BladeException(e);
                        }
                    }
                }

                PathParam pathParam = parameter.getAnnotation(PathParam.class);
                if (null != pathParam) {
                    String name = StringKit.isBlank(pathParam.name()) ? paramName : pathParam.name();
                    String val = request.pathParam(name);
                    if (StringKit.isBlank(val)) {
                        val = pathParam.defaultValue();
                    }
                    args[i] = getRequestParam(argType, val);
                }

                HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
                if (null != headerParam) {
                    String key = StringKit.isBlank(headerParam.value()) ? paramName : headerParam.value();
                    String val = request.header(key);
                    boolean required = headerParam.required();
                    if (StringKit.isBlank(val)) {
                        val = headerParam.defaultValue();
                    }
                    if (required && StringKit.isBlank(val)) {
                        throw new RouteException("header param [" + paramName + "] not is empty.");
                    }
                    args[i] = getRequestParam(argType, val);
                }

                // cookie param
                CookieParam cookieParam = parameter.getAnnotation(CookieParam.class);
                if (null != cookieParam) {
                    String cookieName = StringKit.isBlank(cookieParam.value()) ? paramName : cookieParam.value();
                    String val = request.cookie(cookieName);
                    boolean required = cookieParam.required();
                    if (StringKit.isBlank(val)) {
                        val = cookieParam.defaultValue();
                    }
                    if (required && StringKit.isBlank(val)) {
                        throw new RouteException("cookie param [" + paramName + "] not is empty.");
                    }
                    args[i] = getRequestParam(argType, val);
                }

                // form multipart
                MultipartParam multipartParam = parameter.getAnnotation(MultipartParam.class);
                if (null != multipartParam && argType == FileItem.class) {
                    String name = StringKit.isBlank(multipartParam.value()) ? paramName : multipartParam.value();
                    args[i] = request.fileItem(name);
                }
            } else {
                if (ReflectKit.isBasicType(argType)) {
                    args[i] = request.query(paramName);
                } else {
                    if (argType == Request.class) {
                        args[i] = request;
                        continue;
                    } else if (argType == Response.class) {
                        args[i] = response;
                    } else if (argType == Session.class) {
                        args[i] = request.session();
                    } else if (argType == ModelAndView.class) {
                        args[i] = new ModelAndView();
                    } else if (argType == Map.class) {
                        args[i] = request.querys();
                    } else {
                        try {
                            Field[] fields = argType.getDeclaredFields();
                            if (null == fields || fields.length == 0) {
                                continue;
                            }
                            Object obj = ReflectKit.newInstance(argType);
                            for (Field field : fields) {
                                field.setAccessible(true);
                                if (field.getName().equals("serialVersionUID")) {
                                    continue;
                                }
                                String fieldValue = request.query(field.getName());
                                if (StringKit.isNotBlank(fieldValue)) {
                                    Object value = ConvertKit.convert(field.getType(), fieldValue);
                                    field.set(obj, value);
                                }
                            }
                            args[i] = obj;
                        } catch (NumberFormatException | IllegalAccessException | SecurityException e) {
                            throw new BladeException(e);
                        }
                    }
                }
            }
        }
        return args;
    }

    public static Object getRequestParam(Class<?> parameterType, String val) {
        Object result = null;
        if (parameterType.equals(String.class)) {
            return val;
        }
        if (StringKit.isBlank(val)) {
            if (parameterType.equals(int.class) || parameterType.equals(double.class) ||
                    parameterType.equals(long.class) || parameterType.equals(byte.class) || parameterType.equals(float.class)) {
                result = 0;
            }
            if (parameterType.equals(boolean.class)) {
                result = false;
            }
        } else {
            if (parameterType.equals(Integer.class) || parameterType.equals(int.class)) {
                result = Integer.parseInt(val);
            }
            if (parameterType.equals(Long.class) || parameterType.equals(long.class)) {
                result = Long.parseLong(val);
            }
            if (parameterType.equals(Double.class) || parameterType.equals(double.class)) {
                result = Double.parseDouble(val);
            }
            if (parameterType.equals(Float.class) || parameterType.equals(float.class)) {
                result = Float.parseFloat(val);
            }
            if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
                result = Boolean.parseBoolean(val);
            }
            if (parameterType.equals(Byte.class) || parameterType.equals(byte.class)) {
                result = Byte.parseByte(val);
            }
        }
        return result;
    }

}

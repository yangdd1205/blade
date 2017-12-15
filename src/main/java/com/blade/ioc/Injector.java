package com.blade.ioc;

/**
 * Bean Injector interface
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.5
 */
public interface Injector {

    /**
     * Injection bean
     *
     * @param bean bean instance
     */
    void injection(Object bean);

}
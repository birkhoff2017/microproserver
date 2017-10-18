package com.ycb.zprovider.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by zhuhui on 17-7-27.
 */
@Configuration
public class AuthInterceptor extends WebMvcConfigurerAdapter {
    @Bean
    AuthHandlerInterceptor authHandlerInterceptor() {
        return new AuthHandlerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authHandlerInterceptor())
                .addPathPatterns("/creditcreate/createOrder")
                .addPathPatterns("/machineinfo/getMachineInfo")
                .addPathPatterns("/order/getOrderList")
                .addPathPatterns("/order/getOrderStatus")
                .excludePathPatterns("/shop/getShopList")
                .excludePathPatterns("/shop/getShopInfo")
                .excludePathPatterns("/gateway/notify");
        //开发环境中注释掉，拦截器不起作用，方便开发，
        // super.addInterceptors(registry);
    }
}

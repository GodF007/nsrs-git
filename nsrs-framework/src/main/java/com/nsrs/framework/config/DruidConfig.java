package com.nsrs.framework.config;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Druid监控配置类
 * 配置Druid数据源的监控页面和统计功能
 */
@Configuration
public class DruidConfig {

    /**
     * 配置Druid监控页面的Servlet
     * 访问地址：http://localhost:8088/nsrs/druid/
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> registrationBean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        
        // 设置登录用户名和密码
        registrationBean.addInitParameter("loginUsername", "admin");
        registrationBean.addInitParameter("loginPassword", "admin123");
        
        // 设置允许访问的IP，为空或者null时，表示允许所有访问
        registrationBean.addInitParameter("allow", "");
        
        // 设置拒绝访问的IP，优先级高于allow
        // registrationBean.addInitParameter("deny", "192.168.1.100");
        
        // 是否能够重置数据（生产环境建议设置为false）
        registrationBean.addInitParameter("resetEnable", "false");
        
        return registrationBean;
    }

    /**
     * 配置Web监控的Filter
     * 用于采集web-jdbc关联监控的数据
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> registrationBean = new FilterRegistrationBean<>(new WebStatFilter());
        
        // 添加过滤规则
        registrationBean.addUrlPatterns("/*");
        
        // 添加不需要忽略的格式信息
        registrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        
        return registrationBean;
    }

}
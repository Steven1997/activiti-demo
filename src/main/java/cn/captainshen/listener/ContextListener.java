package cn.captainshen.listener;

import cn.captainshen.Deploy;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //Deploy.deploy();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}

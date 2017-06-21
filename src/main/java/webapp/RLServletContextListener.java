package webapp;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by suat on 17-Jun-17.
 */
public class App implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener destroyed");
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        System.out.println("ServletContextListener started");
    }
}
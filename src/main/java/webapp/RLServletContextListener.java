package webapp;


import org.apache.log4j.Logger;
import tez.algorithm.collaborative_learning.SparkStateClassifier;
import tez.environment.real.ContextChangeListener;
import tez.environment.real.control.ControlGroupScheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by suat on 17-Jun-17.
 */
public class RLServletContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(RLServletContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        ControlGroupScheduler.getInstance().stopNotificationSender();
        ContextChangeListener.getInstance().stopListening();
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        ControlGroupScheduler.getInstance().startNotificationSender();
        ContextChangeListener.getInstance().subscribeToUserContextChanges();
        SparkStateClassifier.getInstance().loadRandomForestClassifier("../webapps/learningserver/WEB-INF/classes/rdfClassifier10x10000");
        System.out.println("Appplication started");
        logger.info("Appplication started");
    }
}
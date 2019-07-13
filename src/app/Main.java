package app;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class Main {


    public static void main(String[] args)
            throws Exception {
        App.generateSeeds(1000);

        //https://docs.oracle.com/javase/tutorial/jmx/mbeans/standard.html?fbclid=IwAR3oAU4GhgffJ5O6Xf0cAEEgtyRag67PonUzTVoQNJqLbzKAiLxKIY-l_kk
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("app:type=App");
        App mbean = new App();
        mbs.registerMBean(mbean, name);


//        while(true){
//            synchronized (System.out){
//                System.out.println(mbean.getDescription());
//            }
//
//            Thread.currentThread().sleep(1000L);
//        }
        System.out.println("Waiting forever...");
        Thread.sleep(Long.MAX_VALUE);
    }
}

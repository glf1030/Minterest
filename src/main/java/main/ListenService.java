package main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;



public class ListenService implements ServletContextListener
{
    private MessageListenerDeamon messageListenerDeamon = null;

    /**
     * 创建一个初始化监听器对象，一般由容器调用
     */
    public ListenService()
    {
        super();
    }

    /**
     * 让Web程序运行的时候自动加载Timer
     */
    public void contextInitialized(ServletContextEvent e)
    {        
    	
    	System.out.println("Starting Tao Bao Auto Rideo Service...");

 
        if (null == messageListenerDeamon)
        {
            messageListenerDeamon = new MessageListenerDeamon();
        }
       
        System.out.println("Starting new thread");
        messageListenerDeamon.start();
    }

    /**
     * 该方法由容器调用 空实现
     */
    public void contextDestroyed(ServletContextEvent e)
    {
        if (null != messageListenerDeamon)
        {
            messageListenerDeamon.interrupt();
        }
    }
}

package main;


import java.io.FileInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crawl.GoogleCrawler;
import crawl.TaoBaoCrawler;





public class MessageListenerDeamon extends Thread
{
    Log log = LogFactory.getLog(MessageListenerDeamon.class);

    java.util.Properties prop=new 	java.util.Properties();
    public MessageListenerDeamon()
    {
        setDaemon(true);
    }
    
    @Override
    public void run() 
    {
        // 获取消息请求检查时间间隔，单位：秒
     
    
   //     try {
        	
    long	queueMessageCheckDuration=24*60*60*1000;
        
        	//System.out.println("loading movies");
        //   String duration=prop.get("Duration").toString();
			//new TaoBaoCrawler().crawl();
             try {
				GoogleCrawler.crawler();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            while (true)
            {
                try {
					Thread.sleep(queueMessageCheckDuration * 7);
					System.out.println("Sleeping for next task start");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
//        }
//        catch (InterruptedException e)
//        {
//            log.error("Exception: " + e.toString());
//        } 
		
    }
}


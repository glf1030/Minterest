package crawl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.concurrent.BlockingQueue;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class GoogleCrawler 
{
	
	public static void main(String[] args)
	{
		try {
			PropertyConfigurator.configure("log4j.properties");
			GoogleCrawler.crawler();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static void crawler() throws InterruptedException
	{
		Logger taskLog=Logger.getLogger("task");
		ExecutorService service = Executors.newCachedThreadPool();  
		//Creating shared object
		BlockingQueue<String> sharedQueue = new LinkedBlockingQueue<String>();
		BlockingQueue<Boolean> taskMonitorQueue=new LinkedBlockingQueue<Boolean>();
		taskMonitorQueue.put(false);


		//Creating Producer and Consumer Thread
		Thread prodThread = new Thread(new Producer(sharedQueue,taskMonitorQueue));
		Thread consThread = new Thread(new Consumer(sharedQueue,taskMonitorQueue));


		//Starting producer and Consumer thread
		service.submit(prodThread);
		service.submit(consThread);

		service.shutdown();
		taskLog.info("main thread is shutdown");

	}




}

//Producer Class in java
class Producer implements Runnable 
{

	Logger taskLog=Logger.getLogger("task");
	Grawler crawler = new Grawler();

	private final BlockingQueue<String> sharedQueue;
	private final BlockingQueue<Boolean> taskMonitorQueue;



	public Producer(BlockingQueue<String> sharedQueue,BlockingQueue<Boolean> taskMonitorQueue) 
	{
		this.sharedQueue =sharedQueue;
		this.taskMonitorQueue=taskMonitorQueue;

	}

	public synchronized void run() 
	{

		BlockingQueue<String> movieList = new LinkedBlockingQueue<String>();
		try
		{

			BufferedReader readerMovie=new BufferedReader(new FileReader("./movieList"));
			String movieName="";
			while((movieName=readerMovie.readLine())!=null)
			{
				String s=movieName;

				movieList.add(s);
			}

			readerMovie.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();                               
		System.out.println(sf.format(date));    
		int movieListSize=movieList.size();
		for(int i=0;i<movieListSize;i++)
		{
			try
			{
				String movieName=movieList.poll();
				ArrayList<String> movieGoogleSearch=null;//crawler.crawler_google_image_htmlFormat(sf.format(date),movieName,movieId);
				for(int j=0;j<movieGoogleSearch.size();j++)
				{
					System.out.println("I am putting "+ movieGoogleSearch.get(j));
					taskLog.info("I am putting"+movieGoogleSearch.get(j));
					sharedQueue.put(movieGoogleSearch.get(j));
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		try {
			taskMonitorQueue.poll();
			taskMonitorQueue.put(true);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		taskLog.info("Producer has finished");
		System.out.println("Producer has finished");

	}

}


//Consumer Class in Java
class Consumer implements Runnable
{
	Logger taskLog=Logger.getLogger("task");

	int corePoolSize=50;
	int maxiumPoolSize=100;
	long keepLiveTime=1000*60;
	


	ArrayList<Future<String>> futureList=new ArrayList<Future<String>>();

	private final BlockingQueue<String> sharedQueue;
	private final BlockingQueue<Boolean> taskMonitorQueue;




	public Consumer (BlockingQueue<String> sharedQueue,BlockingQueue<Boolean>taskMonitorQueue) 
	{
		this.sharedQueue = sharedQueue;
		this.taskMonitorQueue=taskMonitorQueue;

	}

	public synchronized void run() 
	{
	//	ExecutorService threadPool = Executors.newFixedThreadPool(maxiumaThreadN);

		ExecutorService threadPool=new ThreadPoolExecutor(corePoolSize, maxiumPoolSize, keepLiveTime, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());    

		while(true)
		{


			String folder="";

			if(!sharedQueue.isEmpty())
			{
				try 
				{
					folder=sharedQueue.take();

				} catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final String folderName=folder;
				//			    Future<String> future = 
				//			    		threadPool.submit(new Callable<String>() 
				//			    {  
				//			    	
				//		            public String call() throws Exception 
				//		            {  
				//		            	Test1 t=new Test1(folderName);
				//		            	//IC.start(0);   //Here
				//		            	System.out.println("submit task "+folderName);
				//						t.r();
				//						return folderName+" is done";
				//		            }  
				//		        });

				Runnable run = new Runnable() 
				{
					public void run()
					{
						taskLog.info("Consuming:"+folderName);
						System.out.println("Consuming "+folderName);
						Test1 t=new Test1(folderName);
						try {
							t.r();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				// 在未来某个时间执行给定的命令

				threadPool.execute(run);
				
				System.out.println(sharedQueue.size());

			}
			else if(taskMonitorQueue.peek())
			{
				System.out.println("-----------------------------------");
				break;
			}





			//            	int currentThread=0;
			//            	while(currentThread<maxiumaThreadN)
			//            	{
			//            		
			//            		String folder=null;
			//					try 
			//					{
			//						if(!sharedQueue.isEmpty())
			//						{
			//							System.out.println("current sharedQueue has"+sharedQueue.size());
			//							Object[] queue=sharedQueue.toArray();
			//							for(int j=0;j<queue.length;j++)
			//								System.out.println(queue[j].toString());
			//						folder = sharedQueue.take();
			//						currentThread++;
			//						}
			//						
			//					} catch (InterruptedException e) 
			//					{
			//						// TODO Auto-generated catch block
			//						e.printStackTrace();
			//					}
			//					if(folder!=null)
			//					{
			//						  
			//					   // System.out.println("starting thread" +i+ "Consumed:"+ folder);
			//					    Log.info("starting thread" +currentThread+ "Consumed:"+ folder);
			//					    final String folderName=folder;
			//					    Future<String> future = threadPool.submit(new Callable<String>() 
			//					    {  
			//				            public String call() throws Exception 
			//				            {  
			//				            	Test1 t=new Test1(folderName);
			//				            	//IC.start(0);   //Here
			//								t.r();
			//								return folderName+" is done";
			//  
			//				            }  
			//				        });
			//					    futureList.add(future);
			//					    					
			//					}
			//            	}
			////            	System.out.println("-----------------------------------");
			////            	System.out.println(futureList.size());
			//           for(int i=0;i<futureList.size();i++)
			//           {
			//        	   try 
			//        	{
			//        		  
			//        		   Log.info(futureList.get(i).get(1000*60*60,TimeUnit.MILLISECONDS)+"is done");
			////        		   System.out.println(futureList.get(i).get());
			////				   System.out.println(futureList.get(i).get(1000*60*60,TimeUnit.MILLISECONDS)+"is done");
			//	   
			//			} 
			//        	  catch (InterruptedException e) 
			//			{
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			} catch (ExecutionException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			} catch (TimeoutException e) 
			//			{
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
			//           }
			//           
			//           futureList=new ArrayList<Future<String>>();


		}
		taskLog.info("Consumer has finished");
		System.out.println("Consumer has finished");
		threadPool.shutdown();
//		notifyAll();
	}

}

class Test1
{
	Logger taskLog=Logger.getLogger("task");
	String fileName="";
	public  Test1(String s)
	{
		fileName=s;
	}

	public synchronized void r() throws InterruptedException
	{
		taskLog.info(fileName+"is working");
		System.out.println(fileName+"is working");
		   ImageCollector ic=new ImageCollector();
		   ic.singleStart(fileName);
		Thread.sleep(1000*60*10);
		taskLog.info(fileName+" has finished");
		System.out.println(fileName);

	}

}





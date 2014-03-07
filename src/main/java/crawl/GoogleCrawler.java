package crawl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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

import database.MysqlDatabase;
import entity.MovieItem;


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
		BlockingQueue<HashMap<String,String[]>> sharedQueue = new LinkedBlockingQueue<HashMap<String,String[]>>();
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

	MysqlDatabase mdb=new MysqlDatabase();
	private final BlockingQueue<HashMap<String,String[]>> sharedQueue;
	private final BlockingQueue<Boolean> taskMonitorQueue;



	public Producer(BlockingQueue<HashMap<String,String[]>> sharedQueue,BlockingQueue<Boolean> taskMonitorQueue) 
	{
		this.sharedQueue =sharedQueue;
		this.taskMonitorQueue=taskMonitorQueue;

	}

	public synchronized void run() 
	{

		BlockingQueue<String> movieIDList = new LinkedBlockingQueue<String>();
		try
		{

			BufferedReader readerMovie=new BufferedReader(new FileReader("./movieList"));
			String movieID="";
			while((movieID=readerMovie.readLine())!=null)
			{
				String s=movieID;

				movieIDList.add(s);
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
		int movieListSize=movieIDList.size();
		for(int i=0;i<movieListSize;i++)
		{
			
			
				String movieID=movieIDList.poll();
				
				MovieItem mi=mdb.getMovieItemByMovieID(movieID);
				System.out.println(mi.get_movie_name());
				BlockingQueue<String> queryList=mdb.getGoogleImageQueryList();
				
				int querySize=queryList.size();
				for(int j=0;j<2;j++)
				{
					String site_query=queryList.poll();
					System.out.println(site_query);
					crawler.crawler_google_image_htmlFormat(sf.format(date),mi,site_query,sharedQueue);

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

	int corePoolSize=10;
	int maxiumPoolSize=20;
	long keepLiveTime=1000*60;
	


	ArrayList<Future<String>> futureList=new ArrayList<Future<String>>();

	private final BlockingQueue<HashMap<String,String[]>> sharedQueue;
	private final BlockingQueue<Boolean> taskMonitorQueue;




	public Consumer (BlockingQueue<HashMap<String,String[]>> sharedQueue,BlockingQueue<Boolean>taskMonitorQueue) 
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


			HashMap<String,String[]>  folder=new HashMap<String,String[]>();

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
				final MovieItem mi=new MovieItem();
				mi.set_movie_name(folder.get("movieName")[0]);
				mi.set_movie_id(folder.get("movieID")[0]);
				mi.set_director(folder.get("director")[0]);
				mi.set_actor_list(folder.get("actors"));
				
				final String folderName=folder.get("folderName")[0];
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
						Test1 t=new Test1(mi,folderName);
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
				
				System.out.println("sharedQueue has "+sharedQueue.size());
				System.out.println(sharedQueue.isEmpty());
				//System.out.println("taskMonitor"+taskMonitorQueue.peek());
			}
			else if(taskMonitorQueue.peek())
			{
				taskLog.info("-----------------------------------");
				taskLog.info("finished all the tasks");
				System.out.println("-----------------------------------");
				break;
			}
			else
			{
				taskMonitorQueue.peek();
			}
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
	MovieItem mi=null;
	public  Test1(MovieItem mi,String s)
	{
		this.mi=mi;
		this.fileName=s;
	}

	public synchronized void r() throws InterruptedException
	{
		taskLog.info(fileName+"is working");
		System.out.println(fileName+"is working");
		 ImageCollector ic=new ImageCollector();
		ic.multipleStart(fileName, mi);
		  // ic.singleStart(fileName);
		Thread.sleep(1000*60);
		taskLog.info(fileName+" has finished");
		System.out.println(fileName+"has finished");

	}

}





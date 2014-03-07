package collector.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import util.Parameters;
import entity.GoogleHtmlObject;
import entity.MovieItem;
import entity.WebImageObject;

/**
 * Implement multiple threads image crawler from google image
 * @author ys439
 *
 */
public class MultipleThreadCollector {
	static final Logger LOG = Logger.getLogger(SingleThreadCollector.class.getName());
	static final Logger WARN = Logger.getLogger(SingleThreadCollector.class.getName());
	
	

	/**
	 * 
	 * @param itemPath: folder with html files
	 * @param movieItem
	 */
	public static void start(String html, MovieItem movieItem){
		
		PropertyConfigurator.configure("log4j.properties");
        File htmlFile=new File(html);
		if(!htmlFile.getName().endsWith("htm")&&(!htmlFile.getName().endsWith("html"))) return;
		else{
			long startTime = System.currentTimeMillis();
			ExecutorService executor = new ThreadPoolExecutor(Parameters.THREAD_NUM, 20, 60*1000, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());    

			String htmlPath = htmlFile.getAbsolutePath();
			ArrayList<GoogleHtmlObject> googleHtmlObjects = GoogleHtmlParser.getGoogleHtmlItems(htmlPath);
			for(GoogleHtmlObject googleHtmlObj:googleHtmlObjects){
				MyGoogleItemTask myTask = new MyGoogleItemTask(googleHtmlObj, movieItem);
				executor.execute(myTask);
				LOG.info(Thread.currentThread().getName()+"\tAdd new task to queue...");
			}
			executor.shutdown();
			try {
				executor.awaitTermination(Parameters.MAX_HTML_PROCESS_SEC_FOR_ONE_ITEM*googleHtmlObjects.size(), TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOG.info(Thread.currentThread().getName()+"\t"+e.getMessage());
				e.printStackTrace();
			}
			LOG.info(Thread.currentThread().getName()+"\tYeah!haha Google HTML finished "+html+" \tTIMEOUT="+(System.currentTimeMillis()-startTime)/1000 +"secs");
		}
	}
	
	public static void main(String args[]){
		
		MovieItem movieItem = new MovieItem();
		movieItem.set_movie_name("来自星星的你");
		movieItem.set_movie_id("xxxxxxxx");
		movieItem.set_director("");
		movieItem.set_actor_list(new String[]{"全智贤","金秀贤"});
		
		String path = "e:/crawlerdata/来自星星的你/";
//		MysqlDatabase md=new MysqlDatabase();
//		ArrayList<MovieItem> miList=md.batchsearchMovieDataFromMysql();
		
		MultipleThreadCollector.start(path,movieItem);
		
	}
}

class MyGoogleItemTask implements Runnable {
	static final Logger LOG = Logger.getLogger(SingleThreadCollector.class.getName());
	GoogleHtmlObject googleHTMLObj;
	MovieItem movieItem;
	public MyGoogleItemTask(GoogleHtmlObject googleHTMLObj,MovieItem movieItem) {
		// TODO Auto-generated constructor stub
		this.googleHTMLObj = googleHTMLObj;
		this.movieItem = movieItem;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		long startTime = System.currentTimeMillis();
		ArrayList<WebImageObject> webImageObjects = WebPageParser.getImageObjectfromPage(googleHTMLObj.webUrl);
		TargetImageSelector targetImage = new TargetImageSelector(googleHTMLObj, webImageObjects,movieItem);
		
		if(targetImage==null || targetImage.googleObject==null || targetImage.webImageObject==null)return;
		
		RideoItemConstructor rideoItemConstructor = new RideoItemConstructor(targetImage, movieItem.get_movie_id(), movieItem.get_movie_name());
		ImageStorageManager storageManager = new ImageStorageManager(rideoItemConstructor.getRideoItem());
		Boolean isSuccess = storageManager.save2Mysql();
		
		if(isSuccess)storageManager.save2Local();
		LOG.info(Thread.currentThread().getName()+"Google Item finished, TIMEOUT="+(System.currentTimeMillis()-startTime)/1000 +"secs");
	}

}

package collector.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.esotericsoftware.minlog.Log;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.sun.media.jai.codecimpl.WBMPCodec;

import query.MovieQueryFactory;
import collector.parser.ImageStorageManager;
import collector.parser.RideoItemConstructor;
import collector.parser.GoogleHtmlParser;
import collector.parser.TargetImageSelector;
import collector.parser.WebPageParser;
import database.MysqlDatabase;
import entity.GoogleHtmlObject;
import entity.MovieItem;
import entity.WebImageObject;


/**
 * Entry for the image collector
 * @author ys439
 *
 */
public class SingleThreadCollector {
	
	static final Logger LOG = Logger.getLogger(SingleThreadCollector.class.getName());
	static final Logger WARN = Logger.getLogger(SingleThreadCollector.class.getName());

	/**
	 * 
	 * @param itemPath: folder with html files
	 * @param movieItem
	 */
	public static void start(String itemPath, MovieItem movieItem){
		
		PropertyConfigurator.configure("log4j.properties");
		File dir = new File(itemPath);
		File[] htmls = dir.listFiles();
		for(File html:htmls){
			if(html.isDirectory())continue;
			else if(!html.getName().endsWith("htm")&&(!html.getName().endsWith("html")))continue;
			else{
				String htmlPath = html.getAbsolutePath();
				ArrayList<GoogleHtmlObject> googleHtmlObjects = GoogleHtmlParser.getGoogleHtmlItems(htmlPath);
				for(GoogleHtmlObject googleHtmlObj:googleHtmlObjects){
					String webUrl = googleHtmlObj.webUrl;
					
					ArrayList<WebImageObject> webImageObjects = WebPageParser.getImageObjectfromPage(webUrl);
					TargetImageSelector targetImage = new TargetImageSelector(googleHtmlObj, webImageObjects,movieItem);
					
					if(targetImage==null || targetImage.googleObject==null || targetImage.webImageObject==null)continue;
					
					RideoItemConstructor rideoItemConstructor = new RideoItemConstructor(targetImage, movieItem.get_movie_id(), movieItem.get_movie_name());
					ImageStorageManager storageManager = new ImageStorageManager(rideoItemConstructor.getRideoItem());
					Boolean isSuccess = storageManager.save2Mysql();
					
					if(isSuccess)storageManager.save2Local();
					
					
				}
			}
			
		}
	}
	
	public static void main(String args[]){
		
		MovieItem movieItem = new MovieItem();
		movieItem.set_movie_name("辣妈正传");
		movieItem.set_movie_id("xxxxxxxx");
		movieItem.set_director("");
		movieItem.set_actor_list(new String[]{"孙俪"});
		
		String path = "e:/crawlerdata/辣妈正传/";
//		MysqlDatabase md=new MysqlDatabase();
//		ArrayList<MovieItem> miList=md.batchsearchMovieDataFromMysql();
		
		SingleThreadCollector.start(path,movieItem);
		
	}
	

}

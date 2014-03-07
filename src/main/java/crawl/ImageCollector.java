package crawl;

import org.apache.log4j.Logger;

import collector.parser.MultipleThreadCollector;
import collector.parser.SingleThreadCollector;
import entity.MovieItem;


/**
 * Entry for ImageCollecor
 * @author ys439
 *
 */
public class ImageCollector {
	static final Logger LOG = Logger.getLogger(ImageCollector.class.getName());
	static final Logger WARN = Logger.getLogger(ImageCollector.class.getName());
	
	/**
	 * Single thread
	 * @param itemPath: folder with html files
	 * @param movieItem
	 */
	public static void singleStart(String itemPath, MovieItem movieItem){
		
		SingleThreadCollector.start(itemPath,movieItem);
	}
	
	/**
	 * Multiple thread, faster than single one, Thread number set in util.Parameters
	 * @param itemPath
	 * @param movieItem
	 */
	public  void multipleStart(String itemPath, MovieItem movieItem){
		MultipleThreadCollector.start(itemPath,movieItem);
	}
	
	public static void main(String args[]) {

		MovieItem movieItem = new MovieItem();
		movieItem.set_movie_name("辣妈正传");
		movieItem.set_movie_id("xxxxxxxx");
		movieItem.set_director("");
		movieItem.set_actor_list(new String[] { "孙俪" });

		String path = "e:/crawlerdata/辣妈正传/";

		ImageCollector.singleStart(path, movieItem);
		//ImageCollector.multipleStart(path, movieItem);

	}

}

package collector.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.model.fit.RANSAC;

import cern.colt.Arrays;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import entity.GoogleHtmlObject;
import entity.MovieItem;
import entity.WebImageObject;
import util.ImageCollectorUtils;
import util.Parameters;

/**
 * Find the target image from image urls in source webpage
 * @author ys439
 *
 */
public class TargetImageSelector {
	
	static final Logger LOG = Logger.getLogger(TargetImageSelector.class.getName());
	static final Logger WARN = Logger.getLogger(TargetImageSelector.class.getName());
	
	private static final int IMG_WIDTH = 720;
	
	public GoogleHtmlObject googleObject = null;
	public WebImageObject webImageObject = null;
	
	public TargetImageSelector(GoogleHtmlObject gobj, ArrayList<WebImageObject> imgObjs, MovieItem movieItem) {
		
		LOG.warn(Thread.currentThread().getName()+"\tStart to find target image...");
		if(imgObjs==null || imgObjs.size()==0){
			LOG.warn("No image crawled from the source page..."+gobj.webUrl);
			return;
		}
		// validation
		String summary = gobj.summary;
		String title = imgObjs.get(0).title;
		if (!isValidItem(movieItem, title, summary))
			return;

		// if valid, continue to build targetObject
		String queryUrlStr = gobj.smallImageUrl;
		WebImageObject targeObj = getTargetImage(queryUrlStr, imgObjs);
		
		if (targeObj == null) {
			LOG.warn("No image found by image matching program...Maybe caused by small image size, or ioexception");
			return;
		}

		this.webImageObject = targeObj;
		this.googleObject = gobj;
		if(this.webImageObject==null){
			LOG.warn("webImageObject is null");
		}
		else{
			LOG.info("not null\t"+this.webImageObject.url);
		}
		
		File rootDir = new File(googleObject.saveRootDir);
		if(!rootDir.exists()||!rootDir.isDirectory()){
			rootDir.mkdir();
			this.webImageObject.addr = rootDir+"/0."+ImageCollectorUtils.getURLExtension(webImageObject.url);
		}
		else{
			File[] images = rootDir.listFiles();
			int index = images.length;
			this.webImageObject.addr = rootDir+"/"+String.valueOf(index)+"."+ImageCollectorUtils.getURLExtension(webImageObject.url);
			LOG.info(this.webImageObject.addr);
		}
		
	}
	
	private static WebImageObject getTargetImage(String queryurlstr, ArrayList<WebImageObject> targetImageObjs){
		
		Map<WebImageObject,Integer> map = new HashMap<WebImageObject,Integer>();
		Map<WebImageObject,Integer> mapSorted = new TreeMap<WebImageObject,Integer>();
		
		DoGSIFTEngine engine = new DoGSIFTEngine();	
		
		MBFImage queryImg;
		LocalFeatureList<Keypoint> queryKeypoints = null;
		try {
			InputStream in = ImageCollectorUtils.getInputStreamFromURL(queryurlstr);
			BufferedImage bImg = ImageIO.read(in);
			
			if (bImg.getWidth() > IMG_WIDTH)
				bImg = resizeImage(bImg, IMG_WIDTH);
			
			queryImg = ImageUtilities.createMBFImage(bImg, false);
			queryKeypoints = engine.findFeatures(queryImg.flatten());
			in.close();
		} catch (IOException e) {
			WARN.warn(Thread.currentThread().getName()+"\t"+e.getMessage());
			return null;
		}finally{
			
		}
			
		LocalFeatureList<Keypoint> targetKeypoints = null;
		for(WebImageObject targetImageObj:targetImageObjs){
			MBFImage targetImg;
			try {
				InputStream in = ImageCollectorUtils.getInputStreamFromURL(targetImageObj.url);
				if(in==null)continue;
				BufferedImage bImg = ImageIO.read(in);
				if(bImg==null)continue;
				int width = bImg.getWidth();
				int height = bImg.getHeight();
				if(width<Parameters.WIDTH || height<Parameters.HEIGHT){
					continue;
				}
				targetImageObj.width = width;
				targetImageObj.height = height;
				
				if (bImg.getWidth() > IMG_WIDTH)
					bImg = resizeImage(bImg, IMG_WIDTH);
				
				targetImg = ImageUtilities.createMBFImage(bImg, false);
				targetKeypoints = engine.findFeatures(targetImg.flatten());	
				in.close();
			} catch (IOException e) {
				WARN.warn(Thread.currentThread().getName()+"\t"+e.getMessage());
				continue;
			}

			AffineTransformModel fittingModel = new AffineTransformModel(5);
			RANSAC<Point2d, Point2d> ransac = 
				new RANSAC<Point2d, Point2d>(fittingModel, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5), true);

			LocalFeatureMatcher<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
			  new FastBasicKeypointMatcher<Keypoint>(8), ransac);

			matcher.setModelFeatures(queryKeypoints);
			matcher.findMatches(targetKeypoints);
			
			int numMatches = matcher.getMatches().size();	
//			LOG.info(Thread.currentThread().getName()+"\tNumber of matches for image " + targetImageObj.url + " is " + numMatches);
			map.put(targetImageObj, numMatches);
		}
		
		mapSorted = sortByValues(map);
		Iterator<Map.Entry<WebImageObject, Integer>> it = mapSorted.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<WebImageObject, Integer> entry = it.next();
//			LOG.info(entry.getKey().url+"\t"+entry.getValue());
		}
		if(mapSorted.size()>0){
			WebImageObject returnObj = mapSorted.entrySet().iterator().next().getKey();
			LOG.info("Found image:"+returnObj.url);
			return returnObj;
		}
		else{
			return null;
		}
	
	}
	
	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
		Comparator<K> valueComparator =  new Comparator<K>() {
		    public int compare(K k1, K k2) {
		        int compare = map.get(k2).compareTo(map.get(k1));
		        if (compare == 0) return 1;
		        else return compare;
		    }
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}
	
	public static BufferedImage resizeImage(BufferedImage image, int destWidth) {
		
		ResampleOp resampleOp = new ResampleOp(destWidth,(destWidth * image.getHeight()) / image.getWidth() );
		resampleOp.setFilter(ResampleFilters.getLanczos3Filter()); 
		image = resampleOp.filter(image, null);
		
		return image;
		
	}
	
	//Judge if the google item is valid for crawl
	public static boolean  isValidItem(MovieItem movieItem,String title, String summary){
		boolean isValid = false;
		String movieName = movieItem.get_movie_name();
		String director = movieItem.get_director();
		String[] actorList = movieItem.get_actor_list();
		
		if(movieName.equals("") || director==null || actorList==null ){
			LOG.info("Imcomplete MovieItem...");
			return false;
		}
		if(title==null){
			LOG.info("Title from source webpage is empty");
			return false;
		}
		if(summary==null){
			LOG.info("Summary from google image is empty");
			return false;
		}
		StringBuffer movieInfo = new StringBuffer(movieName);
		//Title contains movie name
		if(title.contains(movieName)){
			LOG.info("Title contains movie name...");
			isValid=true;
			return isValid;
		}
		
		
		//Title contains director name or cast name
		ArrayList<String> casts = new ArrayList<>();
		if(!director.equals("") && director!=null){
			casts.add(director);
		}
		
		for(String cast:actorList){
			casts.add(cast);
			movieInfo.append(cast);
		}
		LOG.info("movieName:"+movieName+"\tcasts:"+Arrays.toString(casts.toArray(new String[casts.size()])));
		for(String cast:casts){
			if(title.contains(cast)){
				LOG.info("Title contains cast name...:"+cast);
				isValid = true;
				return isValid;
			}
		}
		
		//otherwise, look at the google summary
		int matchCount = countMatch(summary, movieInfo.toString());
		if(matchCount > 0){
			LOG.info("summary contains movie info...count:"+matchCount);
			return true;
		}
		else return false;
		
	}
	
	public static int countMatch(String summary, String title){
		if(summary==null || title==null)return 0;
		if(summary.equals("")||title.equals(""))return 0;
		
		int count = 0;
		HashSet<Character> summarySet = new HashSet<>();
		char[] titleArr = title.toCharArray();
		char[] summaryArr = summary.toCharArray();
		
		for(char c:summaryArr){
			if(!ImageCollectorUtils.isHanZi(c))continue;
			summarySet.add(c);
		}
		
		for(char c:titleArr){
			if(!ImageCollectorUtils.isHanZi(c))continue;
			if(summarySet.contains(c))count++;
		}
		return count;
	}
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		
//		MovieItem movieItem = new MovieItem();
//		movieItem.set_movie_name("来自星星的你");
//		movieItem.set_movie_id("xxxxxxxx");
//		movieItem.set_director("");
//		movieItem.set_actor_list(new String[]{"全智贤","金秀贤"});
		
		
		MovieItem movieItem = new MovieItem();
		movieItem.set_movie_name("辣妈正传");
		movieItem.set_movie_id("xxxxxxxx");
		movieItem.set_director("");
		movieItem.set_actor_list(new String[]{"孙俪"});
		
		String summary = "44372_804249_672559.jpg";
		String title = "明星产后瘦身Style 领衔变辣妈";
//		System.out.println(TargetImageSelector.countMatch(summary, title));
		System.out.println(TargetImageSelector.isValidItem(movieItem, title, summary));
	}
	
	
}

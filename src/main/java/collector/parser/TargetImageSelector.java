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
import util.Stopwords;

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
		
		if(imgObjs==null || imgObjs.size()==0){
			LOG.warn(Thread.currentThread().getName()+"\tNo image crawled from the source page..."+gobj.webUrl);
			return;
		}
		LOG.warn(Thread.currentThread().getName()+"\tStart to find target image..."+imgObjs.size());
		// validation
		String summary = gobj.summary;
		String title = imgObjs.get(0).title;
		if (!isRelevantItem(movieItem, title, summary))
			return;
		// if valid, continue to build targetObject
		String queryUrlStr = gobj.smallImageUrl;
		WebImageObject targeObj = getTargetImage(queryUrlStr, imgObjs);
		
		if (targeObj == null) {
			LOG.warn(Thread.currentThread().getName()+"\tNo image found by image matching program...Maybe caused by small image size, or ioexception");
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
			LOG.info(Thread.currentThread().getName()+"\t"+this.webImageObject.addr);
		}
		
	}
	
	/**
	 * Rank the images
	 * @param queryurlstr
	 * @param targetImageObjs
	 * @return
	 */
	private static WebImageObject getTargetImage(String queryurlstr, ArrayList<WebImageObject> targetImageObjs){
		LOG.info(Thread.currentThread().getName()+"\tStart to calculate the image similarity..."+targetImageObjs.size());
		
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
				long startTIme = System.currentTimeMillis();
				InputStream in = ImageCollectorUtils.getInputStreamFromURL(targetImageObj.url);
				if(in==null){
					LOG.info(Thread.currentThread().getName()+"\tEmpty inputstream, maybe read time out....");
					continue;
				}
				BufferedImage bImg = ImageIO.read(in);
				if(bImg==null)continue;
				int width = bImg.getWidth();
				int height = bImg.getHeight();
				if(width<Parameters.WIDTH || height<Parameters.HEIGHT){
					LOG.info(Thread.currentThread().getName()+"\tImage size too small...width="+width+" <"+Parameters.WIDTH +" or height="+height+" < "+Parameters.HEIGHT);
					continue;
				}
				targetImageObj.width = width;
				targetImageObj.height = height;
				
				if (bImg.getWidth() > IMG_WIDTH)
					bImg = resizeImage(bImg, IMG_WIDTH);
				
				targetImg = ImageUtilities.createMBFImage(bImg, false);
				targetKeypoints = engine.findFeatures(targetImg.flatten());	
				in.close();
				long endTIme = System.currentTimeMillis();
				LOG.info(Thread.currentThread().getName()+"\tRead image time:"+(endTIme-startTIme)/1000+" secs\t"+targetImageObj.url);
				
				
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
			LOG.info(Thread.currentThread().getName()+"\tFound image:"+returnObj.url);
			return returnObj;
		}
		else{
			LOG.info(Thread.currentThread().getName()+"\tNo image found, maybe caused by image size too small...");
			return null;
		}
	
	}
	
	private static ArrayList<String> String2Bigrams(String str){
		if(str ==null)return null;
		ArrayList<String> result = new ArrayList<>();
		if(str.length() <3){
			result.add(str);
			return result;
		}
		else{
			for(int i = 0,j=2;j<str.length()+1;i++,j++){
					String bi = str.substring(i, j);
					result.add(bi);
//					System.out.println(str+"\t"+bi);
			}
			return result;
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
	
	/**
	 * Judge if the google item is valid for crawl
	 * If the webpage title don't contains shows' name, cast name or director name && 
	 * google snapple do not similar to the movie info
	 * Then, the google item is irrelevant
	 * @param movieItem
	 * @param title
	 * @param summary
	 * @return
	 */
	public static boolean  isRelevantItem(MovieItem movieItem,String title, String summary){
		String movieName = movieItem.get_movie_name();
		String director = movieItem.get_director();
		String[] actorList = movieItem.get_actor_list();
		
		
		if(movieName==null || movieName.equals("") || director==null || actorList==null ){
			LOG.info(Thread.currentThread().getName()+"\tImcomplete MovieItem...");
			return false;
		}
		if(title==null){
			LOG.info(Thread.currentThread().getName()+"\tTitle from source webpage is empty");
			title="";
		}
		if(summary==null){
			LOG.info(Thread.currentThread().getName()+"\tSummary from google image is empty");
			summary ="";
		}
		
		ArrayList<String> movieInfo = new ArrayList<>();
		movieInfo.add(movieName);
		if(!director.equals(""))movieInfo.add(director);
		for(String actor:actorList)if(!actor.equals(""))movieInfo.add(actor);
		
		ArrayList<String> bigrams = new ArrayList<>();
		for(String minfo:movieInfo){
			if(title.contains(minfo)){
				LOG.info(Thread.currentThread().getName()+"\tTitle:"+title+"\tcontains:"+minfo);
				return true;
			}
			ArrayList<String> thisbigram = String2Bigrams(minfo);
			bigrams.addAll(thisbigram);
		}
		
		for(String bigram:bigrams){
			if(summary.contains(bigram)){
				LOG.info(Thread.currentThread().getName()+"\tSummary:"+summary+"\tcontains:"+bigram);
				return true;
			}
		}
		return false;
	}
	
	public static int countMatch(String summary, String title){
		if(summary==null || title==null)return 0;
		if(summary.equals("")||title.equals(""))return 0;
		
		int count = 0;
		HashSet<Character> stoplist = Stopwords.getStoplist();
		HashSet<Character> summarySet = new HashSet<>();
		char[] titleArr = title.toCharArray();
		char[] summaryArr = summary.toCharArray();
		
		for(char c:summaryArr){
			if(!ImageCollectorUtils.isHanZi(c))continue;
			if(stoplist.contains(c))continue;
			summarySet.add(c);
		}
		
		for(char c:titleArr){
			if(!ImageCollectorUtils.isHanZi(c))continue;
			if(summarySet.contains(c)){
				System.out.println(c);
				count++;
			}
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
		movieItem.set_movie_name("最美的时光");
		movieItem.set_movie_id("xxxxxxxx");
		movieItem.set_director("");
		movieItem.set_actor_list(new String[]{"曾丽珍","钟汉良","张钧甯",""});
		
		String summary = "星野Hotel Bleston Court度假村";
		String title = "海外婚礼蜜月首选：星野梦缘 |婚礼|教堂|度假村_新浪时尚_新浪网";
//		System.out.println(TargetImageSelector.countMatch(summary, title));
		System.out.println(TargetImageSelector.isRelevantItem(movieItem, title, summary));
	}
	
	
}

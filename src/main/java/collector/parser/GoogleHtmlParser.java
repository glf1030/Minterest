package collector.parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import query.BlockingSiteFactory;
import entity.GoogleHtmlObject;
import util.ImageCollectorUtils;
import util.Parameters;

/**
 * Get GoogleHtmlObject from input google image html file
 * @author ys439
 *
 */
public class GoogleHtmlParser {
	
	static final Logger LOG = Logger.getLogger(GoogleHtmlParser.class.getName());
	static final Logger WARN = Logger.getLogger(GoogleHtmlParser.class.getName());
	
	public static ArrayList<GoogleHtmlObject> getGoogleHtmlItems(String googleHtmlFile){
		return parse(googleHtmlFile);
	}
	
	
	/**
	 * 
	 * @param googleHtmlFile .htm file of Google Image result
	 * @return url for each google result
	 * @throws IOException occurs when error happens to use jsoup parse the google htm file.
	 */
	private static ArrayList<GoogleHtmlObject> parse(String googleHtmlFile)
//			throws IOException
			{
		LOG.info(Thread.currentThread().getName()+" Start parsing ..."+googleHtmlFile);
		ArrayList<GoogleHtmlObject> result = new ArrayList<GoogleHtmlObject>();
		String rootFolderPath = ImageCollectorUtils.buildImageRootDir(googleHtmlFile);
		
		
		Document googleImageDoc = null;

		File googleFile = new File(googleHtmlFile);
		try {
			googleImageDoc = Jsoup.parse(googleFile,"UTF-8","");
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			
		}
		
		Element body = googleImageDoc.body();
		Element images = body.getElementsByClass("images_table").get(0);
		Elements rows = images.getElementsByTag("tr");
		
		for (Element row : rows) {
			Elements items = row.getElementsByTag("td");
			for (Element item : items) {// each image in google

				String link;
				String imgsrc;
				String cite;
				String summary;
				String height;
				String width;

				if(item.select("a")==null || !item.select("a").first().hasAttr("href")){
					WARN.warn(Thread.currentThread().getName()+"["+item.toString().subSequence(0, 100)+"...]Do not contains <a href> attr...");
					continue;
				}
				else{
					link = item.select("a").first().attr("href");
					if(!link.contains("&") || !link.contains("http")){
						WARN.warn(Thread.currentThread().getName()+"Invalid url:"+link);
						continue;
					}
					else{
						link = link.substring(link.indexOf("http"));
						link = link.substring(0, link.indexOf("&"));
					}
				}

				imgsrc = item.select("img").first().attr("src");

				cite = item.select("cite").first().attr("title");
				
				height = item.select("img").first().attr("height");
				width = item.select("img").first().attr("width");
				
				
				summary = item.toString();
				summary = ImageCollectorUtils.fixGoogleSummary(summary);
				
//				if(Integer.parseInt(width) < Parameters.WIDTH || Integer.parseInt(height) < Parameters.HEIGHT){
//					LOG.info(link+"("+summary+") is throw away, size unvalid:w="+width+"\th="+height);
//					continue;
//				}

				
				GoogleHtmlObject gItem = new GoogleHtmlObject(imgsrc, link, summary, cite,height,width);
				gItem.saveRootDir = rootFolderPath;
				result.add(gItem);
			}
		}
		
		result = removeFromBlockingSite(result);
		LOG.info(Thread.currentThread().getName()+" Finish parsing ..."+googleHtmlFile+"..\tReturn "+result.size()+" GoogleHtmlObject..");
		return result;
	}
	/**
	 * unfinished!
	 * @param googleObjs
	 * @return
	 */
	private static ArrayList<GoogleHtmlObject> removeFromBlockingSite(ArrayList<GoogleHtmlObject> googleObjs){
		ArrayList<GoogleHtmlObject> result = new ArrayList<GoogleHtmlObject>();
		BlockingSiteFactory bsf = new BlockingSiteFactory();
		HashSet<String> b_links = bsf.getSites();
		for(GoogleHtmlObject gobj:googleObjs){
			String weburlstr = gobj.webUrl;
			URL url = null;
			try {
				url = new URL(weburlstr);
			} catch (MalformedURLException e) {
				continue;
			}
			if(url==null){
				continue;
			}
			else {
				String hostname = url.getHost();
				if(b_links.contains(hostname)){
					LOG.info(Thread.currentThread().getName()+"Site is on the blocking list:"+hostname);
					continue;
				}
				else{
					result.add(gobj);
				}
			}
			
		}
		return result;
	}
	
}

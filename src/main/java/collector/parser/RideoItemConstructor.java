package collector.parser;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import entity.GoogleHtmlObject;
import entity.RideoItem;
import entity.WebImageObject;
import util.MD5Generator;

/**
 * Assemble the RideoItem for restore
 * @author ys439
 *
 */
public class RideoItemConstructor {
	
	static final Logger LOG = Logger.getLogger(RideoItemConstructor.class.getName());
	static final Logger WARN = Logger.getLogger(RideoItemConstructor.class.getName());
	
	RideoItem dbObject = null;
	
	public RideoItem getRideoItem(){
		return this.dbObject;
	}
	
	public RideoItemConstructor(TargetImageSelector targetImage,String movie_id,String keyword) {
		LOG.info(Thread.currentThread().getName()+"\tStart to build RideoObject...");
		if(targetImage==null||targetImage.googleObject==null||targetImage.webImageObject==null){
			LOG.info("TargetImageObject is null!");
			return;
		}
		
		GoogleHtmlObject googleObj = targetImage.googleObject;
		WebImageObject imageObject = targetImage.webImageObject;
		
		String description;
		if(imageObject.alt!=null && !imageObject.alt.equals(""))description = imageObject.alt;
		else{
			description = googleObj.summary;
//			description = "";
		}
		
		dbObject = new RideoItem();
		
		dbObject.setMId(movie_id);
		dbObject.setPId(MD5Generator.execute(imageObject.url));
		dbObject.setPUrl(imageObject.url);
		dbObject.setDes(description);
		dbObject.setSourceLink(googleObj.webUrl);
		dbObject.setLocalAdd(imageObject.addr);
		dbObject.setSourceType("Google");
		dbObject.setIsExternal("0");
		dbObject.setIsInteresting(null);
		dbObject.setGroupNum(1);
		dbObject.setTitle(imageObject.title);
		dbObject.setTags(null);
		dbObject.setWidths(imageObject.width);
		dbObject.setHeights(imageObject.height);
		
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		dbObject.setDate(sf.format(date));
		dbObject.setKeyword(keyword);
		LOG.info(Thread.currentThread().getName()+"Finish to build RideoObject...");
	}

}

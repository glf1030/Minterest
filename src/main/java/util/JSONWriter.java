//package util;
//
//import java.util.ArrayList;
//
//import crawl.ImageCollector.ExternalImage;
//
//
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;
//
///**
// * Handler to write json files
// * @author ys439
// *
// */
//public class JSONWriter {
//	public static JSONObject writeImage(int eid,String link,String imgsrc,
//			String height, String width,
//			String title,
//			String cite,String content){
//
//		JSONObject imageObj = new JSONObject();
//		imageObj.put("id", eid);
//		imageObj.put("source", link);
//		imageObj.put("pic_url", imgsrc);
//		imageObj.put("height", height);
//		imageObj.put("width", width);
//		imageObj.put("title", title);
//		imageObj.put("cite", cite);
//		imageObj.put("content", content);
//		
//		return imageObj;
//	}
//	
//	public static JSONObject writeExternalImages(JSONObject inputimageObj, ArrayList<ExternalImage> externalImages){
//		JSONObject imageObj = inputimageObj;
//		
//		JSONArray extImages = new JSONArray();
//		for(ExternalImage image:externalImages){
//			String iurl = image.url;
//			String ialt = image.alt;
//			String addr = image.localAddr;
//			String context = image.context;
//			
//			JSONObject imgobj = new JSONObject();
//			imgobj.put("url", iurl);
//			imgobj.put("alt", ialt);
//			imgobj.put("addr", addr);
//			imgobj.put("context", context);
//			extImages.add(imgobj);
//		}
//		imageObj.put("external_images", extImages);
//		
//		return imageObj;
//	}
//	
//	public static JSONObject writeAll(
//			JSONObject inobj, String alt,String orig_pic_url,String imageLocalAddr,boolean keyframe,
//			ArrayList<ExternalImage> externalImages)
//	{
//		JSONObject imageObj = inobj;
//		imageObj.put("orig_pic_url", orig_pic_url);
//		imageObj.put("pic_addr", imageLocalAddr);
//		imageObj.put("alt", alt);
//		imageObj.put("keyframe_success", keyframe);
//		JSONArray extImages = new JSONArray();
//		for(ExternalImage image:externalImages){
//			String iurl = image.url;
//			String ialt = image.alt;
//			String addr = image.localAddr;
//			String context = image.context;
//			
//			JSONObject imgobj = new JSONObject();
//			imgobj.put("url", iurl);
//			imgobj.put("alt", ialt);
//			imgobj.put("addr", addr);
//			imgobj.put("context", context);
//			extImages.add(imgobj);
//		}
//		imageObj.put("external_images", extImages);
//		
//		return imageObj;
//	}
//
//}

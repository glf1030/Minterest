package util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class ImageCollectorUtils {
	
	static final Logger LOG = Logger.getLogger(ImageCollectorUtils.class.getName());
	static final Logger WARN = Logger.getLogger(ImageCollectorUtils.class.getName());
	
	/**
	 * 
	 * @param urlStr
	 * @return 
	 * @throws IOException
	 */
	public static InputStream getInputStreamFromURL(String urlStr){
		try{
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(Parameters.URL_CONNECT_TIMEOUT));  
			System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(Parameters.URL_READ_TIMEOUT));  
			InputStream in = conn.getInputStream();
			return in;
		}catch(MalformedURLException e){
			WARN.warn(Thread.currentThread().getName()+e.getMessage()+"\tif no URL can be created from the provided string URL="+urlStr);
			return null;
		}catch(SocketTimeoutException e){
			WARN.warn(Thread.currentThread().getName()+"\tRead source timeout:"+urlStr+"\t"+e.getMessage());
			return null;
		}catch(IOException e){
			WARN.warn(Thread.currentThread().getName()+"\tIOException:url="+urlStr+"\t"+e.getMessage());
			return null;
		}
		
	}
	
	public static String buildImageRootDir(String googleImageFile){
		File html = new File(googleImageFile);
		String name = html.getName();
		name = name.substring(0, name.indexOf("."));
		try{
			name = FileName2Pinyin.convertHanzi2PinyinStr(name);
		}catch(Exception e){
			
		}
		File imgDIR = new File(html.getParentFile()+"/"+name);
		if(!imgDIR.exists()){
			imgDIR.mkdir();
		}
		return imgDIR.getAbsolutePath();
	}
	/**
	 * Sometimes, src of img is looks like:
	 * src="/uploads/thumb/54/e8/5553.jpg"
	 * or 
	 * src="/../../uploads/thumb/54/e8/5553.jpg"
	 * @param url
	 * @param link
	 * @return
	 */
	public static String fixURL(String url, String link){
		if(link.contains("http://"))link = link.substring("http://".length());
		String[] parts = link.split("/");
		String result = "";
		if(!url.contains("../")){
			if(!url.contains("/")){
				for(int i = 0;i<parts.length-1;i++){
					result+=parts[i]+"/";
				}
				result += url;
			}else{
				if(url.startsWith("/"))url = url.substring(1);
				result = parts[0]+"/"+url;
			}
			
		}
		else{
			int count = 1;
			while(url.contains("../")){
				count++;
				url = url.substring(url.indexOf("../")+3);
			}
			for(int i = 0;i<parts.length-count;i++){
				result+=parts[i]+"/";
			}
			result+=url;
		}
		result = "http://"+result;//***Attention: Must add Http://, otherwise, it will throws Mal... Exception
		return result;
	}
	/**
	 * 
	 * @param imgUrl image URL
	 * @return  foo.txt      --> "txt"
 				a/b/c.jpg    --> "jpg"
 				a/b.txt/c    --> ""
 				a/b/c        --> ""
	 */
	public static String getURLExtension(String imgUrl){
		String ext = null;
		ext = FilenameUtils.getExtension(imgUrl);
		return ext;
	}
	
//	public static String refineGoogleImageTitle(String origTitle){
//		String title = origTitle;
//		try{
//			title = title.substring(title.indexOf("</cite><br />")+"</cite><br />".length());
//			title = title.substring(0, title.indexOf("<br />"));
//			title = title.replaceAll("<b>|</b>", " ");
//		}catch(IndexOutOfBoundsException e){
//			title = origTitle;
//		}
//		return title;
//	}
	
	/**
	 * Decode the url
	 * 
	 * @param surl
	 * @return
	 */
	public static String urlDecode(String surl){
		String before = "";
		String after = surl;
		while(!before.equals(after)){
			try{
				before = after;
				after = URLDecoder.decode(before, "UTF-8");
			}catch(Exception e){
				WARN.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
				return null;
			}
		}
		
		if(!FileName2Pinyin.isMessyCode(after)){
			return after;
		}
		else{
			before = "";
	    	after = surl;
	    	while(!before.equals(after)){
				try{
					before = after;
					after = URLDecoder.decode(before, "GBK");
				}catch(Exception e){
					WARN.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
					return null;
				}
			}
	    	
	    	if(!FileName2Pinyin.isMessyCode(after))return after;
	    	else{
	    		try{
					after = URLDecoder.decode(surl, "UTF-8");
					return after;
				}catch(Exception e){
					WARN.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
					return null;
				}
	    	}
		}
	}
	
	/**
	 * Save image to local Address
	 * @param url
	 * @param localAddr
	 * @return
	 */
	public static boolean saveImage(String url, String localAddr){
		if(url==null || url.equals("")){
			WARN.warn(Thread.currentThread().getName()+"URL is null or empty...");
			return false;
		}
		
		try {
			InputStream in = ImageCollectorUtils.getInputStreamFromURL(url);
			Image img = ImageIO.read(in);
			if (img == null) {
				return false;
			} 
			else {
				ImageIO.write((BufferedImage) img, 
						ImageCollectorUtils.getURLExtension(url).toUpperCase(), 
						new FileOutputStream(new File(localAddr)));
				LOG.info(Thread.currentThread().getName()+"\tSave the image from url:"+url+" to addr:"+localAddr);
				return true;
			
				
			}
		}catch(SocketTimeoutException e){
			WARN.warn(Thread.currentThread().getName()+"\tRead source timeout:"+url+"\t"+e.getMessage());
			return false;
		} 
		catch (IOException e) {
			WARN.warn(Thread.currentThread().getName() + "\t" + e.getMessage()
					+ "IOException occurs when save image from url:" + url
					+ " to addr:" + localAddr);
			return false;
		}
	}
	
	public static String fixGoogleSummary(String orig){
		String title = orig;
		title = title.substring(title.indexOf("</cite><br />")+"</cite><br />".length());
		title = title.substring(0, title.indexOf("<br />"));
		title = title.replaceAll("<b>|</b>", " ");
		return title;
	}
	
	/**
	 * verify if a ch is a Chinese char
	 * @param ch
	 * @return
	 */
	public static boolean isHanZi(char ch) {
		return (ch >= 0x4E00 && ch <= 0x9FA5);

	}

}

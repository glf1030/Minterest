//package crawl;
//
//
//
//import image.RankImage;
//
//import java.awt.image.BufferedImage;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.SocketTimeoutException;
//import java.net.URL;
//import java.net.URLDecoder;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Random;
//import java.util.TreeMap;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import javax.imageio.ImageIO;
//import net.sf.json.JSONObject;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import util.FileName2Pinyin;
//
//import cern.colt.Arrays;
//
//import com.gargoylesoftware.htmlunit.AjaxController;
//import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
//import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//
//import entity.WebContent;
//
///**
// * Main class for the image collector
// * 
// * @author ys439
// *
// */
//public class ImageCollectorOld {
//	//minimum length of image collect from web
//		int IMAGE_LENGTH_MIN = 60000;
//		int IMAGE_WIDTH_MIN = 300;
//		int IMAGE_HEIGHT_MIN = 150;
//		
//		//# of threads used
//		int THREAD_COUNT = 2;
//		int wait_thread_count = 0;
//		BlockingQueue<File> HTML_QUEUE;
//		public volatile Object signal = new Object();
//		
//		//timeout range for image crawling
//		int RANDOMTIMERANGE = 5;//wait for (0,1)*60 seconds
//		
//		//timeout time
//		int URL_CONNECT_TIMEOUT = 1000*30;
//		int URL_READ_TIMEOUT = 1000*60;
//		int WEB_CLIENT_TIMEOUT = 1000*30;
//		
////		static Logger log = Logger.getLogger(ImageCollector.class);
//		static Logger log = Logger.getLogger("logging");
//		static Logger warn = Logger.getLogger("logwarn");
//	
//	
//	
//	/**
//	 * Changes: 
//	 * @param dir: Dir for a movie item, contains html files for crawling
//	 */
//	public ImageCollectorOld(String movie){
//		
//		PropertyConfigurator.configure("log4j.properties");
//		this.HTML_QUEUE = new LinkedBlockingQueue<File>();
//		
//		File moviedir = new File(movie);
//		File[] items = moviedir.listFiles();
//		if(items == null || items.length == 0)return;
//		for(File itemfolder:items){
//			if(!itemfolder.isDirectory())continue;
//			else{
//				File[] htmls = itemfolder.listFiles();
//				for(File html:htmls){
//					log.info(Thread.currentThread().getName()+html.getAbsolutePath());
//					if(html.isDirectory())continue;
//					else if(!html.getName().endsWith("htm")&&(!html.getName().endsWith("html")))continue;
//					else {
//						this.HTML_QUEUE.add(html);
//					}
//				}
//			}
//		}
//		
//		log.info(Thread.currentThread().getName()+"\t"+HTML_QUEUE.size()+" files to be crawled...");
//	}
//	
//	public ImageCollectorOld(){
//		PropertyConfigurator.configure("log4j.properties");
//	}
//	
//	
//	public void setThreadNum(int count){
//		this.THREAD_COUNT = count;
//	}
//	
//	private String refineImageTitle(String orig){
//		String title = orig;
//		title = title.substring(title.indexOf("</cite><br />")+"</cite><br />".length());
//		title = title.substring(0, title.indexOf("<br />"));
//		title = title.replaceAll("<b>|</b>", " ");
//		return title;
//	}
//	
//	private String getImageDir(String googleImageFile){
//		File html = new File(googleImageFile);
//		String name = html.getName();
//		name = name.substring(0, name.indexOf("."));
//		try{
//			name = util.FileName2Pinyin.convertHanzi2PinyinStr(name);
//		}catch(Exception e){
//			
//		}
//		File imgDIR = new File(html.getParentFile()+"/"+name);
//		if(!imgDIR.exists()){
//			imgDIR.mkdir();
//		}
//		return imgDIR.getAbsolutePath();
//	}
//	
//	
//	/**
//	 * used to save google compressed image, deprecated in the future
//	 * @param imgLink
//	 */
//	private String saveImage(String imgLink,String destPath,String savename){
//		File imgDIR = new File(destPath);
//		if(!imgDIR.exists()){
//			imgDIR.mkdir();
//		}
//		
//		String imageName = savename;
//
//		File imgFile = new File(imgDIR+"/"+imageName+".jpg");
//		try{
//			URL imageURL = new URL(imgLink);
//			HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
//			DataInputStream in = new DataInputStream(conn.getInputStream());
//			DataOutputStream out = new DataOutputStream(new FileOutputStream(imgFile.getAbsoluteFile()));
//			byte[] buffer = new byte[4096];
//            int count = 0;
//            while ((count = in.read(buffer)) > 0)/*将输入流以字节的形式读取并写入buffer中*/
//            {
//                out.write(buffer, 0, count);
//            }
//            out.close();/*后面三行为关闭输入输出流以及网络资源的固定格式*/
//            in.close();
//            conn.disconnect();
//		}catch(MalformedURLException e){
//			warn.warn(Thread.currentThread().getName()+e.getMessage()+"\t"+imgLink+" is not valid url"+"\t"+Arrays.toString(e.getStackTrace()));
////			e.printStackTrace();
//			return null;
//		}catch(IOException e2){
//			warn.warn(Thread.currentThread().getName()+"\t"+imgLink+"\t"+e2.toString());
////			e2.printStackTrace();
//			return null;
//		}
//		return imgFile.getAbsolutePath();
//	}
//	
//	/**
//	 * Sometimes, src of img is looks like:
//	 * src="/uploads/thumb/54/e8/5553.jpg"
//	 * or 
//	 * src="/../../uploads/thumb/54/e8/5553.jpg"
//	 * @param url
//	 * @param link
//	 * @return
//	 */
//	private String fixURL(String url, String link){
//		if(link.contains("http://"))link = link.substring("http://".length());
//		String[] parts = link.split("/");
//		String result = "";
//		if(!url.contains("../")){
//			if(!url.contains("/")){
//				for(int i = 0;i<parts.length-1;i++){
//					result+=parts[i]+"/";
//				}
//				result += url;
//			}else{
//				if(url.startsWith("/"))url = url.substring(1);
//				result = parts[0]+"/"+url;
//			}
//			
//		}
//		else{
//			int count = 1;
//			while(url.contains("../")){
//				count++;
//				url = url.substring(url.indexOf("../")+3);
//			}
//			for(int i = 0;i<parts.length-count;i++){
//				result+=parts[i]+"/";
//			}
//			result+=url;
//		}
//		result = "http://"+result;//***Attention: Must add Http://, otherwise, it will throws Mal... Exception
//		log.info(Thread.currentThread().getName()+"\tURL fixed from +("+url+") to ==>"+result);
//		return result;
//	}
//	
//	
//	class Link{
//		String url;
//		String alt;
//		String text;
//		public Link(String url,String alt,String text){
//			this.alt = alt;
//			this.url = url;
//			this.text = text;
//		}
//	}
//	public ArrayList<ExternalImage> saveExternalImage(String surl, String outpath, WebContent content)
//	{
//		log.info(Thread.currentThread().getName()+"\tis parsing "+ surl);
//		ArrayList<ExternalImage> results = new ArrayList<ExternalImage>();
//		
//		WebClient webClient = new WebClient();
//		webClient.setRefreshHandler(new ThreadedRefreshHandler());
//		webClient.getOptions().setJavaScriptEnabled(false);
//    	webClient.getOptions().setCssEnabled(false);
//    	webClient.setAjaxController(new AjaxController());
//    	webClient.getOptions().setTimeout(WEB_CLIENT_TIMEOUT);
//    	
//    	String fixedURL = urlDecode(surl);
//    	if(!(fixedURL==null || fixedURL.equals(""))){
//    		surl = fixedURL;
//    		log.info(Thread.currentThread().getName()+" is parsing "+ surl+"[after decode]");
//    	}
//    	else{
//    		warn.warn(Thread.currentThread().getName()+"\t Can not decode URL:"+surl);
//    	}
//		HtmlPage htmlPage = null;
//		try{
//			htmlPage = webClient.getPage(surl);
//		}catch(MalformedURLException e){
//			//if no URL can be created from the provided string
//			warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\tNo URL can be created from the provided string:"+surl);
//			return results;
//		}catch(IOException e){
//			warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\tIOException when reading URL:"+surl);
//			return results;
//		}catch(FailingHttpStatusCodeException e){
//			warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\tFail to access the server.URL:"+surl);
//			return results;
//		}
//    	
//		Document doc = Jsoup.parse(htmlPage.asXml());
////		warn.warn(doc.toString());
//		
//		//get webcontent
//		String webcontent = doc.body().text();
//		content.setContent(webcontent);
//		
////		Elements imgs = doc.getElementsByTag("img,IMG,Img");
//		Elements imgs = doc.select("img,IMG,Img");
//		Elements hrefs = doc.select("a[href]");
//		Elements files = doc.select("img[file]"); 
//		
//		ArrayList<Link> links = new ArrayList<Link>();
//		
//		for(Element link:imgs){
//			String src = link.attr("src");
//			String alt = link.attr("alt");
//			String text = link.parent().text();
//			Link thislink = new Link(src, alt, text);
//			links.add(thislink);
//		}
//		
//		for(Element link:hrefs){
//			String src = link.attr("abs:href");
//			String alt = link.text().trim();
//			String text = link.parent().text().trim();
//			Link thislink = new Link(src, alt, text);
//			links.add(thislink);
//		}
//		for(Element link:files){
//			String src = link.attr("abs:file");
//			String alt = link.text().trim();
//			String text = link.parent().text().trim();
//			Link thislink = new Link(src, alt, text);
//			links.add(thislink);
//		}
//		
//		int imageNameId = 1;
//		
//		for(Link link:links){
//			String src = link.url;
//			String alt = link.alt;
//			String text = link.text;
//			
//			//if no src attr presented
//			if(src.equals("")){
//				continue;
//			}
//			if(!(src.endsWith(".jpeg")||src.endsWith(".jpg")|| src.endsWith(".png")|| src.endsWith(".gif"))){
////				log.info(src);
//				continue;
//			}
//			
//			//if the url is not valid
//			if(!src.startsWith("http://")){
//				src = fixURL(src, surl);
//			}
//			
//			//get the original extension from the url
//			String extension = src.substring(src.lastIndexOf('.'));
//			if(extension.length()<1 || extension.length()>5)extension=".jpg";
//			if((!extension.equals(".jpg"))&&(!extension.equals(".png"))&&(!extension.equals(".jpeg"))&&(!extension.equals(".gif")))
//			{
//				continue;
//			}
//			log.info(Thread.currentThread().getName()+"\t\tExternal image src="+src);
//			
//			URL img = null;
//			HttpURLConnection conn = null;
////			Image image = null;
//			BufferedImage image = null;
//			InputStream in = null;
//			try{
//				img = new URL(src);
//				conn = (HttpURLConnection)img.openConnection();
//				conn.setConnectTimeout(URL_CONNECT_TIMEOUT);
//				conn.setReadTimeout(URL_READ_TIMEOUT);
//				
//				//
//				in = conn.getInputStream();
//				image = ImageIO.read(in);
//			}catch(MalformedURLException e){
//				// Either no legal protocol could be found in a specification string or the string could not be parsed.
//				warn.warn(Thread.currentThread().getName()+"\t"+src+"\tIncorrect url:"+src);
//				continue;
//			}catch(SocketTimeoutException e){
//				warn.warn(Thread.currentThread().getName()+"\tRead source timeout:"+src+"\t"+e.getMessage());
//				continue;
//			}catch(IOException e){
//				warn.warn(Thread.currentThread().getName()+"\tIOException when reading URL:"+src);
//				continue;
//			}
//			
//			if(image == null)continue;
//			int width = image.getWidth(null);
//			int height = image.getHeight(null);
////			int length = conn.getContentLength();
////			if(length < IMAGE_LENGTH_MIN){
////				log.info(Thread.currentThread().getName()+"\t\tShort image==>"+src+", omit...");
////				continue;
////			}
//			
//			if(width < IMAGE_WIDTH_MIN || height < IMAGE_HEIGHT_MIN){
//				warn.info(Thread.currentThread().getName()+"\t\tSmall image:"+src+", omit...width="+width+"\theight="+height);
//				continue;
//			}
//			else{
//				DataInputStream dataInputStream = null;
//				DataOutputStream out = null;
//				String filename = String.valueOf(imageNameId++)+extension;
//				String savePath = outpath+"/"+filename;
//				
//				try{
//					img = new URL(src);
//					conn = (HttpURLConnection)img.openConnection();
//					conn.setConnectTimeout(URL_CONNECT_TIMEOUT);
//					conn.setReadTimeout(URL_READ_TIMEOUT);
//					dataInputStream = new DataInputStream(conn.getInputStream());
//					out = new DataOutputStream(new FileOutputStream(savePath));
//					byte[] buffer = new byte[4096];
//					int count = 0;
//					while ((count = dataInputStream.read(buffer)) > 0)/*将输入流以字节的形式读取并写入buffer中*/
//					{
//						out.write(buffer, 0, count);
//					}
//					out.close();/*后面三行为关闭输入输出流以及网络资源的固定格式*/
//					dataInputStream.close();
//				}catch(MalformedURLException e){
//					warn.warn(Thread.currentThread().getName()+e.getMessage()+"\tURL="+src);
//					continue;
//				}catch(FileNotFoundException e){
//					warn.warn(Thread.currentThread().getName()+e.getMessage()+"\tFile not found:"+savePath);
//					continue;
//				}catch(IOException e){
//					warn.warn(Thread.currentThread().getName()+e.getMessage()+"\tIOException url="+src);
//					continue;
//				}finally{
//					conn.disconnect();
//					
//				}
//	            log.info(Thread.currentThread().getName()+"\t\tExternal image save to addr="+savePath);
//	            ExternalImage externalImg = new ExternalImage(src, alt);
//				externalImg.setAddr(savePath);//changed: LIFAN asked for current path, rather than absoluted path
//				externalImg.setContext(text);
//				results.add(externalImg);
//			}
//			
//			long timeout = (long)(new Random().nextDouble()*1000*RANDOMTIMERANGE);
//			try{
//				Thread.sleep(timeout);
//			}catch(InterruptedException e){
//				warn.warn(Thread.currentThread().getName()+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
//				continue;
//			}
//			
//		}
//		
//		if(results.size()==0){
//			warn.warn(Thread.currentThread().getName()+"\tReturn 0 images ==> "+surl);
//		}
//
//		return results;
//	}
//	
//	/**
//	 * Decode the url
//	 * 
//	 * @param surl
//	 * @return
//	 */
//	public static String urlDecode(String surl){
//    	String before = "";
//    	String after = surl;
//    	while(!before.equals(after)){
//			try{
//				before = after;
//				after = URLDecoder.decode(before, "UTF-8");
//			}catch(Exception e){
//				warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
//				return null;
//			}
//		}
//    	
//    	if(!FileName2Pinyin.isMessyCode(after)){
//    		return after;
//    	}
//    	else{
//    		before = "";
//        	after = surl;
//        	while(!before.equals(after)){
//    			try{
//    				before = after;
//    				after = URLDecoder.decode(before, "GBK");
//    			}catch(Exception e){
//    				warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
//    				return null;
//    			}
//    		}
//        	
//        	if(!FileName2Pinyin.isMessyCode(after))return after;
//        	else{
//        		try{
//    				after = URLDecoder.decode(surl, "UTF-8");
//    				return after;
//    			}catch(Exception e){
//    				warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
//    				return null;
//    			}
//        	}
//    	}
//	}
//	
//	/**
//	 * 
//	 * @param GoogleImageFileURL Address of google image search htm file
//	 * @return
//	 */
//	public String getAllImages(String GoogleImageFileURL){
//		log.info(Thread.currentThread().getName()+"\t Start parsing..."+GoogleImageFileURL);
//		
//		String url = "";
//		long start= System.currentTimeMillis(); 
////		JSONObject jdoc = new JSONObject();
//		//JSONArray imageArr = new JSONArray();
//		
//		Document googleImageDoc = null;
//	
//		try{
//			googleImageDoc = Jsoup.parse(new File(GoogleImageFileURL),"UTF-8","");
//
//		} catch (IOException e) {
//			warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
//			return null;
//		}
//		Element body = googleImageDoc.body();
//		Element images = body.getElementsByClass("images_table").get(0);
//		Elements rows = images.getElementsByTag("tr");
//		String dir = getImageDir(GoogleImageFileURL);// \data\xiaobo\小时代 礼服 -Google Search\==>encode to
//		
//		FileWriter fw = null;
//		try {
//			fw = new FileWriter(new File(dir + "/context.json"));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			warn.warn(Thread.currentThread().getName()+"\t"+e1.getMessage()+"\t"+Arrays.toString(e1.getStackTrace()));
//		}
//		
//		int google_image_number = 0;
//		int eid = 0;
//		for (Element row : rows) {
//			Elements items = row.getElementsByTag("td");
//			for (Element item : items) {// each image in google
//				File dir4oneImage = new File(dir + "/"+ String.valueOf(google_image_number++));
//				if (!dir4oneImage.exists())dir4oneImage.mkdir();
//
//				String link;
//				String imgsrc;
//				String cite;
//				String height;
//				String width;
//				String title;
//				String content;
//
//				String imageLocalAddr = null;
//				String orig_pic_url = null;
//				String alt = null;
//				boolean keyframe = false;
//
//				String imageFolderAddr = dir4oneImage.getAbsolutePath() + "/";//
//				String targetImageAddr;
//				if(item.select("a")==null || !item.select("a").first().hasAttr("href")){
//					warn.warn(Thread.currentThread().getName()+"["+item.toString().subSequence(0, 100)+"...]Do not contains <a href> attr...");
//					continue;
//				}
//				else{
//					link = item.select("a").first().attr("href");
//					if(!link.contains("&") || !link.contains("http")){
//						warn.warn(Thread.currentThread().getName()+"Invalid url:"+link);
//						continue;
//					}
//					else{
//						link = link.substring(link.indexOf("http"));
//						link = link.substring(0, link.indexOf("&"));
//					}
//				}
//
//				imgsrc = item.select("img").first().attr("src");
//				height = item.select("img").first().attr("height");
//				width = item.select("img").first().attr("width");
//
//				cite = item.select("cite").first().attr("title");
//
//				String filepath = saveImage(imgsrc, imageFolderAddr, "0");
//				targetImageAddr = filepath;
//				if (filepath == null)
//					continue;
//
//				// get google image title
//				title = item.toString();
//				title = refineImageTitle(title);
//
//				// save external images, externalImages do not contains 0.jpg
//				WebContent webcontent = new WebContent();
//				ArrayList<ExternalImage> externalImages = saveExternalImage(
//						link, imageFolderAddr, webcontent);
//				content = webcontent.getContent();
//				eid++;
//				JSONObject imageObj = JSONWriter.writeImage(eid, link, imgsrc,
//						height, width, title, cite, content);
//
//				// not images crawled, maybe caused by web connection fail
//				if (externalImages.size() < 1) {
//					log.info(Thread.currentThread().getName()+ImageCollectorOld.class
//							+ "\t No images crawled, connection failed or low response:"+link);
//					try {
//						fw.write(imageObj.toString()+"\n");
//						fw.flush();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						warn.warn(Thread.currentThread().getName()+"\t"+e.getCause());
//					}
//				}
//				else{
//					orig_pic_url = "";
//					alt = "";
//					imageLocalAddr = "";
//				}
//
//				// create json
//				imageObj = JSONWriter.writeAll(imageObj, alt, orig_pic_url,
//						imageLocalAddr, keyframe, externalImages);
//				
//				try {
//					fw.write(imageObj.toString()+"\n");
//					fw.flush();
//					log.info(Thread.currentThread().getName()+"\t write ");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
//				}
////				imageArr.add(imageObj);
//				log.info(Thread.currentThread().getName()+"\tPage loaded==>" + link);
//			}
//		}
////		jdoc.put("image", imageArr);
//		try{
////			FileWriter fw = new FileWriter(new File(dir + "/context.json"));
////			fw.write(jdoc.toString());
//			fw.close();
//		}catch(Exception e){
//			warn.warn(Thread.currentThread().getName()+"\t"+e.getMessage()+"\t"+Arrays.toString(e.getStackTrace()));
//		}
//		
//		long end= System.currentTimeMillis(); 
//        System.out.println(GoogleImageFileURL+"\n"+Thread.currentThread().getName()+"总共耗时"+(end-start)/1000+"秒");  
//		return url;
//	}
//	
//	public class ExternalImage{
//		public String url;
//		public String alt;
//		public String context;
//		public String localAddr;
//		
//		public ExternalImage(String u, String al){
//			this.url = u;
//			this.alt = al;
//		}
//		public void setContext(String c){
//			this.context = c;
//		}
//		public void setAddr(String addr){
//			this.localAddr = addr;
//		}
//	}
//	
//	
//	public synchronized String getNextGoogleFile(){
//		log.info(Thread.currentThread().getName()+" The size of queue = "+HTML_QUEUE.size());
//		if(HTML_QUEUE.isEmpty()){
//			return null;
//		}
//		else{
//			String html = null;
//			log.info(Thread.currentThread().getName()+"\tRemain "+HTML_QUEUE.size()+" files haven't parsed, now ==>"+html);
////			html = HTML_QUEUE.get(HTML_QUEUE.size()-1).getAbsolutePath();
////			HTML_QUEUE.remove(HTML_QUEUE.size()-1);
//			try {
//				html = HTML_QUEUE.take().getAbsolutePath();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			return html;
//		}
//	}
//	
//	private CollectorThread[] begin(){
//		CollectorThread[] threads = new CollectorThread[THREAD_COUNT];
//		for(int i = 0;i<THREAD_COUNT;i++){
//			log.info("Starting the "+i+"th thtreads.");
//			threads[i] = new CollectorThread();
//			threads[i].start();
//		}
//		
//		return threads;
//	}
//	
//	
//	
//	class CollectorThread extends Thread{
//		volatile boolean _run = true;
//		
//		public void stopthread(){
//			this._run = false;
//			log.info(Thread.currentThread().getName()+"\t"+this._run);
//		}
//		
//		@Override
//		public void run(){
//			while(_run){
//				String fileurl = getNextGoogleFile();
//				if(fileurl!=null){
//					getAllImages(fileurl);
//				}
//				else{
//					try {
//						synchronized (HTML_QUEUE) {
//							
//							HTML_QUEUE.wait();
//						}
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						log.info(Thread.currentThread().getName()+"\t"+e.getCause());
//					}
//				}
//			}
//			log.info(Thread.currentThread().getName()+"\tstop..");
//		}
//	}
//	
//	
//	public void start(int threadnum) throws InterruptedException{
//		PropertyConfigurator.configure("log4j.properties");
//		
//		setThreadNum(threadnum);
//		long start= System.currentTimeMillis(); 
//		CollectorThread[] threads = begin();
//		
//		while(true){
//			synchronized (HTML_QUEUE) {
//				if(HTML_QUEUE.isEmpty()){
//					log.info("Queue is empty. Ready to stop");
//					for(CollectorThread thread:threads){
//						thread.stopthread();
//					}
//					HTML_QUEUE.notifyAll();
//					long end= System.currentTimeMillis(); 
//					log.info(Thread.currentThread().getName()+"\t总共耗时"+(end-start)/1000+"秒");
//					break;
//				}
//			}
//			Thread.sleep(2000);
//		}
//		
//		log.info(Thread.currentThread().getName()+"\tYeah, done.");
//	}
//	
//	
//	public synchronized void singleStart(String itemDir){
//		File item = new File(itemDir);
//		log.info("start"+item.getAbsolutePath());
//		for(File html:item.listFiles()){
//			if(html.isDirectory())continue;
//			else if(!html.getName().endsWith("htm")&&(!html.getName().endsWith("html")))continue;
//			else {
//				getAllImages(html.getAbsolutePath());
//			}
//		}
//	}
//	
//	public void testLink(String link)
//	{
//		saveExternalImage(link, "./test", new WebContent());
//		
//	}
//	
//	/**
//	 * 
//	 * @param args[0] path for htm files
//	 * @param args[1] number of threads in use
//	 * @throws Exception
//	 */
//	public static void main(String[] args)throws Exception{
//		
////		String path = args[0];
////		int number = Integer.parseInt(args[1]);
////		ImageCollector collector = new ImageCollector(path);
////		collector.start(number);
//		
////		String path = "beijingyushangxiyatu";
////		int number = 5;
////		ImageCollector collector = new ImageCollector(path);
////		collector.start(number);
//		
////		String link = "http://bbs.onlylady.com/thread-3388481-1-1.html";
////		ImageCollector collector = new ImageCollector("");
////		collector.testLink(link);
//		
//		String dir = "E:/crawlerdata/quanzhixian";
//		ImageCollectorOld collector = new ImageCollectorOld();
//		collector.singleStart(dir);
//		
//	}
//	
//	
//}

package crawl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import query.GoogleKeyWordQueryFactory;
import query.MovieQueryFactory;
import query.TaoBaoKeyWordQueryFacotry;


import util.FileCreator;
import util.FileName2Pinyin;
import util.FixedTaoBaoTitle;
import util.MD5Generator;
import util.URLEncoding;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import database.MysqlDatabase;

import entity.ImageItem;
import entity.RideoItem;
import entity.TaoBaoItem;

/*
 * Tao Bao Crawler used to crawl all objects from Taobao
 * Lifan Guo
 * 2014/2/17
 * 
 */
public class TaoBaoCrawler {
	static Logger log = Logger.getLogger("task");
	private static final int WEB_CLIENT_TIMEOUT = 1000*30;

	/**
	 * @param args
	 */
	
	TaoBaoKeyWordQueryFacotry kqf=new TaoBaoKeyWordQueryFacotry();
	//GoogleKeyWordQueryFactory mqf=new GoogleKeyWordQueryFactory("./movieList");
	MovieQueryFactory mqf=new MovieQueryFactory("Taobao");
	int number_pages=5;
	int number_items=40;
	String src="src";
	String data_src="data-src";
	MysqlDatabase msd=new MysqlDatabase();
	String root="/mnt/nfs/nas179/rideo/";
	String taobao="taobao";
	String winpath="/mnt/nfs/nas179/rideo/";
	//String winpath="D:/taobao/";
	String sourceType="Taobao";
	
	
	public TaoBaoCrawler()
	{
		MovieQueryFactory mqf=new MovieQueryFactory("Taobao");
	}
	public static void main(String[] args) 
	{
	
//		ArrayList<String> queryList=new TaoBaoCrawler().getQueryList();
//		for(String e:queryList)
//			System.out.println(e);
    	
new TaoBaoCrawler().crawl();
//		try {
//			String url=new TaoBaoCrawler().retrievalImageFromSingleLink("http://item.taobao.com/item.htm?id=37256304182");
//			System.out.println(url);
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ScriptException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}
	
	
	public void crawl()
	{
		PropertyConfigurator.configure("log4j.properties");
		
		String query_prefix="http://s.taobao.com/search?js=1&q=";
		String query_postfix="&s=";
		
		
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(); 
		log.info(sf.format(date));
		System.out.println(sf.format(date));  
		
		HashMap<String,String> movie_nameID=mqf.getTaobaoQuery();
		Iterator<String> iter=movie_nameID.keySet().iterator();
		ArrayList<String> movie_name_list=new ArrayList<String>();
		while(iter.hasNext())
			movie_name_list.add(iter.next().toString());
//		ArrayList<String> movie_name_list=mqf.getQuery();
		for(int i=0;i<movie_name_list.size();i++)
		{
			System.out.println(movie_name_list.get(i));
			log.info(movie_name_list.get(i));
		}
		for(String m:movie_name_list)
		{
			String movie_name=m;
			String movie_id=movie_nameID.get(movie_name);
			
			
			for(String q:kqf.getQuery())
			{
				
				for(int i=0;i<number_pages;i++)
				{
					String query=query_prefix+URLEncoding.encode(movie_name+" "+q)+query_postfix+i*number_items;
					log.info(query);
					System.out.println(query);
					ArrayList<RideoItem> tbiL=new ArrayList<RideoItem>();
					
				        try {
							tbiL=crawl(query,movie_name,movie_id,movie_name+q);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
                 
					String outpath=root+FileName2Pinyin.convertHanzi2PinyinStr(m)+"/"+taobao+"/"+FileName2Pinyin.convertHanzi2PinyinStr
							(q)+"/"+sf.format(date)+"/";
					
					String wpath=winpath+FileName2Pinyin.convertHanzi2PinyinStr(m)+"/"+taobao+"/"+FileName2Pinyin.convertHanzi2PinyinStr
							(q)+"/"+sf.format(date)+"/";
					if(new File(wpath).exists())
					{
						new File(wpath).delete();
					}
					FileCreator.createFile(wpath);
					imageLocalizer(tbiL,wpath,outpath,i);
					

					
					Boolean exist=false;
					try {
						 exist=msd.InsertLatestRecords(tbiL);
					} catch (SQLException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						exist=true;
					}
					if(exist)
					{
						log.info("exist! "+q);
						System.out.println("exist! ");
					break;
					}
					else
					{
						log.info("not exist! or does not have pages "+q);
						System.out.println("not exist! or does not have pages");
					continue;
					}
				}
			}
		}
		
		
		
		

	}
	
	public ArrayList<RideoItem> crawl(String query, String movie_Name, String movie_id,String keywords) throws MalformedURLException, IOException, InterruptedException
	{

				try {
					ArrayList<RideoItem> list=crawlWithQuery(query,movie_Name,movie_id,keywords);
					return list;
				} catch (FailingHttpStatusCodeException e) {
					Thread.sleep(1000*60*10);
					e.printStackTrace();
					return null;
				}
			
	}
	
	
	

	public ArrayList<RideoItem> crawlWithQuery(String query, String movie_name,String movie_id,String keywords) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException 
	{
		
		
		
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();                               
		System.out.println(sf.format(date)); 
		ArrayList<RideoItem> taoBaoList=new ArrayList<RideoItem>();
		WebClient webClient = new WebClient();
		webClient.setJavaScriptEnabled(false);
    	webClient.setCssEnabled(false);
    	webClient.setTimeout(60000);
    	webClient.setRefreshHandler(new ThreadedRefreshHandler());
    	webClient.setAjaxController(new AjaxController());
    	webClient.setTimeout(WEB_CLIENT_TIMEOUT);
    	HtmlPage htmlPage=null;
		htmlPage = webClient.getPage(query);
    	Document doc = Jsoup.parse(htmlPage.asXml());
    	//System.out.println(doc);
    	Elements e=doc.select("[class=item-box]");
    	for(Element ele:e)
    	{  
    		//System.out.println(ele.text());
    		Elements eles=ele.select("[trace=auction]");
    		if(eles.size()>0)
    		{
    	   // System.out.println(eles.get(0).attr("href"));
    	   String image_url = null;
    	 //  String des=null;
		try {
			image_url = retrievalImageFromSingleLink(eles.get(0).attr("href"));
		   // des=retrievalDesFromSingleLink(eles.get(0).attr("href"));
			//System.out.println(image_url);
		} catch (NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FailingHttpStatusCodeException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Thread.sleep(1000*60);
		} 
		    if(image_url!=null)
		    	
		    {
    	
		   RideoItem item= new RideoItem();
		   item.setPId(MD5Generator.execute(image_url));
		   item.setDate(sf.format(date));
		   item.setPUrl(image_url);
		   item.setSourceLink(eles.get(0).attr("href"));
		   item.setTitle(FixedTaoBaoTitle.fixTaoBaoTitle(ele.text()));
		   item.setDes(item.getTitle());
		   item.setMId(movie_id);
		   item.setSourceType(sourceType);
		   item.setKeyword(keywords);
		   item.setGroupNum(0);
		   item.setIsExternal("0");
		   taoBaoList.add(item);
		   System.out.println(item.getPID()+"\t"+item.getDate()+"\t"+item.getPUrl()+"\t"+item.getSourceLink()+"\t"+item.getTitle()+"\t"+item.getDes()+"\t"+item.getMID()+"\t"+item.getSourceType()+"\t"+item.getKeyword());
    		}
    		try {
				Thread.sleep(1000*2);
			} catch (InterruptedException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		//break;
    	}
    	}
    	return taoBaoList;
    	
	}

//	private String retrievalDesFromSingleLink(String link) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException 
//	{
//		WebClient webClient = new WebClient();
//		webClient.setJavaScriptEnabled(false);
//    	webClient.setCssEnabled(false);
//    	webClient.setRefreshHandler(new ThreadedRefreshHandler());
//    	//webClient.setAjaxController(new AjaxController());
//    	
//    	webClient.setAjaxController(new NicelyResynchronizingAjaxController());
//    //	webClient.setTimeout(WEB_CLIENT_TIMEOUT);
//    	HtmlPage	htmlPage = webClient.getPage(link);
//    	Thread.sleep(100000);
//    	Document doc = Jsoup.parse(htmlPage.asXml());
//    	String text=doc.select("[class=content]").text();
//		// TODO Auto-generated method stub
//		return text;
//	}


	private ArrayList<String> retrievalUsefulLinks()
	{
		
		return null;
	}
	
	public String retrievalImageFromSingleLink(String singleLink) throws NoSuchMethodException, ScriptException, InterruptedException
	{
		try {
			String s=TaoBaoCrawlerSingleWeb.crawlSinglePage(singleLink,src);
			if(s!=null)
			{
				if(s.length()>2)
				{
					//System.out.println(src);
					return s;
				}
				else
				{
					//System.out.println(data_src);
					s=TaoBaoCrawlerSingleWeb.crawlSinglePage(singleLink,data_src);
					return s;
				}
			
			}

		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
		
	}
	
	private void imageLocalizer(ArrayList<RideoItem> imageList,String wpath,String outpath,int p)
	{
		for(int i=0;i<imageList.size();i++)
		{
			RideoItem item=imageList.get(i);
			try {
				ImageItem image=TaoBaoCrawlerSingleWeb.storeSingleImage(item.getPUrl(),wpath, outpath, p*40+i);
				item.setLocalAdd(image.getPath());
				item.setWidths(image.getWidth());
				item.setHeights(image.getHeight());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		
			
		
	}
	
	
}

package crawl;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;






import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import query.MovieQueryFactory;

import database.MysqlDatabase;
import entity.MovieItem;

import util.FileName2Pinyin;
import util.MD5Generator;
import util.URLEncoding;


public class Grawler 
{  
	Logger taskLog=Logger.getLogger("task");
	
	double threashold;
	int number_limit;
	HashMap<String,String>  movie_nameID;
	ArrayList<String> webList;
	MysqlDatabase database=new MysqlDatabase();
	String folder="/rideo/";
   // String folder="/mnt/nfs/nas179/rideo/";
	public Grawler()
	{
		 java.util.Properties prop=new 	java.util.Properties();
		 try {
			prop.load(new FileInputStream("./config"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		
		number_limit=new Integer(prop.get("number").toString());
		try
		{
		 MovieQueryFactory mqf=new MovieQueryFactory("GoogleImage");
	     ArrayList<MovieItem> miList=mqf.getMovieItemQuery();
		 webList=database.batchsearchWebsitesFromMysql();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public ObjectMapper getJsonMapper() 
	{
		ObjectMapper mapper = new ObjectMapper(); 
		mapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
		mapper.configure(Feature.INDENT_OUTPUT, true);
		return mapper;
	}
	public synchronized void crawler_google_image_htmlFormat(String date,MovieItem mi, String site,BlockingQueue<HashMap<String,String[]>> taskQueue)  
	{
		taskLog.info(mi.get_movie_name());
	    String movieName=mi.get_movie_name();
		 String pinyin=FileName2Pinyin.convertHanzi2PinyinStr(movieName);
		 java.util.Properties prop=new 	java.util.Properties();
		File movieFolder=new File("/mnt/nfs/nas179/rideo/"+pinyin);
	   // File movieFolder=new File(folder+pinyin);
		  if(!movieFolder.exists())
		  {
			  System.out.println(movieFolder.getAbsolutePath());
			  movieFolder.mkdirs();
		  }
		  
			String[] user_agents = 
				{
					       "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)",//ok
					     //  "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20130406 Firefox/23.0", //not work
			             //  "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0", //not work
			               "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533 (KHTML, like Gecko) Element Browser 5.0",//ok
			               "IBM WebExplorer /v0.94', 'Galaxy/1.0 [en] (Mac OS X 10.5.6; U; en)"//ok
			             //  "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",//not ok
			             //  "Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14",//not ok
			             //  "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25",//not work
			             //  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1468.0 Safari/537.36",//not work
			              // "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0; TheWorld)",//not work
			               //"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.66 Safari/537.36"
			};
			int num_pages=0;
			try 
			{
				String first_urlStr="https://www.google.com/search?q=\""+URLEncoding.encode(movieName)+"\"+site:"+site+"&source=lnms&tbm=isch";
				
				WebClient webClient = new WebClient();
				webClient.getOptions().setJavaScriptEnabled(false);
		    	webClient.getOptions().setCssEnabled(false);
		    	webClient.getOptions().setTimeout(60000);
		    	webClient.setRefreshHandler(new ThreadedRefreshHandler());
		    	webClient.setAjaxController(new AjaxController());
		    	HtmlPage first_htmlPage=null;
		    	first_htmlPage = webClient.getPage(first_urlStr);
		    	Document first_doc = Jsoup.parse(first_htmlPage.asXml());

		    	Boolean noresutls=first_doc.html().contains("No results found for");
		    	
		    	if(first_doc.select("[id=resultStats]")!=null&&!noresutls)
		  {
		  String number=first_doc.select("[id=resultStats]").html().trim().replace("About", "").replace("results", "").replace(",","").replace("result","").trim();
		 	
	
		  if(number.trim().length()!=0)
		 num_pages=(int) (Double.valueOf(number)/20)+1;
	
	
		  if(num_pages>50)
		  {
			  movieName="《"+movieName+"》";
			  first_urlStr="https://www.google.com/search?q=\""+URLEncoding.encode(movieName)+"\"+site:"+site+"&source=lnms&tbm=isch";
				System.out.println(first_urlStr);
				webClient = new WebClient();
				webClient.getOptions().setJavaScriptEnabled(false);
		    	webClient.getOptions().setCssEnabled(false);
		    	webClient.getOptions().setTimeout(60000);
		    	webClient.setRefreshHandler(new ThreadedRefreshHandler());
		    	webClient.setAjaxController(new AjaxController());
		    	first_htmlPage=null;
		    	first_htmlPage = webClient.getPage(first_urlStr);
		    	first_doc = Jsoup.parse(first_htmlPage.asXml());
		    	
		    	if(first_doc.select("[id=resultStats]")!=null)
		    	{
		 number=first_doc.select("[id=resultStats]").html().trim().replace("About", "").replace("results", "").replace(",","").trim();
		    	
		 num_pages=(int) (Double.valueOf(number)/20)+1;
			System.out.println(num_pages);
		  }
		  }
		  if(num_pages>25)
		  {
			  num_pages=25;
		  }
		    	
		}
		   else
		    	{
		    		taskLog.info("no objects for "+movieFolder);
		    		System.out.println("no objects for "+movieFolder);
		    	}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
	      for(int i=0;i<num_pages;i++)
	      {
	    try
	    {
	     String urlStr="https://www.google.ca/search?q=\""+URLEncoding.encode(movieName)+"\"+site:"+site+"&source=lnms&tbm=isch&start="+i*20;
           System.out.println(urlStr);
		   URL url=new URL(urlStr);
	   
	    	HttpURLConnection cont = (HttpURLConnection) url.openConnection();
	    	cont.setConnectTimeout(10000);
	    	int random=(int)(Math.random()*2) ;
	        cont.setRequestProperty("User-Agent",user_agents[random] );            
	        cont.connect();
	        BufferedReader  reader = new BufferedReader(new InputStreamReader(cont.getInputStream()));
	        String outputFileName=movieFolder.getAbsolutePath()+"/"+MD5Generator.execute(site)+"_"+date+"_"+i+".html";
	      
	        FileWriter fw = new FileWriter(outputFileName);
			BufferedWriter bw = new BufferedWriter(fw);
	        String s;        
	        while((s=reader.readLine())!=null) 
	        {
	             bw.write(new String(s.getBytes(),"UTF-8"));
	        }        
	        
	        bw.flush();
	        bw.close();
	        HashMap<String,String[]> movieTask=new HashMap<String,String[]>();
	        
            movieTask.put("movieName", new String[]{mi.get_movie_name()});
	        movieTask.put("movieID",new String[]{mi.get_movie_id()});
	        movieTask.put("director", new String[]{mi.get_director()});
	        movieTask.put("actors", mi.get_actor_list());
	        movieTask.put("folderName", new String[]{outputFileName});
	        
	        
	        
	        taskQueue.put(movieTask);
	        System.out.println("Producing"+"\t"+outputFileName);
	        taskLog.info("Producing"+"\t"+outputFileName);
	        
	    }  
		      catch(Exception e)
		      {
		    	  e.printStackTrace();
		    	  System.out.println("blocked by google, wait for 30 min");
		    	  try {
					Thread.sleep(1000*60*3*10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		      }
		     }
	      int r=(int) (Math.random()*2);
	      try
	      {
	    	  
	    	  System.out.println("waiting for "+2*r+" min");
	          Thread.sleep(1000*60*2*r);
	      }
	      catch(Exception e)
	      {
	    	  e.printStackTrace();
	    	  
	      }
	      
	  }

	
	public static void main(String[] args) 
	{
		
		try
		{
//			String[] kws=new String[]{keyword_title,keyword_actor_1,keyword_actor_2,keyword_actor_3};
//			new RSSCrawler().crawler_baidu(kws);
       

		 //  Douban d=new Douban("杩滄柟鍦ㄥ摢閲�);
		//   new RSSCrawler().crawler_baidu_JsonFormat("杩滄柟鍦ㄥ摢閲�, d.movie.actors);

//		  RSSCrawler.crawl();
			
              Grawler gcrawler=new Grawler();
              String date="3-14";
              String movieName="角色";
		//gcrawler.crawler_google_image_htmlFormat(date, movieName, "fashion.sina.com.cn",null);

		   
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

}

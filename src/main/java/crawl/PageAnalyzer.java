package crawl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.lionsoul.jcseg.test.JcsegTest;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import entity.GoogleImageItem;

import util.WordSimilarity;





public class PageAnalyzer 
{

	WordSimilarity wordSimi;
	
	
	public PageAnalyzer()
	{
		 try {
			wordSimi=new WordSimilarity();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String[] query={"北京遇上西雅图","汤唯"};
		String in="http://fashion.ifeng.com/beauty/hair/detail_2014_02/07/33576592_0.shtml";
		try {
			System.out.println(new PageAnalyzer().analyze_page(query,in));
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public  double analyzer(String query,String htmlFile)
	{
		try
		{
			
			ArrayList<GoogleImageItem> itemList=new ArrayList<GoogleImageItem>();
			
			Document doc = Jsoup.parse(new File(htmlFile), "UTF-8");
			Elements tableElement=doc.getElementsByClass("images_table");
		
			for(int i=0;i<tableElement.size();i++)
			{
				Elements imageElements=tableElement.get(i).select("td[style]");
				
				for(int j=0;j<imageElements.size();j++)
				{
//					System.out.println(imageElements.get(j).select("cite[title]").text());
//					System.out.println(imageElements.get(j).text());
//					System.out.println(imageElements.get(j).getElementsByAttribute("href").get(0).attributes().get("href"));
//					
//					
					GoogleImageItem item=new GoogleImageItem();
					
					
					item.set_source(imageElements.get(j).select("cite[title]").text());
					item.set_short_text(imageElements.get(j).text().replace(item.get_source(), ""));
					item.set_link(imageElements.get(j).getElementsByAttribute("href").get(0).attributes().get("href").split("=")[1].split("&")[0]);
					
					
					itemList.add(item);
					
//					System.out.println(item.get_source());
//					System.out.println(item.get_link());
//					System.out.println(item.get_short_text());
				}
			
			}
			
			double score=0.0;
			for(int i=0;i<itemList.size();i++)
			{
				//System.out.println(itemList.get(i).get_short_text().trim());
				//String[] strC=test.segment(itemList.get(i).get_short_text()).trim().split(" ");
				//System.out.println(strC);
				
				//System.out.println(score);
				
				if(itemList.get(i).get_short_text().trim().contains(query))
					score=score+1.0;
			}
			
			
	//System.out.println("score:"+score/20);
	return score/itemList.size();
		}
		
		/*
		 * we caculate the query and text relations. 
		 */
	
		
		
		
		
		
		catch(Exception e)
		{
			e.printStackTrace();
			return 0.0;
		}
		
		
		
	}

	
	
	public double analyze_page(String[] query, String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException
	{
		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false);
    	webClient.getOptions().setCssEnabled(false);
    	webClient.getOptions().setTimeout(60000);
    	webClient.setRefreshHandler(new ThreadedRefreshHandler());
    	webClient.setAjaxController(new AjaxController());
    	HtmlPage htmlPage=null;
		htmlPage = webClient.getPage(url);
		List<HtmlAnchor> ha=htmlPage.getAnchors();
		for(int i=0;i<ha.size();i++)
		System.out.println(ha.get(i).asXml());
		return 0.0;
	}
	
	
	public void close()
	{
		wordSimi.close();
	}
	public double caculateSim(String[] query, String[] text) throws SQLException
	{
		String movieName=query[0];
		String queryWord=query[1];
		double count=0.0;
		double num=0.0;
		
		for(int i=0;i<text.length;i++)
		{
			if(Pattern.matches("[\u4e00-\u9fa5]++[\\d]*",text[i]))//regular expression helps match Chinese
			{
				if(!text[i].trim().equals((movieName.trim())))
				{
					//System.out.println(queryWord+"\t"+text[i].trim());
					//System.out.println(this.wordSimi.getSimilarity(queryWord, text[i].trim()));
					num=num+this.wordSimi.getSimilarity(queryWord, text[i].trim());
					count++;
				}
				
			}
		}
//		System.out.println("num"+num);
//		System.out.println("count"+count);
//		System.out.println(num/count);
		if(count==0)
			return 0;
		return num/count;
	}

}

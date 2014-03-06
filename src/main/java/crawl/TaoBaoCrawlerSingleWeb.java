package crawl;


import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;



import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import cern.colt.Arrays;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import entity.ImageItem;

public class TaoBaoCrawlerSingleWeb{

	private static final int URL_CONNECT_TIMEOUT = 1000*30;
	private static final int URL_READ_TIMEOUT = 1000*60;
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args){
		// TODO Auto-generated method stub
//		String q="http://item.taobao.com/item.htm?id=35633049435";
//		String att="data-src";
//		try {
//			String s=TaoBaoCrawlerSingleWebCrawlerSingleWeb.crawlSinglePage(q,att);
//			System.out.println(s);
//		} catch (FailingHttpStatusCodeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String url="http://gi2.md.alicdn.com/bao/uploaded/i2/14821026521739521/T1EIKPXvBhXXXXXXXX_!!0-item_pic.jpg_460x460q90.jpg";
		try {
			System.out.println(new TaoBaoCrawlerSingleWeb().storeSingleImage(url, "D:","/mnt/nfs/asc", 1));
			System.out.println("finish");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*
	 * attr could be src or data-src
	 */
	public static String crawlSinglePage(String singleLink,String attr) throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException 
	{
		WebClient webClient = new WebClient();
		webClient.getOptions().setJavaScriptEnabled(false);
    	webClient.getOptions().setCssEnabled(false);
    	webClient.setRefreshHandler(new ThreadedRefreshHandler());
    	//webClient.setAjaxController(new AjaxController());
    	webClient.addRequestHeader("Referer", singleLink);
    	webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    //	webClient.setTimeout(WEB_CLIENT_TIMEOUT);
    	HtmlPage	htmlPage = webClient.getPage(singleLink);
    	Thread.sleep(10000);
    	Document doc = Jsoup.parse(htmlPage.asXml());
   
    //	System.out.println(doc.html());
    	//System.out.println(singleLink);
		Elements single_eles=doc.select("[id=J_ImgBooth]");
	
		
		if(single_eles.size()>0)
		{
	//	System.out.println(single_eles.get(0));
		//System.out.println(single_eles.get(0).html());
	//	System.out.println(single_eles.get(0).attr(attr));
		return single_eles.get(0).attr(attr);
		}
		else
			return null;
	
	}

	
	public static ImageItem storeSingleImage(String urlLink, String winPath,String outpath,int imageNameId) throws IOException
	{
		String extension = urlLink.substring(urlLink.lastIndexOf('.'));
		URL img = new URL(urlLink);
		HttpURLConnection conn = (HttpURLConnection)img.openConnection();
		conn.setConnectTimeout(URL_CONNECT_TIMEOUT);
		conn.setReadTimeout(URL_READ_TIMEOUT);
		InputStream in = null;
		in = conn.getInputStream();
		
		DataInputStream dataInputStream = null;
		DataOutputStream out = null;
		String filename = String.valueOf(imageNameId++)+extension;
		String returnPath = outpath+"/"+filename;
		String savePath=winPath+"/"+filename;
		dataInputStream = new DataInputStream(in);
		out = new DataOutputStream(new FileOutputStream(savePath));
		byte[] buffer = new byte[4096];
		int count = 0;
		while ((count = dataInputStream.read(buffer)) > 0)/*将输入流以字节的形式读取并写入buffer中*/
		{
			out.write(buffer, 0, count);
		}
		out.flush();
		out.close();/*后面三行为关闭输入输出流以及网络资源的固定格式*/
		dataInputStream.close();
		
		
		
		HttpURLConnection connImage = (HttpURLConnection)img.openConnection();
		conn.setConnectTimeout(URL_CONNECT_TIMEOUT);
		conn.setReadTimeout(URL_READ_TIMEOUT);
		BufferedImage image = ImageIO.read(connImage.getInputStream());
		
		ImageItem item=new ImageItem();
		System.out.println(returnPath);
		System.out.println(image.getWidth());
		System.out.println(image.getHeight());
		item.setPath(returnPath);
		item.setWidth(image.getWidth());
		item.setHeight(image.getHeight());
		return item;
	}
		
	
	
}

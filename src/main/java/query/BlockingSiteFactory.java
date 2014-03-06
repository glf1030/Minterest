package query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import database.MysqlDatabase;

public class BlockingSiteFactory {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String urlstr = "http://www.rpgwebgame.com/images/v3/webgame/2.jpg";
		URL url = new URL(urlstr);
		System.out.println(url.getHost());
		BlockingSiteFactory bsf = new BlockingSiteFactory();
		
		HashSet<String> sites = bsf.getSites();
		System.out.println(sites.contains(url.getHost()));
		
	}
	HashSet<String> blockingSites = null;
	public BlockingSiteFactory() {
		if(blockingSites==null)
			blockingSites= new HashSet();
		try
		{
			MysqlDatabase md=new MysqlDatabase();
			ArrayList<String> queryList=md.getBlockingSites();
		
			for(String q:queryList)
				{
				URL url = new URL(q);
				blockingSites.add(url.getHost());
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
//	public BlockingSiteFactory() {
//		// TODO Auto-generated constructor stub
//		this.blockingSites = new HashSet<>();
//		try {
//			this.blockingSites.add(new URL("http://www.rpgwebgame.com/").getHost());
//			this.blockingSites.add(new URL("http://www.jnyl.org.cn/").getHost());
//		} catch (MalformedURLException e) {
//			return;
//		}
//	}
	
	public HashSet<String> getSites(){
		return this.blockingSites;
	}

}

package database;


import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;




import com.mysql.jdbc.Connection;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

import entity.GoogleImageItem;
import entity.MovieItem;
import entity.RideoItem;
import entity.TaoBaoItem;


public class MysqlDatabase {

	Logger taskLog=Logger.getLogger("database");
	static String driver = "com.mysql.jdbc.Driver";

	public static Connection conn =null;
	
	public MysqlDatabase()
	{
		try
		{
			Class.forName(driver);
	
		
		if(conn==null)
		{
			
//			conn = (Connection) DriverManager.getConnection("jdbc:mysql://192.168.1.55:3306/Rideo?useUnicode=true&characterEncoding=gbk","root","111111");
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/Rideo?useUnicode=true&characterEncoding=gbk","root","111111");
			
		//	conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/ContentDiary?useUnicode=true&characterEncoding=UTF-8","root","111111");
			
			
		//	conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/ContentDiary?useUnicode=true&characterEncoding=UTF-8","root","glf1030");

			System.out.println("sucess connect mysql");

		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public  boolean insertDataCollectionintoMysql(ArrayList<TaoBaoItem> taoBaoCollection)
	{

		try {
			
			
	    for(int i=0;i<taoBaoCollection.size();i++)
	    {
	    
	    	TaoBaoItem single_new=taoBaoCollection.get(i);
		PreparedStatement stat=conn.clientPrepareStatement("INSERT INTO Published_ImRep VALUES(?,?,?,?,?,?,?,?,?,?,?)"); 
		    
			/* id
			 * movie_name
			 * key_word
			 * title
			 * date
			 * content
			 * image_url
			 * gossip_url
			 *image_add
			 *movie_id
			 */
			stat.setInt(1, 0);
			stat.setString(2, single_new.get_Movie_Name()); 
			stat.setString(3, single_new.get_movie_id()); 
			stat.setString(4, single_new.get_url()); 
		    stat.setString(5,  single_new.get_soruce());
			stat.setString(6, single_new.get_alt()); 
			stat.setString(7, single_new.get_localAdd()); 
			stat.setString(8, single_new.get_from()); 
			stat.setString(9, single_new.get_title());
			stat.setInt(10, single_new.get_interesting());
			stat.setInt(11, single_new.get_group());

			int index=stat.executeUpdate();
			System.out.println("insert data "+index);
	    }
	    

		
		
		
			
        return true;
			
		} 
		
		
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}  
		
		
		
		
		
	
	}

	
	public ArrayList<MovieItem> batchsearchTaobaoMovieDataFromMysql()
	{
		ArrayList<MovieItem> miList=new ArrayList<MovieItem>();
		
		 try
		 {
			 String sql="select * from ContentDiary.movie where taobao_search=1";
			 PreparedStatement stmt = conn.clientPrepareStatement(sql);
		
			  ResultSet rs = stmt .executeQuery(sql);
			  
			  while(rs.next())
			  { 
			   
			   MovieItem mi=new MovieItem();
			   String movie_name=rs.getString("movie_name");
			   movie_name= movie_name.substring(0,movie_name.indexOf("("));
			   mi.set_movie_name(movie_name);
			   mi.set_movie_id(rs.getString("movie_id"));
			   mi.set_director(rs.getString("director"));
			   mi.set_actor_list((rs.getString("actor_list").split(";")));
			   
			   miList.add(mi);
			  }
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 
		 System.out.println(miList.size());
		
		return miList;
	}

	

	
	
	


	public ArrayList<String>  getTaoBaoKeyWords()
	{
		ArrayList<String> taobaoKeywords=new ArrayList<String>();
		
		String sql="SELECT * FROM Minterest.TaoBaoQuery";
		 PreparedStatement stmt;
		try {
			stmt = conn.clientPrepareStatement(sql);
			ResultSet rs = stmt .executeQuery(sql);
			while(rs.next())
			{
				taobaoKeywords.add(rs.getString("query"));
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		 return taobaoKeywords;
		
	}
	
	/**
	 * Not finished yet.
	 * @return
	 */
	public ArrayList<String> getBlockingSites(){
		ArrayList<String> taobaoKeywords = new ArrayList<String>();

		String sql = "SELECT * FROM Minterest.Blocking";
		PreparedStatement stmt;
		try {
			stmt = conn.clientPrepareStatement(sql);
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				taobaoKeywords.add(rs.getString("query"));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return taobaoKeywords;
	}

	/*
	 * true: 插入的数据，数据库里面并不存在
	 * false: 插入的数据，数据库里面有。
	 */
	
	public Boolean InsertLatestRecords(ArrayList<RideoItem> rideoItemList) throws SQLException 
	{
		 Boolean existed=false;
		
	    for(int i=0;i<rideoItemList.size();i++)
	    {
	    
	    	RideoItem single_new=rideoItemList.get(i);
	    	
	    	String pid=single_new.getPID();
	    	
	    	String sql="select * from Minterest.Rideo_Initial where p_id='"+pid+"'";
	    	 PreparedStatement stmt;
	    	 stmt = conn.clientPrepareStatement(sql);
				ResultSet rs = stmt .executeQuery(sql);
				while(rs.next())
				{
					
					existed=true;
				}
	    	 
				if(!existed)
				{
		    PreparedStatement stat=null;
		
			stat = conn.clientPrepareStatement("INSERT INTO Minterest.Rideo_Initial VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			stat.setInt(1, 0);
			stat.setString(2, single_new.getPID()); 
			stat.setString(3, single_new.getMID());
			stat.setString(4, single_new.getPUrl()); 
		    stat.setString(5,  single_new.getDes());
			stat.setString(6, single_new.getSourceLink()); 
			stat.setString(7, single_new.getLocalAdd()); 
			stat.setString(8, single_new.getTitle()); 
			stat.setString(9, single_new.getSourceType());
			stat.setString(10, single_new.getIsExternal());
			stat.setString(11, single_new.getIsInteresting());
			stat.setInt(12, single_new.getGroupNum());
			stat.setString(13, single_new.getTags());
			stat.setDouble(14, single_new.getWidths());
			stat.setDouble(15, single_new.getHeights());
			stat.setString(16, single_new.getIssues());
			stat.setString(17, single_new.getDate());
			stat.setString(18, single_new.getKeyword());

			stat.execute();
			
				}

	    }
	 
	    return existed;
	}
	
	
	
	
	public void close()
	{
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws SQLException
	{
		/*
		 * success. 
		 * test by lg
		 */
		MysqlDatabase mdb=new MysqlDatabase();
		MovieItem mi=mdb.getMovieItemByMovieID("8c981058-6d19-4452-b93e-daa4b45a4c4b");
		System.out.println(mi.get_movie_name());
//		ArrayList<MovieItem> miList=mdb.batchsearchTaobaoMovieDataFromMysql();
//		for(MovieItem mi:miList)
//		{
////			for(String s:mi.get_actor_list())
////				System.out.println(s);
//			System.out.println(mi.get_movie_name());
//			
//		}
	}


	public ArrayList<String> batchsearchWebsitesFromMysql() 
	{
ArrayList<String> fashionLinks=new ArrayList<String>();
		
		String sql="SELECT * FROM Minterest.FashionLink";
		 PreparedStatement stmt;
		try {
			stmt = conn.clientPrepareStatement(sql);
			ResultSet rs = stmt .executeQuery(sql);
			while(rs.next())
			{
				fashionLinks.add(rs.getString("link"));
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		 return fashionLinks;
	}


	public ArrayList<MovieItem> batchsearchGoogleImageMovieDataFromMysql() {
		ArrayList<MovieItem> miList=new ArrayList<MovieItem>();
		
		 try
		 {
			 String sql="select * from ContentDiary.movie where google_isearch=1";
			 PreparedStatement stmt = conn.clientPrepareStatement(sql);
		
			  ResultSet rs = stmt .executeQuery(sql);
			  
			  while(rs.next())
			  { 
			   
			   MovieItem mi=new MovieItem();
			   String movie_name=rs.getString("movie_name");
			   movie_name= movie_name.substring(0,movie_name.indexOf("("));
			   mi.set_movie_name(movie_name);
			   mi.set_movie_id(rs.getString("movie_id"));
			   mi.set_director(rs.getString("director"));
			   mi.set_actor_list((rs.getString("actor_list").split(";")));
			   
			   miList.add(mi);
			  }
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 
		
		return miList;
	}


	public MovieItem getMovieItemByMovieID(String movieID) 
	{
		MovieItem mi=new MovieItem();
		 try
		 {
			 String sql="select * from ContentDiary.movie where movie_id='"+movieID+"' limit 1";
			 PreparedStatement stmt = conn.clientPrepareStatement(sql);
		
			  ResultSet rs = stmt .executeQuery(sql);
			  
			  while(rs.next())
			  { 
			   
			   
			   String movie_name=rs.getString("movie_name");
			   movie_name= movie_name.substring(0,movie_name.indexOf("("));
			   mi.set_movie_name(movie_name);
			   mi.set_movie_id(rs.getString("movie_id"));
			   mi.set_director(rs.getString("director"));
			   mi.set_actor_list((rs.getString("actor_list").split(";")));
			  
			  }
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 return mi;
	}


	public BlockingQueue<String> getGoogleImageQueryList() 
	{
		BlockingQueue<String> bq=new LinkedBlockingQueue<String>();
		MovieItem mi=new MovieItem();
		 try
		 {
			 String sql="SELECT * FROM Minterest.FashionLink";
			 PreparedStatement stmt = conn.clientPrepareStatement(sql);
		
			  ResultSet rs = stmt .executeQuery(sql);
			  
			  while(rs.next())
			  { 
			   
			   
			   String link=rs.getString("link");
			   bq.put(link);
			  
			  }
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 return bq;
	}
	
	public Boolean insertRideoItemRecord(RideoItem rideoItem)
			throws MySQLIntegrityConstraintViolationException,SQLException {

		Boolean existed = false;

		String pid = rideoItem.getPID();

		String sql = "select * from Minterest.Rideo_GoogleImage where p_id='" + pid
				+ "'";
		PreparedStatement stmt;
		stmt = conn.clientPrepareStatement(sql);
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()) {

			existed = true;
		}

		PreparedStatement stat = null;

		stat = conn
				.clientPrepareStatement("INSERT INTO Minterest.Rideo_GoogleImage VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		stat.setInt(1, 0);
		stat.setString(2, rideoItem.getPID());
		stat.setString(3, rideoItem.getMID());
		stat.setString(4, rideoItem.getPUrl());
		stat.setString(5, rideoItem.getDes());
		stat.setString(6, rideoItem.getSourceLink());
		stat.setString(7, rideoItem.getLocalAdd());
		stat.setString(8, rideoItem.getTitle());
		stat.setString(9, rideoItem.getSourceType());
		stat.setString(10, rideoItem.getIsExternal());
		stat.setString(11, rideoItem.getIsInteresting());
		stat.setInt(12, rideoItem.getGroupNum());
		stat.setString(13, rideoItem.getTags());
		stat.setDouble(14, rideoItem.getWidths());
		stat.setDouble(15, rideoItem.getHeights());
		stat.setString(16, rideoItem.getIssues());
		stat.setString(17, rideoItem.getDate());
		stat.setString(18, rideoItem.getKeyword());
//		taskLog.info(Thread.currentThread().getName()+"\tInsert sql:"+stat.toString());
		stat.execute();
		
		return existed;

	}
}

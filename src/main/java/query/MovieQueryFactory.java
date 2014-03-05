package query;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import database.MysqlDatabase;
import entity.MovieItem;

public class MovieQueryFactory {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		//String source="./movieList";
		


	}
private  HashMap<String, String> taoBaoquery=new HashMap<String,String>();
private  ArrayList<MovieItem> movieItemQuery=new ArrayList<MovieItem>();
	public  MovieQueryFactory(String type)
	{
		
		try
		{
			if(type.equals("Taobao"))
			{
			MysqlDatabase md=new MysqlDatabase();
			ArrayList<MovieItem> miList=md.batchsearchTaobaoMovieDataFromMysql();
			for(MovieItem mi:miList)
				{
				taoBaoquery.put(mi.get_movie_name(),mi.get_movie_id());
				}
			}
			if(type.equals("GoogleImage"))
			{
				MysqlDatabase md=new MysqlDatabase();
				movieItemQuery=md.batchsearchGoogleImageMovieDataFromMysql();
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	

	
	public HashMap<String,String> getTaobaoQuery()
	{
		return taoBaoquery;
	}
	
	public ArrayList<MovieItem> getMovieItemQuery()
	{
		return movieItemQuery;
	}
}

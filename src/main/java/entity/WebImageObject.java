package entity;

public class WebImageObject{
	public String url;
	public String alt;
	public String text;
	public String title;
	public int width;
	public int height;
	
	public String addr;
	
	
	public WebImageObject(String url,String alt,String text,String title){
		this.alt = alt;
		this.url = url;
		this.text = text;
		this.title = title;
	}
}

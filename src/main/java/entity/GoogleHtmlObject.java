package entity;

public class GoogleHtmlObject{
	public String smallImageUrl;
	public String webUrl;
	public String summary;
	public String cite;
	public String height;
	public String width;
	public String saveRootDir;
	
	public GoogleHtmlObject(
			String smallImageURL,
			String webUrl,
			String summary,
			String cite,
			String height,
			String width
			){
		this.smallImageUrl = smallImageURL;
		this.webUrl = webUrl;
		this.summary = summary;
		this.cite = cite;
		this.height = height;
		this.width = width;
	}
//	public GoogleHtmlObject(GoogleHtmlObject obj) {
//		this.smallImageUrl = obj.smallImageUrl;
//		this.webUrl = obj.webUrl;
//		this.title = obj.title;
//		this.cite = obj.cite;
//		this.height = obj.height;
//		this.width = obj.width;
//	}
}
package collector.parser;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import database.MysqlDatabase;
import entity.RideoItem;
import util.ImageCollectorUtils;

public class ImageStorageManager {
	static final Logger LOG = Logger.getLogger(ImageStorageManager.class.getName());
	static final Logger WARN = Logger.getLogger(ImageStorageManager.class.getName());
	
	RideoItem rideoItem = null;
	
	public ImageStorageManager(RideoItem item) {
		// TODO Auto-generated constructor stub
		if(item==null){
			LOG.info("input rideoItem is null");
			return;
		}
		this.rideoItem = item;
		if(this.rideoItem==null){
			LOG.info("rideoItem is null");
		}
	}
	public boolean save2Mysql(){
		if(this.rideoItem==null){
			LOG.info("No RideoItem received by storage manager...");
			return false;
		}
		MysqlDatabase conn = new MysqlDatabase();
		try {
			LOG.info("Start to save rideoItem to db...");
			conn.insertRideoItemRecord(rideoItem);
			
			LOG.info("Finish to save rideoItem to db...");
		}catch(MySQLIntegrityConstraintViolationException e){
			LOG.info(rideoItem.getPUrl()+" has been input into the database....");
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOG.warn("Fail to saved to Mysql...");
			e.printStackTrace();
			return false;
		}
		LOG.info("Saved to Mysql...");
		return true;
	}
	public boolean save2Local(){
		if(this.rideoItem==null){
			LOG.info("No RideoItem received by storage manager...");
			return false;
		}
		String url = this.rideoItem.getPUrl();
		String localAddr = this.rideoItem.getLocalAdd();
		boolean sucess = saveImage(url, localAddr);
		return sucess;
		
	}
	private boolean saveImage(String url,String localAddr){

		if(url==null || url.equals("")){
			WARN.warn(Thread.currentThread().getName()+"\tURL is null or empty...");
			return false;
		}
		
		try {
			LOG.info(Thread.currentThread().getName()+"Start to save image:"+url+"\tTO "+localAddr);
			InputStream in = ImageCollectorUtils.getInputStreamFromURL(url);
			Image img = ImageIO.read(in);
			if (img == null) {
				return false;
			} 
			else {
				ImageIO.write((BufferedImage) img, 
						ImageCollectorUtils.getURLExtension(url).toUpperCase(), 
						new FileOutputStream(new File(localAddr)));
				LOG.info(Thread.currentThread().getName()+"\tSave the image from url:"+url+" to addr:"+localAddr);
				return true;
			}
		}catch(SocketTimeoutException e){
			WARN.warn(Thread.currentThread().getName()+"\tRead source timeout:"+url+"\t"+e.getMessage());
			return false;
		} 
		catch (IOException e) {
			WARN.warn(Thread.currentThread().getName() + "\t" + e.getMessage()
					+ "IOException occurs when save image from url:" + url
					+ " to addr:" + localAddr);
			return false;
		}
	}

}

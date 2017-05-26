package utils;

import java.util.List;

public class cacheObject {
	
	private long time;
	private List<String> URLS;
	
	public cacheObject (long time, List<String> urls){
		this.time = time;
		this.URLS = urls;
	}
	
	public long getTime (){
		return this.time;
	}
	
	public List<String> getUrls(){
		return URLS;
	}
	
	public void setTime(long time){
		this.time = time;
	}
	
	public void setURLS(List<String> URLS){
		this.URLS = URLS;
	}
}

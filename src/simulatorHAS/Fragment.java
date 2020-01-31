package simulatorHAS;

public class Fragment {
	
	private int bitrate = 0;  //storlek per sekund, 1/4 of the fragment equals one second
	public int leftToDownload = 4;
	
	public Fragment(int quality) {
		
		if(quality == 0){
			bitrate = 250;
		}else if(quality == 1){
			bitrate = 500;
		}else if(quality == 2){
			bitrate = 850;
		}else if(quality == 3){
			bitrate = 1300;
		}
		
	}
	
	
	//För varje bitrate som vi laddat ner, 
	//så vill vi lägga till en sekund i buffern.

	public int download(int bandwidth) {
		
		
		int seconds = 0;
		
//		int fragmentsize = bitrate * 4;
		
		seconds = (bandwidth / bitrate);
		
		if(seconds < leftToDownload) {
			leftToDownload -= seconds; 
			
		}else {
			
			seconds = leftToDownload;
			
			leftToDownload = 0;
			
		}
		
		return seconds;
		
	}
	
	public int getLeftToDownload() {
		return leftToDownload;
	}
	
}

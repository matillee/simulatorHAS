package simulatorHAS;

public class Fragment {

	private int bitrate = 0;
	public int leftToDownload = 4;
	private int quality;
	private boolean finished;
	private int secondsPlayed=0;

	public Fragment(int quality) {
		
		this.quality = quality;

		if (quality == 0) {
			bitrate = 250;
		} else if (quality == 1) {
			bitrate = 500;
		} else if (quality == 2) {
			bitrate = 850;
		} else if (quality == 3) {
			bitrate = 1300;
		}

	}

	public int download(int bandwidth, int buffer, int MAXBUF) {

		int seconds = 0;

		seconds = (bandwidth / bitrate);

		if (seconds + buffer >= MAXBUF) {
			
			if((MAXBUF-buffer)>leftToDownload) {
				seconds = leftToDownload;
			}else {
				seconds = MAXBUF - buffer;
			}
			
		}

		if (seconds < leftToDownload) {
			leftToDownload -= seconds;

		} else {

			seconds = leftToDownload;

			leftToDownload = 0;

		}
		
		
		return seconds;

	}

	public int getLeftToDownload() {
		return leftToDownload;
	}
	
	public int getQuality() {
		return this.quality;
	}

	public int getSecondsPlayed() {
		return this.secondsPlayed;
	}
	
	public void playFrame() {
		this.secondsPlayed++;
		if(secondsPlayed == 4) {
			finished = true;
		}
		
	}
	
	public boolean isDone() {
		return finished;
	}
}

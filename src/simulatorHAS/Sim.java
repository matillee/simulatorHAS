package simulatorHAS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Sim {
	
	private final double alpha = 0.1;
	private int previousEst; // previous estimated available bandwidth
	private ArrayList<Integer> bandwidthHistory = new ArrayList<Integer>(); //download rate in kBit/s

	private int buffer;
	private int MINBUF = 4;
	private int MAXBUF = 6;
	private boolean hasCrashed = true; //if video is paused really
	private boolean canDownload = true;
	private boolean downloading = false;
	private int downloadTime = 1;
	private Fragment downloadingFragment;
	private ArrayList<Integer> playbackQuality = new ArrayList<Integer>();
//	private int[] playbackQuality = new int[6];
	private int frame = 0;
	private int counter = 0;
	
	private static BufferedWriter BufferOccWriter;
	private static BufferedWriter StreamNrWriter;
	private static BufferedWriter RequestWriter;

	
	public static void main(String[] args) {
		
		Sim simulator = new Sim();
		
//		simulator.readTraceFile("/home/mater832/Programmering/Tddd66Lab2.3/src/tracefile.txt");
		simulator.readTraceFile("src/tracefile.txt");

		try {
//			BufferOccWriter = new BufferedWriter(new FileWriter("/home/mater832/Programmering/Tddd66Lab2.3/src/input1.txt/"));
//			StreamNrWriter = new BufferedWriter(new FileWriter("/home/mater832/Programmering/Tddd66Lab2.3/src/input2.txt"));
//			RequestWriter = new BufferedWriter(new FileWriter("/home/mater832/Programmering/Tddd66Lab2.3/src/input3.txt"));
			
			BufferOccWriter = new BufferedWriter(new FileWriter("src/input1.txt/"));
			StreamNrWriter = new BufferedWriter(new FileWriter("src/input2.txt"));
			RequestWriter = new BufferedWriter(new FileWriter("src/input3.txt"));
			
			simulator.simulation();

			BufferOccWriter.close();
			StreamNrWriter.close();
			RequestWriter.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
		
		
	}
	
	private void print(int simTime) {
		System.out.println();
		System.out.println("simTime: " + simTime);
		System.out.println("Buffer size: " + buffer);
		System.out.println("Download time: " + downloadTime);
		System.out.println("Frame: " + frame);
		System.out.println("size: " + playbackQuality.size());
		System.out.println();
	}
	
	
	private void simulation(){
		
		for(int simTime=0;simTime<bandwidthHistory.size();simTime++) { 
		
//		for(int simTime=0;simTime<120;simTime++) { //eftersom filen är 2 min = 120 sekunder
			
			//Ska den vara först eller sist..?
			bufferHandler(simTime); //kollar om vi kan ladda ner eller inte
			
			int newBandwidth = bandwidthHistory.get(simTime);
			
			
			if(canDownload) {
				
				if(!downloading) { 

					if(hasCrashed) {
						downloadTime = 1; //om inte laddat ner tidigare..
					}
					
					
					int bandEst = estimatedBandwidth1(newBandwidth, downloadTime);
//					int bandEst = estimatedBandwidth2(newBandwidth, downloadTime);
					
					int requestedQuality = checkQuality(bandEst);
					
					try {
						RequestWriter.write(simTime + " " + requestedQuality + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
//					playbackQuality[buffer] = quality;
					
					int realQuality = getActualQuality(requestedQuality);
					
					playbackQuality.add(realQuality);
					
					downloadingFragment = new Fragment(realQuality);
					
					downloadTime = 0; //Vi har inte börjat ladda ner än
					
					downloading = true;					
				}else {
					
					downloadTime++;
					
					int secondsDownloaded = downloadingFragment.download(newBandwidth, buffer, MAXBUF);
					
					buffer += secondsDownloaded;
					
					if(downloadingFragment.getLeftToDownload() == 0) {
						downloading = false;
					}
					
				}
				
				
			}else {
				
				//Vi kan inte ladda ner... då har vi nått MAXBUF.
				//Vi vill inte göra något då eller? Bara låta tiden gå..
				
			}
			
			
			
			try {
				BufferOccWriter.write(simTime + " " + buffer + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	// used guidelines from lab to come up with reasonable quality choice
	private int checkQuality(int bandwidth) {
		int quality = 0;

		if (bandwidth >= 500 && bandwidth < 850) {
			quality = 1;
		} else if (bandwidth >= 850 && bandwidth < 1300) {
			quality = 2;
		} else if (bandwidth >= 1300) {
			quality = 3;
		}
		return quality;
	}

	private int getActualQuality(int requestedQuality) {
		
		int previousQuality;
		int actualQuality;
		
		if(playbackQuality.isEmpty() || hasCrashed ) {
			previousQuality = -1;
		}else {
			
			previousQuality = playbackQuality.get(playbackQuality.size()- 1);
		}
		
		 
		
		if (requestedQuality > previousQuality) {
			actualQuality = previousQuality + 1;

		} else if (requestedQuality < previousQuality - 2) {
			actualQuality = previousQuality - 2;

		} else {
			actualQuality = requestedQuality;
		}
		
		return actualQuality;
		
	}
	
	private void readTraceFile(String fileName){
		BufferedReader br;
		String text;

		try {
			br = new BufferedReader(new FileReader(fileName));

			while ((text = br.readLine()) != null) {
				String[] data = text.split(" ");
				bandwidthHistory.add(calckBit(Integer.parseInt(data[4]))); 
			}

			br.close();

		} catch (FileNotFoundException e1) {
			System.out.print("File " + " could not be found.");
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	calculate kbit/s from bytes/s
	private int calckBit(int bytes) {
		int bit = (bytes * 8)/1000;
		
		return bit;
	}
	
	private int estimatedBandwidth1(int bytes, int time) {

		this.previousEst = (bytes / time) * 8;

		return this.previousEst;
	}
	
	private int estimatedBandwidth2(int bytes, int time) {

		int newEst = (bytes / time) * 8;

		double est = (1 - this.alpha) * this.previousEst + this.alpha * newEst;

		this.previousEst = (int) est;

		return this.previousEst;
	}
	
	//Kallas på för varje omgång av for loopen
	private void bufferHandler(int simTime) {
		
		print(simTime);
	
		if(buffer == 0) {
			//Pause video
			hasCrashed = true;
		}else {
			
			if(hasCrashed) {

				if(buffer >= MINBUF) {

					hasCrashed = false;
				}
			}else {
				//play video
				
				buffer--;
				try {
					
					if(frame == 1) {
						StreamNrWriter.write(simTime + " " + playbackQuality.get(0)+ "\n");
					}else if (frame > 0){
						StreamNrWriter.write(simTime + " " + playbackQuality.get(frame-2)+ "\n");
					}
					
					counter++;
					if(counter == 4) {
						frame++;
						counter = 0;
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
//			if(buffer <= MINBUF && hasCrashed) {
			if((buffer <= MINBUF) || (buffer > MINBUF && buffer < MAXBUF)) {
				//Start downloading
				canDownload = true;
//				hasCrashed = false;
			}
			
			if(buffer == MAXBUF) {
				//stop downloading
				canDownload = false;
			}
//			else if(buffer == MINBUF && !hasCrashed) {
//				canDownload = true;
//			}
		}
		
	}
	
	
	
	

}

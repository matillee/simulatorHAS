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
	
	private static BufferedWriter BufferOccWriter;
	private static BufferedWriter StreamNrWriter;
	private static BufferedWriter RequestWriter;

	
	public static void main(String[] args) {
		
		Sim simulator = new Sim();
		
//		simulator.readTraceFile("/home/mater832/Programmering/Tddd66Lab2.3/src/tracefile.txt");
		simulator.readTraceFile("src/tracefile.txt");
		simulator.simulation();

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
	
	
	private void simulation(){
		
		for(int simTime=0;simTime<bandwidthHistory.size();simTime++) { 
		
//		for(int simTime=0;simTime<120;simTime++) { //eftersom filen är 2 min = 120 sekunder
			
			//Ska den vara först eller sist..?
			bufferHandler(); //kollar om vi kan ladda ner eller inte
			
			int newBandwidth = bandwidthHistory.get(simTime);
			
			
			if(canDownload) {
				
				if(!downloading) { 

					if(hasCrashed) {
						downloadTime = 1; //om inte laddat ner tidigare..
					}
					
					
					int bandEst = estimatedBandwidth1(newBandwidth, downloadTime);
//					int bandEst = estimatedBandwidth2(newBandwidth, downloadTime);
					
					int quality = checkQuality(bandEst);
					
					playbackQuality.add(quality);
//					playbackQuality[buffer] = quality;
					
					downloadingFragment = new Fragment(quality);
					
					downloadTime = 0; //Vi har inte börjat ladda ner än
					
				}else {
					
					downloadTime++;
					
					int secondsDownloaded = downloadingFragment.download(newBandwidth);
					
					buffer += secondsDownloaded;
					
					if(downloadingFragment.getLeftToDownload() == 0) {
						canDownload = false;
					}
					
				}
				
				
			}else {
				
				//Vi kan inte ladda ner... då har vi nått MAXBUF.
				//Vi vill inte göra något då eller? Bara låta tiden gå..
				
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
	private void bufferHandler() {
		
		if(buffer == 0) {
			//Pause video
			hasCrashed = true;
		}else {
			
			if(hasCrashed) {
				//don't play video until minBuf is reached
				if(buffer >= MINBUF) {
					//play video	
					//buffer--; //Väntar till nästa gång..
					hasCrashed = false;
				}
			}else {
				//play video
				
				buffer--; //hur vet vi vilken kvalitet vi spelar?
				try {
					StreamNrWriter.write(playbackQuality.get(frame));
					frame++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			if(buffer < MINBUF) {
				//Start downloading
				canDownload = true;
			}
			
			if(buffer > MAXBUF) {
				//stop downloading
				canDownload = false;
			}
		}
		
	}
	
	
	
	

}

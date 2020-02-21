package simulatorHAS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import hejhej.Fragment;
import hejhej.Sim;

public class Sim {

	private final double alpha = 1;
	private int previousEst = 1; // previous estimated available bandwidth
	private ArrayList<Integer> bandwidthHistory = new ArrayList<Integer>(); // download rate in kBit/s

	private int buffer;
	private int MINBUF = 4;
	private int MAXBUF = 6;
	private boolean hasCrashed = true;
 
	private boolean canDownload = true;
	private boolean downloading = false;
	private int downloadTime = 1;
	private Fragment downloadingFragment;

	private ArrayList<Fragment> fragments = new ArrayList<Fragment>();
	private int frame = 0;

	private int realQuality;
	private int prev;

	private static BufferedWriter BufferOccWriter;
	private static BufferedWriter StreamNrWriter;
	private static BufferedWriter RequestWriter;

	public static void main(String[] args) {

		Sim simulator = new Sim();

		simulator.readTraceFile("/home/mater832/Programmering/Tddd66Lab2.3/src/tracefile.txt");


		try {

			BufferOccWriter = new BufferedWriter(new FileWriter("/home/mater832/Programmering/hejhej/src/input1.txt/"));
			StreamNrWriter = new BufferedWriter(new FileWriter("/home/mater832/Programmering/hejhej/src/input2.txt"));
			RequestWriter = new BufferedWriter(new FileWriter("/home/mater832/Programmering/hejhej/src/input3.txt"));

			simulator.simulation();

			BufferOccWriter.close();
			StreamNrWriter.close();
			RequestWriter.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}


	private void simulation() {

		for (int simTime = 0; simTime < bandwidthHistory.size(); simTime++) {

			int newBandwidth = bandwidthHistory.get(simTime);

			if (canDownload) {

				if (!downloading && (fragments.size() < 30)) {

					if (fragments.isEmpty()) {
						downloadTime = 1; 
					}

					int bandEst = estimatedBandwidth1(newBandwidth, downloadTime);
//					int bandEst = estimatedBandwidth2(newBandwidth, downloadTime);

					int requestedQuality = checkQuality(bandEst);

					realQuality = getActualQuality(requestedQuality);

					try {
						RequestWriter.write(simTime + " " + realQuality + "\n");

					} catch (IOException e) {

						e.printStackTrace();
					}

					downloadingFragment = new Fragment(realQuality);
					fragments.add(downloadingFragment);

					downloadTime = 0;
					prev = 0;
					downloading = true;

				} else if (downloading) {

					downloadTime++;

					int secondsDownloaded = downloadingFragment.download(newBandwidth, buffer, MAXBUF);

					buffer += secondsDownloaded;

					if (downloadingFragment.getLeftToDownload() == 0) {
						downloading = false;
						
						downloadingFragment = null;
					} 

				}

			} 

			try {
				BufferOccWriter.write(simTime + " " + buffer + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (fragments.size() == 30 && buffer == 0) {
				break;
			}

			bufferHandler(simTime);

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

		if (hasCrashed || prev == -1) {
			previousQuality = -1;
		} else {

			previousQuality = fragments.get(fragments.size() - 1).getQuality();

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

	private void readTraceFile(String fileName) {
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
		int bit = (bytes * 8) / 1000;

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


	private void bufferHandler(int simTime) {

		if (buffer >= MINBUF) {
			hasCrashed = false;
		}

		if (buffer == 0) {
			hasCrashed = true;
			prev = -1;
		} else if (!hasCrashed) {

			if (frame < fragments.size() && !fragments.isEmpty()) {
				playVideo(simTime);
			}
		}

		if ((buffer <= MINBUF) || (buffer > MINBUF && buffer < MAXBUF)) {
			canDownload = true;
		}

		if (buffer == MAXBUF) {
			canDownload = false;
		}


	}

	private void playVideo(int simTime) {

		Fragment nowPlayed;
		
		do {
			
			nowPlayed = fragments.get(frame);
			
			
			if(nowPlayed.isDone()) {
				frame++;
			}
			
		}while(nowPlayed.getSecondsPlayed() > 4);
			
		
		try {

			if (nowPlayed.getLeftToDownload() == 0) {
				
				buffer--;
				nowPlayed.playFrame();
				
				StreamNrWriter.write(simTime + " " + nowPlayed.getQuality() + "\n");

			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	

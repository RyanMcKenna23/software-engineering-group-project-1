import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
 
public class ChronoTimer { //main program, links everything together

	//Queues of Racers
	Queue<Racer> racerQueue1 = new LinkedList<Racer>(); //beginning1
	Queue<Racer> racerQueue2 = new LinkedList<Racer>(); //beginning2
	Queue<Racer> racerRun1 = new LinkedList<Racer>(); //running1
	Queue<Racer> racerRun2 = new LinkedList<Racer>(); //running2
	Queue<Racer> racerFinish1 = new LinkedList<Racer>(); //done w/o numbers
	Queue<Racer> racerFinish2 = new LinkedList<Racer>(); //done w/ numbers

	ArrayList<String> systemLog = new ArrayList<String>(); //stores one run at a time

	ArrayList<Run> runList = new ArrayList<Run>(); //list of previous runs
	private int runNum = 1; //number of the current run

	
	private int queueSize = 0;// Is used in trigger to keep track of the size of racerQueue1
	public boolean AlreadyStarted = false; // Keeps track of whether or not race has started, for PARGRP
	
	private boolean power = false;
	Time t = new Time(); //time instance to do functions

	private boolean[][] enabled = new boolean[2][4]; //array holding enable for each channel
	public Sensor[][] connected = new Sensor[2][4]; //array holding connected for each sensor	
	private boolean runStarted = false; //a run must be created before almost everything else

	private int eventType = 0; //0 is not set, 1 is IND, 2 is PARIND, 3 is ending run, 4 is GRP, 5 is PARGRP
	private int queueNum = 1; //keeps track of which beginning queue to add new racer to

	private int placeHoldNum = 1; //keeps track of placeholderNum for GRP race finishes
	public double groupStart = 0.0; //stores start time for GRP races
	
	private int hours = 0; //hours, minutes, and seconds of setTime
	private int minutes = 0;
	private double seconds = 0.0;

	public static void main(String args[]) throws IOException{ //runs the ChronoTimer
		Simulator sim = new Simulator();
		ChronoTimer t = new ChronoTimer();
		Server server = new Server(t.racerFinish1, t.racerFinish2, t.runList);

		String command; //input from simulator

		do{
			command = sim.getInput();
			if(command == "" || command == "GUI"){
				break;
			}
			t.sendCommand(command);
		}while(true);
		
		if(command.equals("GUI")){
			GUI frame = new GUI(t);
			frame.setVisible(true);
			GUI_Back back=new GUI_Back(t);
			back.setVisible(true);
		}
		server.startServer();
	}
	
	public int getEventType(){ //returns eventType //used for testing
		return eventType;
	}
	 
	public boolean getEnabled(int i, int j){ //returns enabled at a given index //used for testing
		return enabled[i][j];
	}

	public int getRunNum(){ //used for testing
		return runNum;
	}
	 
	public Sensor getConnected(int i, int j){ //returns sensor at a given index //used for testing
		return connected[i][j];
	}

	public boolean getRunStart(){ //used for testing
		return runStarted;
	}
	
	public void sendCommand(String command) throws IOException{ //receives commands from Simulator
		//1
		if(command.contains("POWER")){
			power();
			boolean b = isPowerOn();
			if(b) System.out.println("Power On.");
			else System.out.println("Power Off.");
		}
		//2
		else if(command.contains("RESET")){
			boolean b = reset();
			if(b) System.out.println("Reset.");
			else System.out.println("Try Again - NOT Reset.");
		}
		//3
		else if(command.contains("TIME")){
			String[] time = command.split(" ");
			String[] splitTime = time[1].split(":");
			if(splitTime[0] == null || splitTime[1] == null || splitTime[2] == null){
				return;
			}
			int _hours = Integer.parseInt(splitTime[0]);
			int _minutes = Integer.parseInt(splitTime[1]);
			double _seconds = Double.parseDouble(splitTime[2]);
			boolean b = setTime(_hours, _minutes,_seconds);
			if(b) System.out.println("Time has been set to " + _hours + ":" + _minutes + ":" + _seconds + ".");
			else System.out.println("Try Again - Time has not been set.");
		}
		//4
		else if(command.contains("EVENT")){
			String[] event = command.split(" ");
			if(event[1] == null){
				return;
			}
			boolean b = setEventType(event[1]);
			if(b) System.out.println("Event Type has been set to " + event[1]);
			else System.out.println("Try Again - Event Type not set.");
		}
		//5
		else if(command.contains("NEWRUN")){
			boolean b = newRun();
			if(b) System.out.println("New Run has been started.");
			else System.out.println("Try Again - New Run NOT started.");
		}
		//6
		else if(command.contains("NUM")){
			String[] num = command.split(" ");
			if(num[1] == null){
				return;
			}
			int i = Integer.parseInt(num[1]);
			boolean b = addRacer(i);
			if(b)System.out.println("Runner Number " + i + " Added.");
			else System.out.println("Try Again - Runner Number " + i + " Not Added.");
		}
		//7
		else if(command.contains("TOG")){
			command = command.substring(command.length()-1, command.length());
			if(command == null){
				return;
			}
			int channel = Integer.parseInt(command);
			boolean b = togChannel(channel);
			if(b) System.out.println("Channel Number " + channel + " has been toggled.");
			else System.out.println("Try Again - Channel Number " + channel + " has NOT been toggled.");
		}
		//8	
		else if(command.contains("TRIG")){
			command = command.substring(command.length()-1, command.length());
			if(command == null){
				return;
			}
			int channel = Integer.parseInt(command);
			boolean b = trigChannel(channel);
			if(b == true) System.out.println("Triggering Channel " + channel + " was successful!");
			else System.out.println("Try Again - Triggering Channel " + channel + " was not successful.");
		}
		//9
		else if(command.contains("START")){
			boolean b = start();
			if(b) System.out.println("Started on Channel 1.");
			else System.out.println("Try Again - Channel 1 Not Started.");
		}
		//10
		else if(command.contains("FINISH")){
			boolean b = finish();
			if(b) System.out.println("Finished on Channel 2.");
			else System.out.println("Try Again - Channel 2 Not Finished.");
		}
		//11
		else if(command.contains("DNF")){
			boolean b = dnfRacer();
			if(b) System.out.println("Racer did not finish.");
			else System.out.println("Racer not ended.");
		}
		//12
		else if(command.contains("CANCEL")){
			boolean b = cancelRacer();
			if(b) System.out.println("Racer has been cancelled.");
			else System.out.println("Racer has NOT been cancelled.");
		}
		//13
		else if(command.contains("SWAP")){
			boolean b = swap();
			if(b) System.out.println("Swap was successful.");
			else System.out.println("Swap was not successful.");
		}
		//14
		else if(command.contains("CONN")){
			String[] conn = command.split(" ");
			if(conn[1] == null){
				return;
			}
			int runnerNumEx = Integer.parseInt(conn[1]);
			boolean b = connectSensor(runnerNumEx);
			if(b) System.out.println("Connecting Sensor was successful.");
			else System.out.println("Connecting Sensor was not successful.");
		}
		//15
		else if(command.contains("DISC")){
			String[] disc = command.split(" ");
			if(disc[1] == null){
				return;
			}
			int runnerNumEx = Integer.parseInt(disc[1]);
			boolean b = disconnectSensor(runnerNumEx);
			if(b) System.out.println("Disconnecting Sensor was successful.");
			else System.out.println("Disconnecting Sensor was not successful.");	
		}
		//16
		else if(command.contains("GROUP")){
			String[] runNum = command.split(" ");
			if(runNum[1] == null){
				return;
			}
			int runnerNumEx = Integer.parseInt(runNum[1]);
			boolean b = setGroupRacerNum(runnerNumEx);
			if(b) System.out.println("Setting Group Racer Number was successful.");
			else System.out.println("Setting Group Racer Number was not successful.");
		}
		//17
		else if(command.contains("ENDRUN")){
			boolean b = endRun();
			if(b) System.out.println("Run has been ended.");
			else System.out.println("Run has NOT been ended.");
		}
		//18
		else if(command.contains("PRINT")){
			String[] runNumArray = command.split(" ");
			if(runNumArray[1] == null){
				return;
			}
			int runNum = Integer.parseInt(runNumArray[1]);
			System.out.println("Printing Run " + runNum + ".");
			boolean b = print(runNum);
			if(b) System.out.println("Printing Run was successful.");
			else System.out.println("Printing Run was not successful.");
		}
		//19
		else if(command.contains("EXPORT")){
			String[] runNum = command.split(" ");
			if(runNum[1] == null){
				return;
			}
			int runnerNumEx = Integer.parseInt(runNum[1]);
			System.out.println("Exporting Run " + runnerNumEx + ".");
			boolean b = export(runnerNumEx);
			if(b) System.out.println("Exporting Run was successful.");
			else System.out.println("Exporting Run was not successful.");
		}
		//20
		else if(command.contains("EXIT")){
			System.out.println("Exiting Program.");
			exit();
		}  
		System.out.println(""); //returns nothing if no legal command
	}

	public boolean isPowerOn(){ //returns power boolean
		return power;
	}

	public void power(){ //if(on) -> turn off, stay in simulator //else if(off) -> turn on
		if(power){
			power = false;
			systemLog.add(t.getSystemTime() + " Power Turned Off.");
		}
		else{
			power = true;
			systemLog.add(t.getSystemTime() + " Power Turned On.");
		}
	}

	public void exit(){	//"quits program" //exit simulator
		if(isPowerOn()) {
			System.out.println("Try Again - Power must be 'Off'.");
			systemLog.add(t.getSystemTime() + " Exit Unsuccessful.");
			return;
		}
		systemLog.add(t.getSystemTime() + " System Exited.");
		System.exit(0);
	}

	public boolean reset(){ //sets variables to initial values
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Reset Unsuccessful.");
			return false;
		}
		racerQueue1 = new LinkedList<Racer>();
		racerQueue2 = new LinkedList<Racer>();
		racerRun1 = new LinkedList<Racer>();
		racerRun2 = new LinkedList<Racer>();
		racerFinish1 = new LinkedList<Racer>();
		racerFinish2 = new LinkedList<Racer>();

		systemLog = new ArrayList<String>();

		enabled = new boolean[2][4];
		connected = new Sensor[2][4];
		runStarted = false;
		
		AlreadyStarted = false;

		hours = 0;
		minutes = 0;
		seconds = 0.0;

		eventType = 0;
		queueNum = 1;
		
		systemLog.add(t.getSystemTime() + " Reset Successful.");
		return true;
	}
	
	public boolean setTime(int hrs, int min, double sec){ //allows user to set time
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Setting Time Unsuccessful.");
			return false;
		}

		hours = hrs;
		minutes = min;
		seconds = sec;
		systemLog.add(t.getSystemTime() + " Setting Time Successful.");
		return true;
	}

	public boolean setEventType(String s){ //sets IND, PARIND, GRP, PARGRP
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Setting EventType Unsuccessful.");
			return false;
		}
		if(s.equalsIgnoreCase("IND")){
			eventType = 1;
		}
		else if(s.equalsIgnoreCase("PARIND")){
			eventType = 2;
		}
		else if(s.equalsIgnoreCase("GRP")){
			eventType = 4;
		}
		else if(s.equalsIgnoreCase("PARGRP")){ //PARGRP
			eventType = 5;
		}
		systemLog.add(t.getSystemTime() + " Setting EventType Successful.");
		return true;
	}

	public boolean newRun(){ //creates a new run
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " NewRun Unsuccessful.");
			return false;
		}
		if(eventType == 0){
			System.out.println("Try Again - Event Type must be set.");
			systemLog.add(t.getSystemTime() + " NewRun Unsuccessful.");
			return false;
		}
		if(runStarted == true){
			System.out.println("Try Again - Only One Run can be going at a time.");
			systemLog.add(t.getSystemTime() + " NewRun Unsuccessful.");
			return false;
		}
		if(runStarted != true){
			runStarted = true;
			if(eventType == 1){
				systemLog.add(t.getSystemTime() + " New IND Run Started.");
			}
			else if(eventType == 2){
				systemLog.add(t.getSystemTime() + " New PARIND Run Started.");
			}
			else if(eventType == 4){
				systemLog.add(t.getSystemTime() + " New GRP Run Started.");
			}
			else if(eventType == 5){
				systemLog.add(t.getSystemTime() + " New PARGRP Run Started."); //PARGRP
			}
			systemLog.add(t.getSystemTime() + " NewRun Successful.");
		}

		return true;
	}

	public boolean endRun(){ //ends a run
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " EndRun Unsuccessful.");
			return false;
		}
		if(eventType == 0){
			System.out.println("Try Again - Event Type must be set.");
			systemLog.add(t.getSystemTime() + " EndRun Unsuccessful.");
			return false;
		}
		if(runStarted == false){
			System.out.println("Try Again - A Run has not been started.");
			systemLog.add(t.getSystemTime() + " EndRun Unsuccessful.");
			return false;
		}

		systemLog.add(t.getSystemTime() + " Run Ended.");
		eventType = 3;
		dnfRacer();
		Run r = new Run(runNum, systemLog, racerFinish1, racerFinish2);
		runList.add(r);
		runNum++;
		reset();
		systemLog.add(t.getSystemTime() + " EndRun Successful.");
		return true;
	}

	public boolean dnfRacer(){ //sets end time of next racer to finish to DNF, not return to queue
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " DNF Unsuccessful.");
			return false;
		}
		if(eventType == 0){
			System.out.println("Try Again - Event Type must be set.");
			systemLog.add(t.getSystemTime() + " DNF Unsuccessful.");
			return false;
		}
		if(runStarted == false){
			System.out.println("Try Again - A Run has not been started.");
			systemLog.add(t.getSystemTime() + " DNF Unsuccessful.");
			return false;
		}

		if(eventType == 1){ //IND 
			if(racerRun1.isEmpty()){
				System.out.println("Try Again - There are no Racers in the Queue.");
				systemLog.add(t.getSystemTime() + " DNF Unsuccessful - No Racers.");
				return false;
			}
			Racer r = racerRun1.remove();
			r.setEnd(-1);
			r.setState(2);
			systemLog.add(t.getSystemTime() + " Racer " + r.getNum() + " did not finish.");
			racerFinish2.add(r);
		}
		else if(eventType == 2){ //PARIND
			systemLog.add(t.getSystemTime() + " dnfRacer cannot be called on PARIND runs.");
			System.out.println("dnfRacer cannot be called on PARIND runs.");
			return false;
		}
		else if(eventType == 3){ //used when ending run, empties both run queues
			while(!racerRun1.isEmpty()){
				Racer r = racerRun1.remove();
				r.setEnd(-1);
				r.setState(2);
				systemLog.add(t.getSystemTime() + " Racer " + r.getNum() + " did not finish.");
				racerFinish2.add(r);
			}
			while(!racerRun2.isEmpty()){
				Racer r = racerRun2.remove();
				r.setEnd(-1);
				r.setState(2);
				systemLog.add(t.getSystemTime() + " Racer " + r.getNum() + " did not finish.");
				racerFinish2.add(r);
			}
		}
		else if(eventType == 4){ //GRP
			Racer r = new Racer(placeHoldNum, groupStart, -1, "DNF", 2);
			placeHoldNum++;
			systemLog.add(t.getSystemTime() + " Racer " + r.getNum() + " did not finish.");
			racerFinish1.add(r);
		}
		else if(eventType == 5){  //PARGRP
			systemLog.add(t.getSystemTime() + " dnfRacer cannot be called on PARGRP runs.");
			System.out.println("dnfRacer cannot be called on PARGRP runs.");
			return false;
		}
		return true;
	}

	public boolean cancelRacer(){ //discard race for 1st racer & put back in queue as next to start
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Cancel Unsuccessful.");
			return false;
		}
		if(eventType == 0){
			System.out.println("Try Again - Event Type must be set.");
			systemLog.add(t.getSystemTime() + " Cancel Unsuccessful.");
			return false;
		}
		if(runStarted == false){
			System.out.println("Try Again - A Run has not been started.");
			systemLog.add(t.getSystemTime() + " Cancel Unsuccessful.");
			return false;
		}

		if(eventType == 1){ //IND
			if(racerRun1.isEmpty()){
				System.out.println("Try Again - There are no Racers in the Queue.");
				systemLog.add(t.getSystemTime() + " Cancel Unsuccessful - No Racers.");
				return false;
			}
			Queue<Racer> newQueue = new LinkedList<Racer>();
			Racer r = racerRun1.remove();
			r.setStart(0.0);
			r.setState(0);
			systemLog.add(t.getSystemTime() + " Racer " + r.getNum() + " has been canceled");
			newQueue.add(r);
			while(!racerQueue1.isEmpty()){
				Racer r1 = racerQueue1.remove();
				newQueue.add(r1);
			}
			racerQueue1 = newQueue;
		}
		else if(eventType == 2){ //PARIND
			systemLog.add(t.getSystemTime() + " cancelRacer cannot be called on PARIND runs.");
			System.out.println("cancelRacer cannot be called on PARIND runs.");
			return false;
		}
		else if(eventType == 4){ //GRP
			systemLog.add(t.getSystemTime() + " cancelRacer cannot be called on GRP runs.");
			System.out.println("cancelRacer cannot be called on GRP runs.");
			return false;
		}
		else if(eventType == 5){  //PARGRP
			systemLog.add(t.getSystemTime() + " cancelRacer cannot be called on PARGRP runs.");
			System.out.println("cancelRacer cannot be called on PARGRP runs.");
			return false;
		}
		return true;
	}

	public boolean togChannel(int channelNum){ //enable or disable the channel
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Toggle Unsuccessful.");
			return false;
		}
		if(eventType == 0){
			System.out.println("Try Again - Event Type must be set.");
			systemLog.add(t.getSystemTime() + " Toggle Unsuccessful.");
			return false;
		}
		if(runStarted == false){
			System.out.println("Try Again - A Run has not been started.");
			systemLog.add(t.getSystemTime() + " Toggle Unsuccessful.");
			return false;
		}

		if(eventType == 1 || eventType == 2 || eventType == 4 || eventType == 5){ //same thing for IND & PARIND & GRP & PARGRP
			if(channelNum % 2 != 0){ //odd
				boolean enable = enabled[0][channelNum/2];

				if(enable == false){ //odd & disabled
					enabled[0][channelNum/2] = true;
					systemLog.add(t.getSystemTime() + " Start Channel Num " + channelNum + " has been enabled.");
				}
				else if(enable == true){ //odd & enabled
					enabled[0][channelNum/2] = false;
					systemLog.add(t.getSystemTime() + " Start Channel Num " + channelNum + " has been disabled.");
				}
			}
			else if(channelNum % 2 == 0){ //even
				boolean enable1 = enabled[1][(channelNum/2)-1];

				if(enable1 == false){ //even & disabled
					enabled[1][(channelNum/2)-1] = true;
					systemLog.add(t.getSystemTime() + " End Channel Num " + channelNum + " has been enabled.");
				}

				else if(enable1 == true){ //even & enabled
					enabled[1][(channelNum/2)-1] = false;
					systemLog.add(t.getSystemTime() + " End Channel Num " + channelNum + " has been disabled.");
				}
			}//end else if
		}//end outside if
		return true;
	}//end method

	public boolean trigChannel(int channelNum){ //trigger the channel num & pulls racer from queue
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Trigger Unsuccessful.");
			return false;
		}
		if(eventType == 0){
			System.out.println("Try Again - Event Type must be set.");
			systemLog.add(t.getSystemTime() + " Trigger Unsuccessful.");
			return false;
		}
		if(runStarted == false){
			System.out.println("Try Again - A Run has not been started.");
			systemLog.add(t.getSystemTime() + " Trigger Unsuccessful.");
			return false;
		}

		//IND
		if(eventType == 1){ 
			if(channelNum != 1 && channelNum != 2){
				systemLog.add(t.getSystemTime() + " IND Races only use channels 1 & 2.");
				System.out.println("IND Races only use channels 1 & 2.");
				return false;
			}
			if(channelNum == 1){ //start
				if(racerQueue1.isEmpty()){
					System.out.println("No Racers in the Queue"); 
					systemLog.add(t.getSystemTime() + " No Racers in the Queue to start race.");
					return false;
				}
				if(enabled[0][0]){
					Racer r = racerQueue1.remove();
					double start = t.start();
					r.setStart(start);
					r.setState(1);
					racerRun1.add(r);
					systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " started racing.");
					return true;
				}
				systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
				return false;
			}
			else if(channelNum == 2){ //end
				if(enabled[1][0]){
					if(racerRun1.isEmpty()){
						System.out.println("Try Again - There are no Racers in the Queue.");
						systemLog.add(t.getSystemTime() + " Trigger Unsuccessful - No Racers.");
						return false;
					}
					Racer r1 = racerRun1.remove();
					double end = t.end();
					r1.setEnd(end);
					r1.setElapsed(r1.getStart(), end);
					r1.setState(2);
					racerFinish2.add(r1);
					systemLog.add(t.getSystemTime() + " Racer Num " + r1.getNum() + " finished racing.");
					return true;
				}
				systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
				return false;
			}
		}//end IND

		//PARIND
		else if(eventType == 2){ 
			if(channelNum != 1 && channelNum != 2 && channelNum != 3 && channelNum != 4){
				systemLog.add(t.getSystemTime() + " PARIND Races only use channels 1, 2, 3 and 4.");
				System.out.println("PARIND Races only use channels 1, 2, 3 and 4.");
				return false;
			}
			if(channelNum % 2 != 0){ //odd, start
				if(racerQueue1.isEmpty() && racerQueue2.isEmpty()){
					System.out.println("No Racers in either Queue."); 
					systemLog.add(t.getSystemTime() + " No Racers in either Queue to start race.");
					return false;
				}
				if(enabled[0][channelNum/2]){//enabled
					if(channelNum == 1){
						if(racerQueue1.isEmpty()){
							return false;
						}
						Racer r = racerQueue1.remove();
						double start = t.start();
						r.setStart(start);
						r.setState(1);
						racerRun1.add(r);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " started racing.");
						return true;
					}
					else if(channelNum == 3){
						if(racerQueue2.isEmpty()){
							return false;
						}
						Racer r = racerQueue2.remove();
						double start = t.start();
						r.setStart(start);
						r.setState(1);
						racerRun2.add(r);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " started racing.");
						return true;
					}					
				}//end enabled if
				systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
				return false;
			}//end odd channel
			else if(channelNum % 2 == 0){ //even, end
				if(enabled[1][(channelNum/2)-1]){ //enabled
					if(channelNum == 2){
						if(racerRun1.isEmpty()){
							return false;
						}
						Racer r1 = racerRun1.remove();
						double end = t.end();
						r1.setEnd(end);
						r1.setState(2);
						r1.setElapsed(r1.getStart(), end);
						racerFinish2.add(r1);
						systemLog.add(t.getSystemTime() + " Racer Num " + r1.getNum() + " finished racing.");
						return true;
					}
					else if(channelNum == 4){
						if(racerRun2.isEmpty()){
							return false;
						}
						Racer r1 = racerRun2.remove();
						double end = t.end();
						r1.setEnd(end);
						r1.setState(2);
						r1.setElapsed(r1.getStart(), end);
						racerFinish2.add(r1);
						systemLog.add(t.getSystemTime() + " Racer Num " + r1.getNum() + " finished racing.");
						return true;
					}
				}//end enabled if
				systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
				return false;
			}//end even channel
		}//end PARIND

		//GRP
		else if(eventType == 4){
			if(channelNum != 1 && channelNum != 2){
				systemLog.add(t.getSystemTime() + " GRP Races only use channels 1 & 2.");
				System.out.println("GRP Races only use channels 1 & 2.");
				return false;
			}
			if(channelNum == 1){ //start
				if(enabled[0][0]){
					groupStart = t.start();
					systemLog.add(t.getSystemTime() + " Group of Racers " + " started racing.");
					return true;
				}
				systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
				return false;
			}
			else if(channelNum == 2){ //end
				if(enabled[1][0]){
					double end = t.end();
					Racer r = new Racer(placeHoldNum, groupStart, end,"", 2);
					placeHoldNum++;
					r.setElapsed(groupStart, end);
					systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
					racerFinish1.add(r);
					return true;
				}
				systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
				return false;
			}
		}//end GRP
		else if(eventType == 5){
			if(racerRun1.size() <= 8) // makes sure you dont add more than 8 racers
			{
				if(channelNum == 1 && !AlreadyStarted){ //start
					if(enabled[0][0]){
						groupStart = t.start();
						queueSize = racerQueue1.size();
						for(int i = 1; i < queueSize + 1; i ++)
						{
							Racer RacerSwitch = racerQueue1.remove();
							RacerSwitch.setState(1);
							RacerSwitch.setStart(groupStart);
							racerRun1.add(RacerSwitch);
						}
						systemLog.add(t.getSystemTime() + " Group of Parellel Racers " + " started racing.");
						AlreadyStarted = true;
						return true;
					}
					systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
					return false;
				}
				else if(channelNum != 1 || channelNum != 2 || channelNum != 3 || channelNum != 4 || channelNum != 5 || channelNum != 6 ||channelNum != 7 || channelNum != 8 )
				{
					return false;
				}
				else if(channelNum == 1 && AlreadyStarted && queueSize >= 1){ 
					if(enabled[0][0]){
						double end = t.end();
						Racer r = racerQueue1.remove();
						r.setState(2);
						r.setElapsed(groupStart, end);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
						racerFinish1.add(r);
						return true;
					}
					
					systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
					return false;
				}
				
				
				else if(channelNum == 2 && AlreadyStarted && queueSize >= 2){
					if(enabled[1][0]){
						double end = t.end();
						Racer r = racerQueue1.remove();
						r.setState(2);
						r.setElapsed(groupStart, end);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
						racerFinish1.add(r);
						return true;
					}
					
					systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
					return false;
				}
				
				else if(channelNum == 3 && AlreadyStarted && queueSize >= 3){
					if(enabled[0][1]){
						double end = t.end();
						Racer r = racerQueue1.remove();
						r.setState(2);
						r.setElapsed(groupStart, end);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
						racerFinish1.add(r);
						return true;
					}
					
					systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
					return false;
				}
				
				
				else if(channelNum == 4 && AlreadyStarted && queueSize >= 4){
					if(enabled[1][1]){
						double end = t.end();
						Racer r = racerQueue1.remove();
						r.setState(2);
						r.setElapsed(groupStart, end);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
						racerFinish1.add(r);
						return true;
					}
					
					systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
					return false;
				}
				
				
				else if(channelNum == 5 && AlreadyStarted && queueSize >= 5){
					if(enabled[0][2]){
						double end = t.end();
						Racer r = racerQueue1.remove();
						r.setState(2);
						r.setElapsed(groupStart, end);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
						racerFinish1.add(r);
						return true;
					}
					
					systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
					return false;
				}
				
				
				else if(channelNum == 6 && AlreadyStarted && queueSize >= 6){
					if(enabled[1][2]){
						double end = t.end();
						Racer r = racerQueue1.remove();
						r.setState(2);
						r.setElapsed(groupStart, end);
						systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
						racerFinish1.add(r);
						return true;
					}
					
					
					else if(channelNum == 7 && AlreadyStarted && queueSize >= 7){
						if(enabled[0][3]){
							double end = t.end();
							Racer r = racerQueue1.remove();
							r.setState(2);
							r.setElapsed(groupStart, end);
							systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
							racerFinish1.add(r);
							return true;
						}
						
						systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
						return false;
					}
					
					else if(channelNum == 8 && AlreadyStarted && queueSize >= 8){
						if(enabled[1][3]){
							double end = t.end();
							Racer r = racerQueue1.remove();
							r.setState(2);
							r.setElapsed(groupStart, end);
							systemLog.add(t.getSystemTime() + " Racer Num " + r.getNum() + " finished racing.");
							racerFinish1.add(r);
							return true;
						}
						
						systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
						return false;
					}
					else if(channelNum < 9 && channelNum > 1 && !AlreadyStarted)
					{
						return false;
					}
					
					
					systemLog.add(t.getSystemTime() + " Channel " + channelNum + " is not Enabled.");
					return false;
				}
			}
		}		
		
		systemLog.add(t.getSystemTime() + " Event Type " + eventType + " is not valid.");
		return false;
	}

	public boolean start(){ //triggers channel 1
		return trigChannel(1);
	}

	public boolean finish(){//triggers channel 2
		return trigChannel(2);
	}

	public boolean addRacer(int racerNum){ //same as num //adds racer to queue
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Adding Racer Unsuccessful.");
			return false;
		}
		if(eventType == 0){
			System.out.println("Try Again - Event Type must be set.");
			systemLog.add(t.getSystemTime() + " Adding Racer Unsuccessful.");
			return false;
		}
		if(runStarted == false){
			System.out.println("Try Again - A Run has not been started.");
			systemLog.add(t.getSystemTime() + " Adding Racer Unsuccessful.");
			return false;
		}

		Racer r = new Racer(racerNum, 0.0, 0.0, "0", 0);
		if(eventType == 1){ //IND, just add
			racerQueue1.add(r);
			systemLog.add(t.getSystemTime() + " Racer Num " + racerNum + " has been added to IND event.");
		}
		else if(eventType == 2){ //PARIND add to one queue, then the other, etc.
			if(queueNum == 1){
				racerQueue1.add(r);
				queueNum = 2;
				systemLog.add(t.getSystemTime() + " Racer Num " + racerNum + " has been added to PARIND event.");
			}
			else if(queueNum == 2){
				racerQueue2.add(r);
				queueNum = 1;
				systemLog.add(t.getSystemTime() + " Racer Num " + racerNum + " has been added to PARIND event.");
			}
		}
		else if(eventType == 4){ //GRP, shouldn't add like this
			systemLog.add(t.getSystemTime() + " Racer Num " + racerNum + " cannot be added to GRP event in this way.");
			System.out.println("Racers cannot be added to GRP event in this way");
			return false;
		}
		else if(eventType == 5){
			racerQueue1.add(r);
			systemLog.add(t.getSystemTime() + " Racer Num " + racerNum + " has been added to PARGRP event.");
		}
		return true;
	}

	public boolean print(int rNum){ //prints a run to the console
		if(rNum > runList.size()){
			System.out.println("Try Again - " + rNum + " is not a valid run number.");
			systemLog.add(t.getSystemTime() + " " + rNum + " is not a valid run number.");
			return false;
		}
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Print Unsuccessful.");
			return false;
		}
		if(eventType != 0 || runStarted == true){
			System.out.println("Try Again - Run must be ended before print.");
			systemLog.add(t.getSystemTime() + " Print Unsuccessful.");
			return false;
		}
		else{
			Run r = runList.get(rNum-1);
			r.print();
		}
		systemLog.add(t.getSystemTime() + " Printing Successful.");
		return true;
	}
	
	public ArrayList<String> printGUI(){ //prints the last run to the GUI print box
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " GUIPrint Unsuccessful.");
			return null;
		}
		if(eventType != 0 || runStarted == true){
			System.out.println("Try Again - Run must be ended before print.");
			systemLog.add(t.getSystemTime() + " GUIPrint Unsuccessful.");
			return null;
		}
		else{	
			Run r = runList.get(runNum-2);
			systemLog.add(t.getSystemTime() + " GUIPrint Successful.");
			return r.guiPrint();
		}
	}

	public boolean export(int rNum){ //exports run to a file
		if(rNum > runList.size()){
			System.out.println("Try Again - " + rNum + " is not a valid run number.");
			systemLog.add(t.getSystemTime() + " " + rNum + " is not a valid run number.");
			return false;
		}
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Export Unsuccessful.");
			return false;
		}
		if(eventType != 0 || runStarted == true){
			System.out.println("Try Again - Run must be ended before export.");
			systemLog.add(t.getSystemTime() + " Export Unsuccessful.");
			return false;
		}
		else{
			Run r = runList.get(rNum-1);
			try {
				r.export();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		systemLog.add(t.getSystemTime() + " Export Successful.");
		return true;
	}

	public boolean setGroupRacerNum(int racerNum){ //ability to set group racer num after in queue1
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " SetGroupRacerNum Unsuccessful.");
			return false;
		}
		if(eventType != 4){
			System.out.println("Try Again - Event Type must be Group.");
			systemLog.add(t.getSystemTime() + " SetGroupRacerNum Unsuccessful.");
			return false;
		}
		if(racerFinish1.isEmpty()){
			System.out.println("Try Again - No Racers have finish w/o Numbers.");
			systemLog.add(t.getSystemTime() + " SetGroupRacerNum Unsuccessful.");
			return false;
		}
		Racer r = racerFinish1.remove();
		r.setNum(racerNum);
		racerFinish2.add(r);
		systemLog.add(t.getSystemTime() + " SetGroupRacerNum Successful.");
		return true;
	}

	public boolean connectSensor(int channelNum){ //connects sensor
		if(channelNum % 2 != 0){ //odd
			Sensor connect = connected[0][channelNum/2];
			
			if(connect == null){ //odd & not connected
				connected[0][channelNum/2] = new Sensor(channelNum);
				systemLog.add(t.getSystemTime() + " Sensor on " + channelNum + " has been connected.");
				return true;
			}
			else if(connect != null){ //odd & connected
				systemLog.add(t.getSystemTime() + " Sensor is already connected.");
				System.out.println("Try Again - Sensor is already connected.");
				return false;
			}
		}
		else if(channelNum % 2 == 0){ //even
			Sensor connect1 = connected[1][(channelNum/2)-1];
			
			if(connect1 == null){ //even & not connected
				connected[1][(channelNum/2)-1] = new Sensor(channelNum);
				systemLog.add(t.getSystemTime() + " Sensor on " + channelNum + " has been connected.");
				return true;
			}
			else if(connect1 != null){ //even & connected
				systemLog.add(t.getSystemTime() + " Sensor is already connected.");
				System.out.println("Try Again - Sensor is already connected.");
				return false;
			}
		}//end else if
		return false;
	}
	
	public boolean disconnectSensor(int channelNum){ //disconnects sensor
		if(channelNum % 2 != 0){ //odd
			Sensor connect = connected[0][channelNum/2];
			
			if(connect != null){ //odd & connected
				connected[0][channelNum/2] = null;
				systemLog.add(t.getSystemTime() + " Sensor on " + channelNum + " has been disconnected.");
				return true;
			}
			else if(connect == null){ //odd & not connected
				systemLog.add(t.getSystemTime() + " Sensor is not connected.");
				System.out.println("Try Again - Sensor is not connected.");
				return false;
			}
		}
		else if(channelNum % 2 == 0){ //even
			Sensor connect1 = connected[1][(channelNum/2)-1];
			
			if(connect1 != null){ //even & connected
				connected[1][(channelNum/2)-1] = null;
				systemLog.add(t.getSystemTime() + " Sensor on " + channelNum + " has been disconnected.");
			}
			
			else if(connect1 == null){ //even & not connected
				systemLog.add(t.getSystemTime() + " Sensor is not connected.");
				System.out.println("Try Again - Sensor is not connected.");
				return false;
			}
		}//end else if
		return false;
	}

	public boolean swap(){ //switches first two racers
		if(!isPowerOn()) {
			System.out.println("Try Again - Power must be 'On'.");
			systemLog.add(t.getSystemTime() + " Swap Unsuccessful.");
			return false;
		}
		
		if(runStarted == false){
			System.out.println("Try Again - A Run has not been started.");
			systemLog.add(t.getSystemTime() + " Swap Unsuccessful.");
			return false;
		}

		if(eventType == 1){ //check to see if it's an IND event
			if(racerRun1.size() < 2){ //refuses to execute if current runs >=2
				System.out.println("Cannot swap, <2 racers running"); //prints error message and returns 
				systemLog.add(t.getSystemTime() + " Swap Unsuccessful.");
				return false;
			}

			Queue<Racer> copy = new LinkedList<Racer>();
			copy.addAll(racerFinish1);

			int startSize = racerFinish1.size(); //initial size of list of finished competitors
			while(racerFinish1.size() == startSize+1){ //when the list of finished competitors is incremented
				while(racerFinish1.size() == startSize+2){ //when the list of finished racers increments by 2
					for(int i = -2; i < racerFinish1.size(); i++){//FOR loop removes all but the most recent 2 entries to the "finished" queue
						racerFinish1.remove();
					}
					Racer hold = racerFinish1.remove();
					copy.add(racerFinish1.remove());
					copy.add(hold);
					racerFinish1 = copy;
				}
			}
			return true;
		}
		else {
			System.out.println("Swap can only be called during IND events; wrong event type.");
			systemLog.add(t.getSystemTime() + " Swap Unsuccessful - Wrong Event Type.");
			return false;
		}
//		public boolean swap(){

//			Racer swap1 = racerRun1.remove();
//			Racer swap2 = racerRun1.remove();
//
//			Queue<Racer> swap = new LinkedList<Racer>();
//			swap.add(swap2);
//			swap.add(swap1);
//
//			while(!racerRun1.isEmpty()){
//				Racer r = racerRun1.remove();
//				swap.add(r);
//			}
//			racerRun1 = swap;
//			return true;
//
//		}

	}

	
	
}//end ChronoTimer

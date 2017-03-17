package sprint_2;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Simulator {
	String inputType;
	boolean inputloop = true;
	Scanner input = new Scanner(System.in);

	boolean fileNameLoop = true;
	String filename;
	File file;
	int linenumber = 0;
	ChronoTimer test = new ChronoTimer();

//	Create a simulator that issues events for the Chronotimer and has a system 
//	clock. Events should be generated by reading in text from the command file or the console.

	public String getInput(){
		while(inputloop){
			System.out.println("Do you want user input or input from a file? U:User Input and T:Text File");
			inputType = input.nextLine();
			if(inputType.equalsIgnoreCase("U") || inputType.equalsIgnoreCase("T")){
				inputloop = false;
			}
		}
		if(inputType.equalsIgnoreCase("U")){
			while(true){
				System.out.println("Enter a Command.");
				System.out.println("1. Power");
				System.out.println("2. Exit");
				System.out.println("3. Reset");
				System.out.println("4. Time");
				System.out.println("5. DNF");
				System.out.println("6. Cancel");
				System.out.println("7. Toggle");
				System.out.println("8. Trigger");
				System.out.println("9. Start");
				System.out.println("10. Finish");
				System.out.println("11. Num");
				System.out.println("12. Event Type");
				System.out.println("13. New Run");
				System.out.println("14. End Run");
				System.out.println("15. Print");
				System.out.println("16. Export");



				int menuChoice = input.nextInt();

				switch(menuChoice){
				case 1:
					return "Power";
				case 2:
					return "Exit";
				case 3:
					return "Reset";
				case 4: 
					System.out.println("What is the time? (minutes:seconds)");
					String time = input.nextLine();
					time = input.nextLine();
					return "Time " + time;
				case 5:
					return "DNF";
				case 6:
					return "Cancel";
				case 8:
					while(true){
						System.out.print("What channel do you want to trigger/enable?");
						int triggerInput = input.nextInt();
						if(triggerInput >= 1 && triggerInput <= 8){
							return "Trigger " + triggerInput;
						}
						else{
							System.out.println("Invalid Input");
						}
					}
				case 7:
					while(true){
						System.out.print("What channel do you want to toggle?");
						int toggleInput = input.nextInt();
						if(toggleInput >= 1 && toggleInput <= 8){
							return "Toggle " + toggleInput;
						}
						else{
							System.out.println("Invalid Input");
						}
					}
				case 9:
					return "Start";
				case 10:
					return "Finish";
				case 11:
					System.out.println("What is the number of the runner?");
					int runnerNum=input.nextInt();
					return "NUM "+runnerNum;
				case 12:
					while(true){
						System.out.println("Please Choose the event type");
						System.out.println("1. Individual Run");
						System.out.println("2. Parellel Run");
						int runChoice=input.nextInt();
						switch (runChoice){
						case 1:
							return "Event IND";
						case 2:
							return "Event PARIND";
						default:
							System.out.println("Invalid Input");
						}
					}
				case 13:
					return "Newrun";
				case 14:
					return "Endrun";
				case 15:
					System.out.println("Which run do you want to print out?");
					int runNum=input.nextInt();
					return "Print "+runNum;
				case 16:
					System.out.println("Which run do you want to print out?");
					int runNumExport=input.nextInt();
					return "Export "+runNumExport;
				default:
					System.out.println("Invalid Input");
					break;
				}
			}	
		}
		else { //input type = text file
			while(fileNameLoop){
				System.out.println("Please input the file name.");
				filename = input.nextLine();

				file = new File(filename);

				try(Scanner reader = new Scanner(new FileReader(file))){
					fileNameLoop = false;

					while(reader.hasNextLine()){
						test.sendCommand(reader.nextLine());
					}

				}catch(IOException e){
					System.out.println("Bad File");
				}
			}//end while

			return "";
		}
	}
}
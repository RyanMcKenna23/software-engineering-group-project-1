
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
			System.out.println("Do you want user input or input from a file or do you want to use a GUI? U:User Input,T:Text File, and G;GUI Input");
			inputType = input.nextLine();
			if(inputType.equalsIgnoreCase("U") || inputType.equalsIgnoreCase("T")||inputType.equalsIgnoreCase("G")){
				inputloop = false;
			}
		}
		if(inputType.equalsIgnoreCase("U")){
			while(true){
				System.out.println("Enter a Command.");
				System.out.println("1. Power");
				System.out.println("2. Reset");
				System.out.println("3. Time");
				System.out.println("4. Event Type");
				System.out.println("5. New Run");
				System.out.println("6. Add Runner (NUM)");
				System.out.println("7. Toggle");
				System.out.println("8. Trigger");
				System.out.println("9. Start");
				System.out.println("10. Finish");
				System.out.println("11. DNF");
				System.out.println("12. Cancel");
				System.out.println("13. End Run");
				System.out.println("14. Print");
				System.out.println("15. Export");
				System.out.println("16. Exit");

				int menuChoice = input.nextInt();

				switch(menuChoice){
				case 1:
					return "POWER";
				case 2:
					return "RESET";
				case 3:
					System.out.println("What is the time? (hours:minutes:seconds)");
					String time = input.nextLine();
					time = input.nextLine();
					return "TIME " + time;
				case 4: 
					while(true){
						System.out.println("Please Choose the Event Type:");
						System.out.println("1. Individual Run");
						System.out.println("2. Parallel Individual Run");
						int runChoice = input.nextInt();
						switch (runChoice){
						case 1:
							return "EVENT IND";
						case 2:
							return "EVENT PARIND";
						default:
							System.out.println("Invalid Input.");
						}
					}
				case 5:
					return "NEWRUN";
				case 6:
					System.out.println("What is the number of the runner you want to add?");
					int runnerNum = input.nextInt();
					return "NUM " + runnerNum;
				case 7:
					while(true){
						System.out.print("What channel do you want to toggle?");
						int toggleInput = input.nextInt();
						if(toggleInput >= 1 && toggleInput <= 8){
							return "TOG " + toggleInput;
						}
						else{
							System.out.println("Invalid Input.");
						}
					}
				case 8:
					while(true){
						System.out.print("What channel do you want to trigger?");
						int triggerInput = input.nextInt();
						if(triggerInput >= 1 && triggerInput <= 8){
							return "TRIG " + triggerInput;
						}
						else{
							System.out.println("Invalid Input.");
						}
					}
				case 9:
					return "START";
				case 10:
					return "FINISH";
				case 11:
					return "DNF";
				case 12:
					return "CANCEL";
				case 13:
					return "ENDRUN";
				case 14:
					System.out.println("Which run do you want to print out?");
					int runNum = input.nextInt();
					return "PRINT " + runNum;
				case 15:
					System.out.println("Which run do you want to export?");
					int runNumExport = input.nextInt();
					return "EXPORT " + runNumExport;
				case 16:
					return "EXIT";
				default:
					System.out.println("Invalid Input.");
					break;
				}
			}	
		}
		else if(inputType.equalsIgnoreCase("T")){ //input type = text file
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
		else{
			return "GUI";
		}
	}
}
package sprint_1;
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
					String time=input.nextLine();
					time=input.nextLine();
					return "Time "+time;
				case 5:
					return "DNF";
				case 6:
					return "Cancel";
				case 7:
					while(true){
						System.out.print("What channel do you want to trigger?");
						int triggerInput = input.nextInt();
						if(triggerInput >= 1 && triggerInput <= 8){
							return "Trigger " + triggerInput;
						}
						else{
							System.out.println("Invalid Input");
						}
					}
				case 8:
					while(true){
						System.out.print("What channel do you want to toggle?");
						int toggleInput = input.nextInt();
						if(toggleInput >= 1 && toggleInput <= 8){
							return "Trigger " + toggleInput;
						}
						else{
							System.out.println("Invalid Input");
						}
					}
				case 9:
					return "Start";
				case 10:
					return "Finish";
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
				fileNameLoop = false;
				try{
					file = new File(filename);
					fileNameLoop = false;
				}catch(NullPointerException ex){
					System.out.println("Invalid file name"); 
				}
			}
			Scanner reader = null;
			try{
				reader = new Scanner(new FileReader(file));
			}catch(IOException e){}
			
			int templine = -1;
			while(templine < linenumber){
				reader.nextLine();
				templine++;
			}
			linenumber++;
			
			if(reader.hasNextLine()){
				return reader.nextLine();
			}
			else{
				return "";
			}
		}
	}
}
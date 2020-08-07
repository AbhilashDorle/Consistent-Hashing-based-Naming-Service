import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BootstrapUserInteraction extends Thread implements Serializable {

	Bootstrap bootstrap;
	public BootstrapUserInteraction(Bootstrap bootstrap) {
		// TODO Auto-generated constructor stub
		this.bootstrap = bootstrap;
	}
	
	@Override
	public void run() {
		
		String input = "";	
		Scanner s = new Scanner(System.in);
		do {
			System.out.print("DHT >");
			input = s.nextLine();
			String[] commandAndValue = input.split(" ");
			
			switch(commandAndValue[0]) {
			
			case "lookup":
				//System.out.println("In Lookup");
				try {
					System.out.println(bootstrap.lookup(Integer.parseInt(commandAndValue[1])));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			case "Insert":
				try {
					bootstrap.insert(Integer.parseInt(commandAndValue[1]), commandAndValue[2]);
				} catch (NumberFormatException | ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			
			case "delete":
				try {
					bootstrap.delete(Integer.parseInt(commandAndValue[1]));
				} catch (NumberFormatException | ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
				
			default:
				System.out.println("Bash: Wrong Command");
			
			}
			
			
		}while(true);
		
		
		
	}
	

}

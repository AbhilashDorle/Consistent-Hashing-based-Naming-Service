import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class NameServerMain implements Serializable  {

	static Socket socket = null;
	static ObjectOutputStream outputStream = null;
	static ObjectInputStream inputStream = null;
	static HashMap<Integer, String> data = new HashMap<>();
	static NSInfoHelperClass nsInfo = null;
	 static Socket fwdSocket = null;
	 
	 String lookup(int key,String serverTracker) throws IOException, ClassNotFoundException {
			
			if(data.containsKey(key))
				return (data.get(key));
			else if(key > this.nsInfo.id) {
			 try {
				fwdSocket = new Socket(nsInfo.getSuccessorIP(), nsInfo.successorPortListning);
			
			 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
			 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
			 outputStreamFwd.writeObject("lookup "+key);
			 outputStreamFwd.writeObject(serverTracker);
			 String value = (String) inputStreamFwd.readObject();
			 System.out.println("Got Value" + value);
			
			 String ServerTracker = (String) inputStreamFwd.readObject();
			 System.out.println("Server" + ServerTracker);
			 System.out.println("Checking in successor" + nsInfo.getSuccessorId());
			 fwdSocket.close();
			return value+" "+ServerTracker;
			 } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			 }
			}
			return "No key found";
			
		}
	  String insert(int key, String value) throws UnknownHostException, IOException, ClassNotFoundException {
			//check if the key should be in bootstrap
			//check if the key should be in bootstrap
			if(key < nsInfo.id) {
				System.out.println("Key inserted" + key);
				data.put(key,value);
				return ""+nsInfo.id;
			}
			
			else if(key > this.nsInfo.id) {
					//if no then contact successor
				 fwdSocket = new Socket(nsInfo.getSuccessorIP(), nsInfo.successorPortListning);
				ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
				ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
				outputStreamFwd.writeObject("Insert "+key+" "+value);
				outputStreamFwd.writeObject(nsInfo.id);
				value = (String) inputStreamFwd.readObject();
				fwdSocket.close();
				return value;
			}
			return null;
	}
	String delete(int key) throws IOException, ClassNotFoundException {
		
		//if key in bootstrap server then dekete
		if(key < nsInfo.id)
			if(data.containsKey(key)) {
				data.remove(key);
				return ""+nsInfo.id;
			}	
			else {
				System.out.println("NoKeyFound");
				return "NoKeyFound";
			}
				
		else if(key > this.nsInfo.id) {
				 fwdSocket = new Socket(nsInfo.getSuccessorIP(), nsInfo.successorPortListning);
				 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
				 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
				 outputStreamFwd.writeObject("delete "+key);
				 String value = (String) inputStreamFwd.readObject();
				 fwdSocket.close();
				 return value;
				//else check in successor
		}
		return null;
		
	}
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		List<String> configFile = Files.readAllLines(Paths.get(args[0]));
		NameServerMain nameServer = new NameServerMain();
		int id = Integer.parseInt(configFile.get(0));
		int listeningPort = Integer.parseInt(configFile.get(1));
		String serverIP = configFile.get(2).split(" ")[0];
		int serverPort = Integer.parseInt(configFile.get(2).split(" ")[1]);
		String input = "";
		String bootstrapIP = "";
		int bootstrapPort = 0;
		Scanner s = new Scanner(System.in);
		nameServer.nsInfo = new NSInfoHelperClass(id, listeningPort);
		do {
			System.out.print("NameServer434 >");
			input = s.nextLine();
			String[] commandAndValue = input.split(" ");
			
			switch(commandAndValue[0]) {
			
			case "enter":
				socket = new Socket(serverIP, serverPort);	
				outputStream = new  ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				String nameServerIP = Inet4Address.getLocalHost().getHostAddress();
				outputStream.writeObject("entry "+id + " "+ nameServerIP + " " + listeningPort);
				//ns will send its id, its ip and its listeningport where other server can contact it for key
				bootstrapIP = (String) inputStream.readObject();
				bootstrapPort = (int) inputStream.readObject();
				String serverTracker = (String) inputStream.readObject();
				int successorPortListning = (int) inputStream.readObject();
				int predessorPortListning = (int) inputStream.readObject();
				int successorId = (int) inputStream.readObject();
				int predessorId = (int) inputStream.readObject();
				String successorIP = (String) inputStream.readObject();	
				String predessorIP = (String) inputStream.readObject();
				
				nameServer.nsInfo.updateInformation(successorPortListning,predessorPortListning, successorId, predessorId, successorIP, predessorIP);
				nameServer.nsInfo.id = id;
				//System.out.println("SuccessorId : " + successorId +" PredessorId " +predessorId + "PredessorIP " + predessorIP+" PredessorPort : "+predessorPortListning);
				while(true) {
					
					int key =  (int) inputStream.readObject();
					if(key == -1)
						break;
					
					String value = (String) inputStream.readObject();
					nameServer.data.put(key, value);
				}
				outputStream.close();
				inputStream.close();
				socket.close();
				NameServerConnectionHandler thread = new NameServerConnectionHandler(nameServer);
				thread.start();
				System.out.println("Successful entry");
				System.out.println("Range of IDs managed ["+predessorId+","+id+"]");
				System.out.println("Servers Visited" + serverTracker);
			

			break;
			
			case "exit":
				
				//give all keys to successor and tell him to update his predessor
				socket = new Socket(nameServer.nsInfo.successorIP, nameServer.nsInfo.successorPortListning);
				outputStream = new  ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				
				outputStream.writeObject("updateYourPredessorAndTakeAllKeys");//To successor
				outputStream.writeObject(nameServer.nsInfo.predessorPortListning);//send predessor port
				outputStream.writeObject(nameServer.nsInfo.predessorId);//send predessor id
				outputStream.writeObject(nameServer.nsInfo.predessorIP);//send predessor Ip
				
				for(int key = nameServer.nsInfo.predessorId; key < nameServer.nsInfo.id; key++) {
					if(nameServer.data.containsKey(key)) {
						//System.out.println(key);
						outputStream.writeObject(key);
						outputStream.writeObject(nameServer.data.get(key));
						nameServer.data.remove(key);
						
					}
				}
				outputStream.writeObject(-1);
				outputStream.close();
				inputStream.close();
				socket.close();
				
				/////////////////////////**************************/////////////////////////////
				//tell predessor to update its successor
				socket = new Socket(nameServer.nsInfo.predessorIP, nameServer.nsInfo.predessorPortListning);
				outputStream = new  ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				
				outputStream.writeObject("updateYourSuccessor");//sned predessor the new successor
				outputStream.writeObject(nameServer.nsInfo.successorPortListning);//send successor port
				outputStream.writeObject(nameServer.nsInfo.successorId);//send successor id
				outputStream.writeObject(nameServer.nsInfo.successorIP);//send successor Ip
				
				outputStream.close();
				inputStream.close();
				socket.close();
				// tell bootstrap that i am exiting
				socket = new Socket(bootstrapIP, bootstrapPort);
				outputStream = new  ObjectOutputStream(socket.getOutputStream());
				outputStream.writeObject("updateMaxServerID");
				outputStream.writeObject(id);
				socket.close();
				
				
			break;
			
			default:
				
				System.out.println("Bash: Wrong input");
			
			
			
			}
			System.out.println("NameServer SuccessorId : "+nameServer.nsInfo.successorId + " PredessorId :"+nameServer.nsInfo.predessorId);
		
		}while(!input.equals("exit"));
		System.out.println("NameServer Exited");
	}

}

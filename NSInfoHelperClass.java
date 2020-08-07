import java.io.Serializable;

///////////////////BOOOTSTRAPP/////////////////////////////////////////////
public class NSInfoHelperClass implements Serializable {
	
	int predessorPortListning;
	int successorPortListning;
	int successorId;
	int predessorId;
	String predessorIP;
	String successorIP;
	int id;
	int serverPortForConnection;

	public NSInfoHelperClass(int id, int serverPortForConnection) {
		successorPortListning = 0;
		successorId = 0;
		this.predessorId = 0;
		this.id = id;
		this.serverPortForConnection = serverPortForConnection;
	}
	public void updateInformation(int successorPortListning, int predessorPortListning, int successorId, int predessorId, String successorIP, String predessorIP) {
		this.successorPortListning = successorPortListning;
		this.successorId = successorId;
		this.predessorId = predessorId;
		this.predessorIP = predessorIP;
		this.successorIP = successorIP;
		this.predessorPortListning = predessorPortListning;
	}
	public int getSuccessorId() {
		return this.successorId;
	}
	public int getPredessorId() {
		return this.predessorId;
	}
	public String getSuccessorIP() {
		return this.successorIP;
	}
	
	public void updateSuccessorInfo(int successorId, String successorIP) {		
		this.successorId = successorId;	
		this.successorIP = successorIP;
	}
	public void updatePredessorId(int predessorId, String predessorIP) {
		this.predessorId = predessorId;
		this.predessorIP = predessorIP;
	}
	
}

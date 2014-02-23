package jandc.intractobeacon;

public class RegionMessage {

	public String getUID() {
		return UID;
	}

	public void setUID(String uID) {
		UID = uID;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	protected String UID;
	private String message;

	public RegionMessage(String uniqueId, String message) {
		setUID(uniqueId);
		setMessage(message);
	}

}

package flyinpig.sync.service.structures;

public class DeviceInfo {

	String devInfo = null;
	
	public DeviceInfo(String device_details) {
		devInfo = device_details;
	}
	
	@Override
	public String toString()
	{
		return devInfo;		
	}

}

package info.fshi.lightweightdatamule.routing;

public class RadioStatus {

	public boolean isOn;
	public boolean isAuthorized;
	
	public RadioStatus(){
		isOn = false;
		isAuthorized = false;
	}
	
	public void setOnState(boolean isOn){
		this.isOn = isOn;
	}
	
	public void setAuthorizedState(boolean isAuthorized){
		this.isAuthorized = isAuthorized;
	}
	
}
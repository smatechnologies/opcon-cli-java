package com.smatechnologies.opcon.command.api.modules;

public class UtilDateFormat {

	static final long 	serialVersionUID = 05200000L;
	
	private String dfDateSeperator; 
	private int dfDatePositionOne;
	private int dfDatePositionTwo;
	private int dfDatePositionThree;
	private boolean dfFourDigitYear;
	private boolean dfTwoDigitDay;
	private boolean dfTwoDigitMonth;
	
    public UtilDateFormat() {
    	dfDateSeperator = null;
    	dfDatePositionOne = 99;
    	dfDatePositionTwo = 99;
    	dfDatePositionThree = 99;
    	dfFourDigitYear = true;
    	dfTwoDigitDay = true;
    	dfTwoDigitMonth = true;
    }
    
    public String getDfDateSeperator() { 
		return dfDateSeperator; 
	}
   
    public void setDfDateSeperator(String dfDateSeperator) {
		this.dfDateSeperator = dfDateSeperator;
	}
    
    public int getDfDatePositionOne() { 
		return dfDatePositionOne; 
	}
   
   public void setDfDatePositionOne(int dfDatePositionOne) {
		this.dfDatePositionOne = dfDatePositionOne;
	}
    
    public int getDfDatePositionTwo() { 
		return dfDatePositionTwo; 
	}
   
    public void setDfDatePositionTwo(int dfDatePositionTwo) {
		this.dfDatePositionTwo = dfDatePositionTwo;
	}

    public int getDfDatePositionThree() { 
		return dfDatePositionThree; 
	}
   
    public void setDfDatePositionThree(int dfDatePositionThree) {
		this.dfDatePositionThree = dfDatePositionThree;
	}

	public boolean isDfFourDigitYear() {
		return dfFourDigitYear;
	}

	public void setDfFourDigitYear(boolean dfFourDigitYear) {
		this.dfFourDigitYear = dfFourDigitYear;
	}

	public boolean isDfTwoDigitDay() {
		return dfTwoDigitDay;
	}

	public void setDfTwoDigitDay(boolean dfTwoDigitDay) {
		this.dfTwoDigitDay = dfTwoDigitDay;
	}

	public boolean isDfTwoDigitMonth() {
		return dfTwoDigitMonth;
	}

	public void setDfTwoDigitMonth(boolean dfTwoDigitMonth) {
		this.dfTwoDigitMonth = dfTwoDigitMonth;
	}

}


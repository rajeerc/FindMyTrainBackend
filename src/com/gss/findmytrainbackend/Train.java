package com.gss.findmytrainbackend;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Train {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String trainid;
	
	@Persistent
	private String start;
	
	@Persistent
	private String destination;
	
	@Persistent (mappedBy = "train")
	private List<Stop> stops = new ArrayList<Stop>();
	
	@Persistent
	private int count = 0;
	
	public Train (){
	}
	

	
	//adds the stops in which the train would stop
	public void addStop (String station, String time){

			Stop tempStop = new Stop();
			tempStop.setStation(station);
			tempStop.setTime(time);
			stops.add(tempStop);
			
	}

	/**
	 * @return the trainid
	 */
	public String getTrainid() {
		return trainid;
	}

	/**
	 * @param trainid the trainid to set
	 */
	public void setTrainid(String trainid) {
		this.trainid = trainid;
	}

	/**
	 * @return the start
	 */
	public String getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(String start) {
		this.start = start;
	}

	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @return the stops
	 */
	public List<Stop> getStops() {
		return stops;
	}

	
	
}

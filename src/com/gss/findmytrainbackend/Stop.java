package com.gss.findmytrainbackend;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Stop {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;
	
	//the station name
	@Persistent
	private String station;
	
	//the time at which the train arrives the station
	@Persistent
	private String time;
	
	@Persistent
	private Train train;
	
	
	/**
	 * @return the station
	 */
	public String getStation() {
		return station;
	}
	/**
	 * @param station the station to set
	 */
	public void setStation(String station) {
		this.station = station;
	}
	/**
	 * @return the time
	 */
	public String getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time = time;
	}
	/**
	 * @return the id
	 */
	public Key getId() {
		return id;
	}
	
}

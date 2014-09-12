package com.gss.findmytrainbackend;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Record {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	//station is saved as a string as no other information of a station is needed except its name
	@Persistent
	private String station;
	
	//time stamp at which the record is sent from the device to the server
	@Persistent
	private String timeStamp;
	
	
	 //status of a particular train w.r.t. the station selected
	 //		0 : Not left the station
	 // 	1 : Left the station
	@Persistent
	private int status;
	
	//the user can add any comment about the selected train
	@Persistent
	private String comment;
	
	@Persistent
	private String trainid;
	
	//to keep track of the user in case a rating system is to be implemented
	@Persistent
	private String userid;

	/**
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
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
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

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
	 * @return the timeStamp
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	
	
	
	
}

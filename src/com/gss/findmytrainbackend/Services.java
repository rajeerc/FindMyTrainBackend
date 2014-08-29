package com.gss.findmytrainbackend;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.appidentity.AppIdentityServicePb.GetDefaultGcsBucketNameRequest;
import com.gss.findmytrainbackend.PMF;

@Api(name = "findmytrain", namespace = @ApiNamespace(ownerDomain = "gss.com", ownerName = "gss.com", packagePath = "findmytrainbackend"))
public class Services {

	@ApiMethod(name = "listOfTrains", path = "findmytrain/v1/listTrains")
	public List<String> listTrains(@Named("station") String station) {
		// this method should return the list of trains based on the station
		// if a trains stops at the station 'station' that will be added to the
		// 'trains' list
		List<Train> trains = new ArrayList<Train>();
		List<Train> tempTrains = null;

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(Train.class);

		try {
			tempTrains = (List<Train>) q.execute();
			List<Stop>	tempStops = new ArrayList<Stop>();
			
			for (Train t : tempTrains){
				tempStops = t.getStops();
				
				for (Stop s: tempStops){
					if (s.getStation().equals(station)){
						trains.add(t);
						break;
					}
				}
			}
			
		} finally {
			q.closeAll();
		}
		
		//returning a string list instead of the train list
		
		List<String> result = new ArrayList<String>();
		
		for (Train t: trains){
			result.add("From " + t.getStart() + " To " + t.getDestination() + "\n" + t.getTrainid());
		}
		return result;
		
		//returning the list of train objects
		//return trains;
	}
}

package com.gss.findmytrainbackend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

@Api(name = "findmytrain", namespace = @ApiNamespace(ownerDomain = "gss.com", ownerName = "gss.com", packagePath = "findmytrainbackend"))
public class Services {

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "listOfTrains", path = "findmytrain/v1/listTrains")
	public List<String> listTrains(@Named("station") String station) {
		// This method should return the list of trains based on the station
		// if a trains stops at the station 'station' that will be added to the
		// 'trains' list
		List<Train> trains = new ArrayList<Train>();
		List<Train> tempTrains = null;

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(Train.class);

		try {
			tempTrains = (List<Train>) q.execute();
			List<Stop> tempStops = new ArrayList<Stop>();

			for (Train t : tempTrains) {
				tempStops = t.getStops();

				for (Stop s : tempStops) {
					if (s.getStation().equals(station)) {
						trains.add(t);
						break;
					}
				}
			}

		} finally {
			q.closeAll();
		}

		// returning a string list instead of the train list

		List<String> result = new ArrayList<String>();

		for (Train t : trains) {
			result.add("From " + t.getStart() + " To " + t.getDestination()
					+ "\n" + t.getTrainid());
		}
		return result;

		// returning the list of train objects
		// return trains;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "listOfStationsOnSelectedTrain", path = "findmytrain/v1/listStationsOnTrain")
	public List<String> listStationsOnTrain(@Named("trainId") String id) {
		// This method lists the set of stations based on the train id and
		// then cross check with the record values to see whether
		// the train has passed each station

		// at the moment, this method returns the status of a train based on the
		// count
		// returns the data in the following from as a lits
		// "stationName\nstatus"

		List<String> stationList = new ArrayList<String>();
		List<Train> trains = new ArrayList<Train>();
		List<Record> records = new ArrayList<Record>();
		List statuses0Count = new ArrayList();
		List statuses1Count = new ArrayList();
		int count0;
		int count1;
		List<String> result = new ArrayList<String>();
		final long FIVE_MINUTES = 1000*60*5;

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(Train.class);
		q.setFilter("trainid == argTrainId");
		q.declareParameters("String argTrainId");

		Query q2 = pm.newQuery(Record.class);
		q2.setFilter("trainid == argTrainId");
		q2.declareParameters("String argTrainId");

		try {
			trains = (List<Train>) q.execute(id);
			records = (List<Record>) q2.execute(id);
			List<Stop> stops = new ArrayList<Stop>();

			stops = trains.get(0).getStops();

			for (Stop s : stops) {
				stationList.add(s.getStation());
				count0 = 0;
				count1 = 0;

				
//				getting the status of a train on station basis
//				based on the count of records which says 
//				either the train has left the station or not
				
				for (Record r : records) {
					if (r.getStation().equals(s.getStation())) {
						if (r.getStatus() == 0)
							count0++;
						else if (r.getStatus() == 1)
							count1++;
					}
				}

				if (count0 > count1)
					result.add(s.getStation() + "\n0");
				else
					result.add(s.getStation() + "\n1");
			}

		} finally {
			q.closeAll();
		}

		// return stationList;
		return result;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "insertRecords", path = "findmytrain/v1/insertRecords")
	public void insertRecords( Record record) {
		// this method is used to add the timestamp of the server to the record object
		// and then the object is persisted
		// duplicate testing is not done here since there is no way of having
		// duplicate entries here

		Date date = new Date();
		String timeStamp = Long.toString(date.getTime());

		record.setTimeStamp(timeStamp);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(record);
		} finally {
			pm.close();
		}
	}
}

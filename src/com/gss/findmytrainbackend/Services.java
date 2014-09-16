package com.gss.findmytrainbackend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import Jama.*;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.search.query.ExpressionParser.index_return;

@Api(name = "findmytrain", namespace = @ApiNamespace(ownerDomain = "gss.com", ownerName = "gss.com", packagePath = "findmytrainbackend"))
public class Services {

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "listOfTrains", path = "findmytrain/v1/listTrains")
	public List<String> listTrains(@Named("station") String station)
			throws ParseException {
		// This method should return the list of trains based on the station
		// if a trains stops at the station 'station' that will be added to the
		// 'trains' list
		final long ONE_MINUTE_IN_MILLIS = 60000;
		final int TIME_GAP_IN_MINUTES = 30;
		List<Train> trains = new ArrayList<Train>();
		List<Train> tempTrains = null;

		Date atRequest = new Date();
		SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
		String serverTime = parser.format(atRequest);
		Date serverDate = parser.parse(serverTime);
		Date upperLimit = new Date(serverDate.getTime()
				+ (TIME_GAP_IN_MINUTES * ONE_MINUTE_IN_MILLIS));
		Date lowerLimit = new Date(serverDate.getTime()
				- (TIME_GAP_IN_MINUTES * ONE_MINUTE_IN_MILLIS));

		// System.out.println("Server date : " + serverDate);
		// System.out.println("Server time : " + serverTime);

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query q = pm.newQuery(Train.class);

		try {
			tempTrains = (List<Train>) q.execute();
			List<Stop> tempStops = new ArrayList<Stop>();

			for (Train t : tempTrains) {
				tempStops = t.getStops();

				for (Stop s : tempStops) {
					// checking if a selected train stops at the selected
					// station
					if (s.getStation().equals(station)) {

						Date stationTime = parser.parse(s.getTime());

						// checking whether the station time is in between the
						// required time gap
						if (stationTime.after(lowerLimit)
								&& stationTime.before(upperLimit)) {
							trains.add(t);
							break;
						}
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
	@ApiMethod(name = "checkTrainAndStationStatus", path = "findmytrain/v1/checkStatus")
	public List<String> checkTrainAndStationStatus(
			@Named("Train id") String id,
			@Named("Currnet Station") String station) {

		// this method returns the list of stations for a selected train and
		// whether that train has passed each station

		final int STATION_GAP = 5; // five station from the current station to
									// both directions are considered for the
									// calculation
		final int RECORD_THRESHOLD = 10;
		final double PROBABILITY_THRESHOLD_LEFT = 0.65;
		final double PROBABILITY_THRESHOLD_NOT_LEFT = 0.35;
		final double TRANSITION_THRESHOLD = 0.2;
		final int RECORD_CONSIDERATION_LIMIT_NUMBER = 50;
		final int RECORD_CONSIDERATION_LIMIT_MINUTES = 30; // this value can be
															// reduced when the
															// number of users
															// become
															// higher.(since
															// there will be
															// more data to
															// analyze with in
															// the given time
															// period)
		final long ONE_MINUTE_IN_MILLIS = 60000;

		List<String> result = new ArrayList<String>();
		List<Train> trains = new ArrayList<Train>();
		List<Record> records = new ArrayList<Record>();

		PersistenceManager pm = PMF.get().getPersistenceManager();

		Query queryTrain = pm.newQuery(Train.class);
		queryTrain.setFilter("trainid == argTrainId");
		queryTrain.declareParameters("String argTrainId");

		Query queryRecord = pm.newQuery(Record.class);
		queryTrain.setFilter("trainid == argTrainId");
		queryTrain.declareParameters("String argTrainId");

		trains = (List<Train>) queryTrain.execute(id);
		records = (List<Record>) queryRecord.execute(id);
		List<Stop> stops = new ArrayList<Stop>();
		List<Record> tempRecords;
		int stationIndex = 0;
		int initialOneCount;
		int initialZeroCount;
		double probabilityInitial;
		double probabilityTransition;
		int[] centralDifference;
		int transitionCount;

		Date atRequest = new Date();
		SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
		String serverTime = parser.format(atRequest);
		Date serverDate = null;
		Date lowerLimit = null;
		try {
			serverDate = parser.parse(serverTime);
			lowerLimit = new Date(
					serverDate.getTime()
							- (RECORD_CONSIDERATION_LIMIT_MINUTES * ONE_MINUTE_IN_MILLIS));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		stops = trains.get(0).getStops();

		for (int i = 0; i < stops.size(); i++) {
			if (stops.get(i).getStation().equals(station))
				stationIndex = i;
		}

		int loopStart = stationIndex >= STATION_GAP ? stationIndex
				- STATION_GAP : 0;
		int loopEnd = stationIndex + STATION_GAP < stops.size() ? stationIndex
				+ STATION_GAP : stops.size();

		for (int i = loopStart; i < loopEnd; i++) {

			Stop tempStop = stops.get(i);
			String tempStatus = "not sure"; // for the situation where there are
											// not enough data present
			tempRecords = new ArrayList<Record>(); // these are the records used
													// to process the data for
													// each selected station

			initialOneCount = 0;
			initialZeroCount = 0;

			for (Record r : records) {
				if (r.getStation().equals(tempStop.getStation())) {
					tempRecords.add(r);
				}
			}

			// case where there is no data
			if (tempRecords.size() < RECORD_THRESHOLD) {
				result.add(tempStop.getStation() + ":" + tempStatus);
				continue;
			}

			// when there is enough data
			sortRecords(tempRecords);

			// selecting a set of record to perform the analysis based on their
			// recentness and the number of records
			int k = 0;
			for (int j = 0; j < tempRecords.size(); j++) {
				Date tempDate = new Date(tempRecords.get(j).getTimeStamp());
				String tempTime = parser.format(tempDate);
				try {
					Date recordDate = parser.parse(tempTime);
					if (recordDate.after(lowerLimit)) {
						k = j;
						break;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			if (tempRecords.size() - k < RECORD_CONSIDERATION_LIMIT_NUMBER) {
				if (tempRecords.size() < RECORD_CONSIDERATION_LIMIT_NUMBER) {
					k = 0;
				} else
					k = tempRecords.size() - RECORD_CONSIDERATION_LIMIT_NUMBER;
			}

			for (int j = k; j < tempRecords.size(); j++) {
				// counting the initial ones and zeros
				System.out.print(j + " ");
				if (tempRecords.get(j).getStatus() == 1)
					initialOneCount++;
				else
					initialZeroCount++;
			}
			System.out.println();

			probabilityInitial = (double) initialOneCount
					/ (tempRecords.size());

			if (probabilityInitial > PROBABILITY_THRESHOLD_LEFT) {
				tempStatus = "left";
			} else if (probabilityInitial < PROBABILITY_THRESHOLD_NOT_LEFT) {
				tempStatus = "not left";
			} else {
				// using the first central difference formula

				centralDifference = new int[tempRecords.size() - 1];

				for (int j = 1; j < tempRecords.size(); j++) {
					centralDifference[j - 1] = tempRecords.get(j).getStatus()
							- tempRecords.get(j - 1).getStatus();
				}

				transitionCount = 0;

				for (int j : centralDifference) {
					if (j == 1)
						transitionCount++;
				}

				probabilityTransition = (double) transitionCount
						/ centralDifference.length;

				if (probabilityTransition < TRANSITION_THRESHOLD) {
					tempStatus = "left";
				} else {
					tempStatus = "not sure";
				}

			}
			// at this point data can be further checked to refine the results
			result.add(tempStop.getStation() + ":" + tempStatus);
		}

		return result;
	}

	
	/*
	
	@SuppressWarnings("unchecked")
	@ApiMethod(name = "listOfStationsOnSelectedTrain", path = "findmytrain/v1/listStationsOnTrain")
	public List<String> listStationsOnTrain(@Named("trainId") String id) {
		// This method lists the set of stations based on the train id and
		// then cross check with the record values to see whether
		// the train has passed each station

		// at the moment, this method returns the status of a train based on the
		// count returns the data in the following from as a lists
		// "stationName\nstatus"

		List<String> stationList = new ArrayList<String>();
		List<Train> trains = new ArrayList<Train>();
		List<Record> records = new ArrayList<Record>();
		List statuses0Count = new ArrayList();
		List statuses1Count = new ArrayList();
		List<String> result = new ArrayList<String>();
		final int NUMBER_OF_GAPS = 3;
		final int GAP_TIME_IN_MINUTES = 2;
		final long TIME_GAP = 1000 * 60 * GAP_TIME_IN_MINUTES; // here the time
																// gap is set to
																// two minutes
		Date now = new Date();
		int index;
		int[][] count = new int[2][NUMBER_OF_GAPS]; // store the status of the
													// train in three time gaps
		double[] probabilities = new double[NUMBER_OF_GAPS];
		double a, b, c;
		double[][] matrixArray = new double[NUMBER_OF_GAPS][NUMBER_OF_GAPS];
		Matrix matrixA;
		Matrix inverse;
		Matrix matrixB;
		Matrix answer;
		double[] centralDifference;
		int zeroCount;
		int fiveCount;
		int fourCount;
		int oneCount;
		String status;
		int initialZeroCount, initialOneCount;
		double probability;
		int centralDifferenceSum;

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
			List<Record> tempRecords;

			stops = trains.get(0).getStops();

			for (Stop s : stops) {
				stationList.add(s.getStation());

				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < NUMBER_OF_GAPS; j++) {
						count[i][j] = 0;
					}
				}

				// ********************************************************************* //
				// getting the status of a train on station basis
				// based on the count of records which says
				// either the train has left the station or not
				// ********************************************************************* //

				// for (Record r : records) { if
				// (r.getStation().equals(s.getStation())) {
				//
				// index = (int) ((now.getTime()-
				// Long.parseLong(r.getTimeStamp()))/TIME_GAP);
				//
				// // index gets values when the record timestamp // 0 : is
				// within two minutes // 1 : is between two minutes and four
				// minutes // 2 : is between four minutes and six minutes
				//
				// count[r.getStatus()][index]++;
				//
				// } }
				//
				// for (int i=0; i<NUMBER_OF_GAPS; i++){ probabilities[i] =
				// (double)count[1][i] / (count[1][i] + count[0][i]); }
				//
				// //curve fitting part for (int i=0; i<NUMBER_OF_GAPS; i++){
				// double x = GAP_TIME_IN_MINUTES / 2;
				// matrixArray[i][NUMBER_OF_GAPS-1] = 1; for (int
				// j=NUMBER_OF_GAPS-2; j>=0; j--){ matrixArray[i][j] =
				// matrixArray[i][j+1] * x; } }
				//
				// matrixA = new Matrix(matrixArray); inverse =
				// matrixA.inverse(); matrixB = new Matrix(NUMBER_OF_GAPS, 1);
				// for (int i=0; i<NUMBER_OF_GAPS; i++) matrixB.set(i, 0,
				// probabilities[i]);
				//
				// answer = inverse.times(matrixB);

				// ********************************************************************* //
				// trying to do the predicting with the new method
				// using first central difference formula
				// first attempt
				// ********************************************************************* //

				// tempRecords = new ArrayList<Record>();
				// for (Record r : records) {
				// if (r.getStation().equals(s.getStation())) {
				// tempRecords.add(r);
				// }
				// }
				// status = "not sure";
				// if (tempRecords.size() == 0) {
				// result.add(s.getStation() + "\n" + status);
				// continue;
				// }
				//
				// sortRecords(tempRecords);
				//
				// for (Record r : tempRecords)
				// System.out.print(r.getStatus());System.out.println();
				//
				// centralDifference = new int[tempRecords.size() - 1];
				//
				// for (int i = 1; i < tempRecords.size(); i++) {
				// centralDifference[i - 1] = tempRecords.get(i).getStatus()
				// - tempRecords.get(i - 1).getStatus();
				// }
				//
				// for (int i = 0; i < centralDifference.length - 2; i++) {
				// if ((centralDifference[i] == 1
				// && centralDifference[i + 1] == 0 && centralDifference[i + 2]
				// == -1)
				// || (centralDifference[i] == -1
				// && centralDifference[i + 1] == 0 && centralDifference[i + 2]
				// == 1)) {
				// centralDifference[i] = 4;
				// centralDifference[i + 1] = 4;
				// centralDifference[i + 2] = 4;
				// }
				// }
				//
				// for (int i = 0; i < centralDifference.length - 1; i++) {
				// if ((centralDifference[i] == 1 && centralDifference[i + 1] ==
				// -1)
				// || (centralDifference[i] == -1 && centralDifference[i + 1] ==
				// 1)) {
				// centralDifference[i] = 5;
				// centralDifference[i + 1] = 5;
				// }
				// }
				//
				// zeroCount = 0;
				// oneCount = 0;
				// fourCount = 0;
				// fiveCount = 0;
				//
				// // length-1 is taken to neglect errors in the final data
				//
				// for (int i = 0; i < centralDifference.length; i++) {
				// System.out.print(centralDifference[i]);
				// switch (centralDifference[i]) {
				// case 0:
				// zeroCount++;
				// break;
				// case 1:
				// case -1:
				// oneCount += i;
				// break;
				// case 4:
				// fourCount++;
				// break;
				// case 5:
				// fiveCount++;
				// break;
				// }
				// }
				// System.out.println();
				//
				// if ((zeroCount + fourCount > fiveCount) && oneCount == 0) {
				//
				// status = "not left";
				// } else if (fourCount + fiveCount >= zeroCount) {
				// status = "may be";
				// } else if ((zeroCount > fourCount + fiveCount) && oneCount !=
				// 0) {
				// status = "left";
				// }
				// ********************************************************************* //
				// this is the part based on the simple count of 1's and 0' in
				// the records
				// ********************************************************************* //

				// status = "not sure";
				// tempRecords = new ArrayList<Record>();
				// for (Record r : records) {
				// if (r.getStation().equals(s.getStation())) {
				// tempRecords.add(r);
				// }
				// }
				// int count1=0;
				// int count0=0;
				//
				// for (Record r: tempRecords){
				// if (r.getStatus() == 1){
				// count1++;
				// }
				// else {
				// count0++;
				// }
				// }
				//
				// double percentage = (double)count1 / (count1 + count0);
				//
				// if (percentage > 0.65){
				// status = "left";
				// }
				// else if (percentage < 0.35){
				// status = "not left";
				// }
				// else if (percentage <= 0.65 && percentage >= 0.35){
				// status = "may be";
				// }
				//
				// result.add(s.getStation() + ":" + status);

				// ********************************************************************* //
				// trying to do the predicting with the new method
				// using first central difference formula
				// second attempt
				// ********************************************************************* //

				initialOneCount = 0;
				initialZeroCount = 0;
				status = "not sure"; // in case there is not enough data / no
										// data present to do the analysis
				tempRecords = new ArrayList<Record>();
				for (Record r : records) {
					if (r.getStation().equals(s.getStation())) {
						tempRecords.add(r);

						// counting the initial ones and zeros
						if (r.getStatus() == 1)
							initialOneCount++;
						else
							initialZeroCount++;
					}
				}

				// case where there is no data
				if (tempRecords.size() == 0) {
					result.add(s.getStation() + ":" + status);
					continue;
				}
				// probability = (double) initialOneCount
				// / (initialOneCount + initialZeroCount);
				//
				// if (probability >= 0.35 && probability <= 0.65) {
				// // situation where the 1's and 0's are distributed even
				// // approximately
				// status = "may be";
				// } else {
				sortRecords(tempRecords);

				centralDifference = new double[tempRecords.size() - 1];

				// calculating the second central difference
				for (int i = 1; i < tempRecords.size() - 1; i++) {
					centralDifference[i - 1] = tempRecords.get(i + 1)
							.getStatus() - tempRecords.get(i - 1).getStatus();
					centralDifference[i - 1] /= 2;
				}

				// centralDifferenceSum = 0;
				//
				// for (int i : centralDifference) {
				// centralDifferenceSum += i;
				// }

				// switch (centralDifferenceSum) {
				// case 1: {
				// if (initialOneCount > initialZeroCount)
				// status = "left";
				// else
				// status = "not left";
				// break;
				// }
				// case -1: {
				// if (intial)
				//
				// break;
				// }
				//
				// }

				// }

				zeroCount = 0;
				oneCount = 0;
				fourCount = 0;
				fiveCount = 0;

				// length-1 is taken to neglect errors in the final data

				System.out.println(s.getStation());
				for (int i = 0; i < centralDifference.length; i++) {
					System.out.println(centralDifference[i]);
				}
				System.out.println();

				// if ((zeroCount + fourCount > fiveCount) && oneCount == 0) {
				//
				// status = "not left";
				// } else if (fourCount + fiveCount >= zeroCount) {
				// status = "may be";
				// } else if ((zeroCount > fourCount + fiveCount) && oneCount !=
				// 0) {
				// status = "left";
				// }
			}

		} finally {
			q.closeAll();
		}
		for (String s : result)
			System.out.println(s);

		// return stationList;
		return result;
	}
	
	*/

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "insertRecords", path = "findmytrain/v1/insertRecords")
	public void insertRecords(Record record) {
		// this method is used to add the timestamp of the server to the record
		// object
		// and then the object is persisted
		// duplicate testing is not done here since there is no way of having
		// duplicate entries here

		// changed code: record timestamp from string to long
		// Date date = new Date();
		// String timeStamp = Long.toString(date.getTime());

		record.setTimeStamp(new Date().getTime());

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(record);
		} finally {
			pm.close();
		}
	}

	// private method to sort the records based on their timestamp
	// only used in the listStationsOnTrain() method
	private void sortRecords(List<Record> records) {
		for (int i = records.size() - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				if (records.get(j).getTimeStamp() > records.get(j + 1)
						.getTimeStamp()) {
					Record temp = records.get(j);
					records.set(j, records.get(j + 1));
					records.set(j + 1, temp);
				}
			}
		}
	}
}

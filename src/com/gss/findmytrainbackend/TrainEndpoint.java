package com.gss.findmytrainbackend;

import com.gss.findmytrainbackend.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "findmytrain", namespace = @ApiNamespace(ownerDomain = "gss.com", ownerName = "gss.com", packagePath = "findmytrainbackend"))
public class TrainEndpoint {

	/**
	 * This method lists all the entities inserted in datastore. It uses HTTP
	 * GET method and paging support.
	 * 
	 * @return A CollectionResponse class containing the list of all entities
	 *         persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listTrain")
	public CollectionResponse<Train> listTrain(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Train> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Train.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Train>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and
			// accomodate
			// for lazy fetch.
			for (Train obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Train> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET
	 * method.
	 * 
	 * @param id
	 *            the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getTrain")
	public Train getTrain(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		Train train = null;
		try {
			train = mgr.getObjectById(Train.class, id);
		} finally {
			mgr.close();
		}
		return train;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity
	 * already exists in the datastore, an exception is thrown. It uses HTTP
	 * POST method.
	 * 
	 * @param train
	 *            the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertTrain")
	public Train insertTrain(Train train) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (containsTrain(train)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(train);
		} finally {
			mgr.close();
		}
		return train;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does
	 * not exist in the datastore, an exception is thrown. It uses HTTP PUT
	 * method.
	 * 
	 * @param train
	 *            the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateTrain")
	public Train updateTrain(Train train) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsTrain(train)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(train);
		} finally {
			mgr.close();
		}
		return train;
	}

	/**
	 * This method removes the entity with primary key id. It uses HTTP DELETE
	 * method.
	 * 
	 * @param id
	 *            the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeTrain")
	public void removeTrain(@Named("id") String id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Train train = mgr.getObjectById(Train.class, id);
			mgr.deletePersistent(train);
		} finally {
			mgr.close();
		}
	}

	private boolean containsTrain(Train train) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Train.class, train.getTrainid());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}

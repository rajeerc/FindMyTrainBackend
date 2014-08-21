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
public class StopEndpoint {

	/**
	 * This method lists all the entities inserted in datastore. It uses HTTP
	 * GET method and paging support.
	 * 
	 * @return A CollectionResponse class containing the list of all entities
	 *         persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listStop")
	public CollectionResponse<Stop> listStop(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Stop> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Stop.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Stop>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and
			// accomodate
			// for lazy fetch.
			for (Stop obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Stop> builder().setItems(execute)
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
	@ApiMethod(name = "getStop")
	public Stop getStop(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Stop stop = null;
		try {
			stop = mgr.getObjectById(Stop.class, id);
		} finally {
			mgr.close();
		}
		return stop;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity
	 * already exists in the datastore, an exception is thrown. It uses HTTP
	 * POST method.
	 * 
	 * @param stop
	 *            the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertStop")
	public Stop insertStop(Stop stop) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (stop.getId() != null) {
				if (containsStop(stop)) {
					throw new EntityExistsException("Object already exists");
				}
			}
			mgr.makePersistent(stop);
		} finally {
			mgr.close();
		}
		return stop;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does
	 * not exist in the datastore, an exception is thrown. It uses HTTP PUT
	 * method.
	 * 
	 * @param stop
	 *            the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateStop")
	public Stop updateStop(Stop stop) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsStop(stop)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(stop);
		} finally {
			mgr.close();
		}
		return stop;
	}

	/**
	 * This method removes the entity with primary key id. It uses HTTP DELETE
	 * method.
	 * 
	 * @param id
	 *            the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeStop")
	public void removeStop(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Stop stop = mgr.getObjectById(Stop.class, id);
			mgr.deletePersistent(stop);
		} finally {
			mgr.close();
		}
	}

	private boolean containsStop(Stop stop) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Stop.class, stop.getId());
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

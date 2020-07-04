package io.antmedia.conferenceapp.datastore;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.antmedia.conferenceapp.types.Room;


public class ConferenceStore {

	private DB db;
	private BTreeMap<String, String> map;
	
	private Gson gson;
	protected static Logger logger = LoggerFactory.getLogger(ConferenceStore.class);
	private static final String MAP_NAME = "CONFERENCE";
	public static final String BEAN_NAME = "datastore";
	
	public ConferenceStore(String dbName) {
		db = DBMaker
				.fileDB(dbName)
				.fileMmapEnableIfSupported()
				.transactionEnable()
				.make();

		map = db.treeMap(MAP_NAME).keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).counterEnable()
				.createOrOpen();
		
		GsonBuilder builder = new GsonBuilder();
		gson = builder.create();

	}

	public boolean saveRoom(Room room) {
		String roomId = room.getId();
		synchronized (this) {
			if (room != null) {
				try {
					map.put(roomId, gson.toJson(room));
					db.commit();
				} catch (Exception e) {
					logger.error(ExceptionUtils.getStackTrace(e));
					roomId = null;
					return false;
				}
			}
		}

		return true;
	}

	public Room getRoom(String id) {
		synchronized (this) {
			if (id != null) {
				String jsonString = map.get(id);
				if (jsonString != null) {
					return gson.fromJson(jsonString, Room.class);
				}
			}
		}
		return null;
	}

	public List<Room> getRooms() {
		ArrayList<Room> rooms = new ArrayList<>();
		for(String id : map.getKeys()) {
			rooms.add(gson.fromJson(map.get(id), Room.class));
		}
		return rooms;
	}
}
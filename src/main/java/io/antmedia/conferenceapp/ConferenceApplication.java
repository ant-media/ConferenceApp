package io.antmedia.conferenceapp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.antmedia.conferenceapp.types.Participant;
import io.antmedia.conferenceapp.types.Room;

public class ConferenceApplication extends MultiThreadedApplicationAdapter {

	public static final String BEAN_NAME = "web.handler";
	protected static Logger logger = LoggerFactory.getLogger(ConferenceApplication.class);
		
	private ConcurrentHashMap<Room, List<Participant>> roomMap = new ConcurrentHashMap<>();

	public Map<Room, List<Participant>> getRoomMap() {
		return roomMap;
	}

	public void setRoomMap(ConcurrentHashMap<Room, List<Participant>> roomMap) {
		this.roomMap = roomMap;
	}
	
	public Room getRoom(String roomId) {
		for (Room room : roomMap.keySet()) {
			if(room.getId().equals(roomId)) {
				return room;
			}
		}
		return null;
	}
}

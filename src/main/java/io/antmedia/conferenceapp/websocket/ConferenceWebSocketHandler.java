package io.antmedia.conferenceapp.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.webrtc.Logging;
import org.webrtc.Logging.Severity;

import io.antmedia.conferenceapp.ConferenceApplication;
import io.antmedia.conferenceapp.datastore.ConferenceStore;
import io.antmedia.conferenceapp.settings.ConferenceSettings;
import io.antmedia.conferenceapp.settings.ConferenceSettingsManager;
import io.antmedia.conferenceapp.types.Participant;
import io.antmedia.conferenceapp.types.Room;

@ServerEndpoint(value="/wsconference", configurator=DefaultServerEndpointConfigurator.class)
public class ConferenceWebSocketHandler {

	private JSONParser jsonParser = new JSONParser();

	protected static Logger logger = LoggerFactory.getLogger(ConferenceWebSocketHandler.class);

	private ConferenceApplication application;

	private Room currentRoom;

	private Participant currentParticipant;

	private ConferenceSettings settings;

	private ConferenceStore datastore;

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		logger.info("Web Socket opened");

		try {
			ApplicationContextFacade servletContext = (ApplicationContextFacade) FieldUtils.readField(session.getContainer(), "servletContext", true);
			ConfigurableWebApplicationContext ctxt = (ConfigurableWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			
			application = (ConferenceApplication) ctxt.getBean(ConferenceApplication.BEAN_NAME);
			settings = (ConferenceSettings) ctxt.getBean(ConferenceSettings.BEAN_NAME);
			datastore = (ConferenceStore) ctxt.getBean(ConferenceStore.BEAN_NAME);

		} catch (Exception e) {
			logger.error("Application context can not be set to WebSocket handler");
			logger.error(ExceptionUtils.getMessage(e));
		} 
	}


	@OnClose
	public void onClose(Session session) {
		processLeave();
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
	}

	@OnMessage
	public void onMessage(Session session, String message) {
		if (message == null) {
			logger.error("Received message null for session id: {}" , session.getId());
			return;
		}

		try {
			logger.debug("Received message: {} session id: {}" , message, session.getId());

			JSONObject jsonObject = (JSONObject) jsonParser.parse(message);

			String cmd = (String) jsonObject.get(ConferenceWSConstants.COMMAND);

			if (cmd == null) {
				logger.error("Received message does not contain any command for session id: {}, message:{}" , session.getId(), message);
				return;
			}

			if (cmd.equals(ConferenceWSConstants.JOIN_ROOM_COMMAND)) 
			{	
				String roomId = (String) jsonObject.get(ConferenceWSConstants.ROOM);
				processJoin(session, roomId);
			}	
			else if (cmd.equals(ConferenceWSConstants.LEAVE_THE_ROOM)) 
			{
				processLeave();
			}
			else 
			{
				logger.info("Unprocessed message: {} ", message);
			}
		} catch (ParseException e) {
			logger.info("Received message: {} session id: {}" , message, session.getId());
			logger.error(ExceptionUtils.getStackTrace(e));

		}
	}

	private void processJoin(Session session, String roomId) {
		if(!isJoinable(roomId)) {
			return;
		}
		
		Map<Room, List<Participant>> roomMap = application.getRoomMap();
		currentRoom = application.getRoom(roomId);
		
		List<Participant> participants;
		if(currentRoom == null) {
			currentRoom = new Room(roomId);
			participants = new ArrayList<>();
			currentRoom.setZombi(true);
			datastore.saveRoom(currentRoom);
		}
		else {
			participants = roomMap.get(currentRoom);
		}

		String streamId = "Stream_"+System.currentTimeMillis();
		currentParticipant = new Participant(streamId, session);
		participants.add(currentParticipant);

		roomMap.put(currentRoom, participants);

		sendJoinedMessage(session, streamId);
		
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//send room info to participants
		ArrayList<String> streamIds = new ArrayList<>();
		for (Participant participant : participants) {
			streamIds.add(participant.streamId);
		}

		for (Participant participant : participants) {
			sendRoomInfoCommand(roomId, streamIds, participant.session);
		}
	}


	private boolean isJoinable(String roomId) {
		Room room = datastore.getRoom(roomId);
		
		boolean isAccepted = !settings.isAcceptOnlyRoomsInDataStore() ||
				(settings.isAcceptOnlyRoomsInDataStore() && room != null);
		
		long now = System.currentTimeMillis();
		boolean isValid = room == null || (room.getFinishTime() > now && now > room.getStartTime());
		
		return isAccepted && isValid;
	}


	private void sendJoinedMessage(Session session, String streamId) {
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put(ConferenceWSConstants.COMMAND, ConferenceWSConstants.JOINED_THE_ROOM);
		jsonResponse.put(ConferenceWSConstants.STREAM_ID, streamId);

		sendMessage(jsonResponse.toJSONString(), session);
	}
	
	private void processLeave() {
		Map<Room, List<Participant>> roomMap = application.getRoomMap();
		List<Participant> participants = roomMap.get(currentRoom);
		participants.remove(currentParticipant);

		roomMap.put(currentRoom, participants);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sendLeavedMessage(currentParticipant.session);
		
		//send room info to participants
		ArrayList<String> streamIds = new ArrayList<>();
		for (Participant temp : participants) {
			streamIds.add(temp.streamId);
		}

		for (Participant temp : participants) {
			sendRoomInfoCommand(currentRoom.getId(), streamIds, temp.session);
		}
	}
	
	private void sendLeavedMessage(Session session) {
		JSONObject jsonResponse = new JSONObject();
		jsonResponse.put(ConferenceWSConstants.COMMAND, ConferenceWSConstants.LEAVED_THE_ROOM);

		sendMessage(jsonResponse.toJSONString(), session);
	}

	private void sendRoomInfoCommand(String roomId, List<String> streams, Session session) {
		JSONArray jsonStreamArray = new JSONArray();
		jsonStreamArray.addAll(streams);
		JSONObject jsObject = new JSONObject();
		jsObject.put(ConferenceWSConstants.COMMAND, ConferenceWSConstants.ROOM_INFORMATION_NOTIFICATION);
		jsObject.put(ConferenceWSConstants.STREAMS_IN_ROOM, jsonStreamArray);	

		String jsonString = jsObject.toJSONString();
		sendMessage(jsonString, session);
	}
	
	public void sendMessage(String message, final Session session) {
		synchronized (this) {
			if (session.isOpen()) {
				try {
					session.getBasicRemote().sendText(message);
				} catch (IOException e) {
					logger.error(ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}
}

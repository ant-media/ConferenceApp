package io.antmedia.conferenceapp.websocket;

public class ConferenceWSConstants {

	private ConferenceWSConstants() {
	}

	public static final String PING_COMMAND = "ping";
	
	public static final String PONG_COMMAND = "pong";
	
	public static final String COMMAND = "command";

	public static final String STREAM_ID = "streamId";

	public static final String DEFINITION = "definition";

	public static final String ERROR_COMMAND = "error";

	public static final String ERROR_CODE = "error_code";

	public static final String SESSION_STREAM_ROOM_MAP = "session_room_map";

	public static final String JOIN_ROOM_COMMAND = "joinRoom";

	public static final String ROOM = "room";

	public static final String LEAVE_THE_ROOM = "leaveFromRoom";
	
	public static final String JOINED_THE_ROOM = "joinedTheRoom";
	
	public static final String LEAVED_THE_ROOM = "leavedFromRoom";
	
	public static final String STREAMS_IN_ROOM = "streams";

	public static final String ROOM_INFORMATION_NOTIFICATION = "roomInformation";

}

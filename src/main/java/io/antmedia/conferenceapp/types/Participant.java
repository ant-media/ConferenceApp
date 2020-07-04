package io.antmedia.conferenceapp.types;

import javax.websocket.Session;

public class Participant {
	public String streamId;
	public Session session;
	
	public Participant(String streamId, Session session) {
		this.streamId = streamId;
		this.session = session;
	}
}

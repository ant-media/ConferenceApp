package io.antmedia.conferenceapp.settings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@PropertySource("/WEB-INF/red5-web.properties")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConferenceSettings {
	public static final String SETTINGS_ACCEPT_ONLY_ROOMS_IN_DATA_STORE = "settings.acceptOnlyRoomsInDataStore";

	public static final String BEAN_NAME = "conferenceSettings";

	@Value( "${"+SETTINGS_ACCEPT_ONLY_ROOMS_IN_DATA_STORE+":false}" )
	private boolean acceptOnlyRoomsInDataStore;

	public boolean isAcceptOnlyRoomsInDataStore() {
		return acceptOnlyRoomsInDataStore;
	}

	public void setAcceptOnlyRoomsInDataStore(boolean acceptOnlyRoomsInDataStore) {
		this.acceptOnlyRoomsInDataStore = acceptOnlyRoomsInDataStore;
	}
}

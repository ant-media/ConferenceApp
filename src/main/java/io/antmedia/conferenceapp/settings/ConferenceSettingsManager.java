package io.antmedia.conferenceapp.settings;

import io.antmedia.datastore.preference.PreferenceStore;

public class ConferenceSettingsManager {
	public static final String BEAN_NAME = "conferenceSettingsManager";
	
	private ConferenceSettings settings;

	public ConferenceSettings getSettings() {
		return settings;
	}

	public boolean updateSettings(ConferenceSettings conferenceSettings) {
		settings.setAcceptOnlyRoomsInDataStore(conferenceSettings.isAcceptOnlyRoomsInDataStore());
		
		PreferenceStore store = new PreferenceStore("webapps/ConferenceApp/WEB-INF/red5-web.properties");
		store.put(ConferenceSettings.SETTINGS_ACCEPT_ONLY_ROOMS_IN_DATA_STORE, String.valueOf(conferenceSettings.isAcceptOnlyRoomsInDataStore()));
		
		return store.save();
	}

	public void setSettings(ConferenceSettings settings) {
		this.settings = settings;
	}
}

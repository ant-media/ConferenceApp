package io.antmedia.conferenceapp.rest;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.Gson;

import io.antmedia.conferenceapp.datastore.ConferenceStore;
import io.antmedia.conferenceapp.settings.ConferenceSettings;
import io.antmedia.conferenceapp.settings.ConferenceSettingsManager;
import io.antmedia.conferenceapp.types.Room;
import io.antmedia.datastore.db.types.Broadcast;
import io.antmedia.rest.model.Result;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Component
@Path("/")
public class ConferenceRestService {

	@Context
	protected ServletContext servletContext;
	private ConferenceStore datastore;
	private ConferenceSettingsManager settingsManager;
	Gson gson = new Gson();

	
	@GET
	@Path("/rooms/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRooms() {
		List<Room> rooms = getDatastore().getRooms();
		return Response.status(Status.OK).entity(rooms).build();
	}
	
	@GET
	@Path("/rooms/{roomId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRoom(@ApiParam(value = "id of the Room", required = true) @PathParam("roomId") String roomId) {
		Room room = null;
		if (roomId != null) {
			room = getDatastore().getRoom(roomId);
		}
		return Response.status(Status.OK).entity(room).build();
	}


	@ApiResponses(value = { @ApiResponse(code = 400, message = "If room id is already used in the data store, it returns error", response=Result.class),
			@ApiResponse(code = 200, message = "Returns the created room", response = Broadcast.class)})
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("/rooms/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createRoom(@ApiParam(value = "id of the Room", required = true) @QueryParam("roomId") String roomId) {
		if(getDatastore().getRoom(roomId) == null) {
			Room room = new Room(roomId);
			getDatastore().saveRoom(room);

			return Response.status(Status.OK).entity(room).build();
		}
		else {
			return Response.status(Status.BAD_REQUEST).entity("Room id exists").build();

		}
	}

	@GET
	@Path("/settings/}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getSettings(){
		return gson.toJson(getSettingsManager().getSettings());
	}
	
	
	@PUT
	@Path("/settings/}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String changeSettings(ConferenceSettings newSettings){
		return gson.toJson(new Result(settingsManager.updateSettings(newSettings)));
	}
	
	
	public ConferenceStore getDatastore() {
		if(datastore == null) {
			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			datastore = (ConferenceStore) ctxt.getBean(ConferenceStore.BEAN_NAME);
		}
		return datastore;
	}
	
	public ConferenceSettingsManager getSettingsManager() {
		if(settingsManager == null) {
			WebApplicationContext ctxt = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
			settingsManager = (ConferenceSettingsManager) ctxt.getBean(ConferenceSettingsManager.BEAN_NAME);
		}
		return settingsManager;
	}

}

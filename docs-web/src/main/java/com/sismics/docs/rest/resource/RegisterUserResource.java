package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.RegisterUserDao;
import com.sismics.docs.core.dao.dto.RegisterUserDto;
import com.sismics.docs.core.model.jpa.RegisterUser;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * REST resource for managing user registration requests.
 */
@Path("/registerUser")
public class RegisterUserResource extends BaseResource {
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 50;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int INITIAL_STATUS = 0;

    /**
     * Registers a new user request.
     */
    @PUT
    @Path("/register")
    public Response registerUser(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email,
            @FormParam("storage") String storage) {
        
        validateRegistrationInput(username, password, email, storage);
        
        RegisterUser registerUser = createRegistrationRequest(username, password, email, storage);
        
        try {
            new RegisterUserDao().create(registerUser);
        } catch (Exception e) {
            handleRegistrationException(e);
        }

        return buildSuccessResponse();
    }

    /**
     * Validates registration input parameters.
     */
    private void validateRegistrationInput(String username, String password, String email, String storage) {
        username = ValidationUtil.validateLength(username, "username", USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH);
        email = ValidationUtil.validateLength(email, "email", 1, EMAIL_MAX_LENGTH);
        Long storageNum = ValidationUtil.validateLong(storage, "storage");
        ValidationUtil.validateEmail(email, "email");
    }

    /**
     * Creates a registration request object.
     */
    private RegisterUser createRegistrationRequest(String username, String password, String email, String storage) {
        RegisterUser registerUser = new RegisterUser();
        registerUser.setUsername(username);
        registerUser.setPassword(password);
        registerUser.setEmail(email);
        registerUser.setStorage(ValidationUtil.validateLong(storage, "storage"));
        registerUser.setStatus(INITIAL_STATUS);
        return registerUser;
    }

    /**
     * Handles registration exceptions.
     */
    private void handleRegistrationException(Exception e) throws ClientException, ServerException {
        switch (e.getMessage()) {
            case "AlreadyRegisteringUsername":
                throw new ClientException("AlreadyRegisteringUsername", "This username is under review", e);
            case "AlreadyExistingUsername":
                throw new ClientException("AlreadyExistingUsername", "Login already used", e);
            default:
                throw new ServerException("UnknownError", "Unknown server error", e);
        }
    }

    /**
     * Lists all registration requests.
     */
    @GET
    @Path("/list")
    public Response list() {
        List<RegisterUserDto> registerUserDtoList = new RegisterUserDao().listAll();
        JsonArrayBuilder registerUsers = Json.createArrayBuilder();

        for (RegisterUserDto dto : registerUserDtoList) {
            registerUsers.add(buildRegisterUserJson(dto));
        }

        return Response.ok()
                .entity(Json.createObjectBuilder()
                        .add("register_users", registerUsers)
                        .build())
                .build();
    }

    /**
     * Builds JSON representation of a registration request.
     */
    private JsonObjectBuilder buildRegisterUserJson(RegisterUserDto dto) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", dto.getId())
                .add("username", dto.getUsername())
                .add("email", dto.getEmail())
                .add("storage", dto.getStorage())
                .add("submit_time", dto.getSubmitTime())
                .add("status", dto.getStatus());

        if (dto.getOperatedTime() == null) {
            builder.add("operated_time", "null");
        } else {
            builder.add("operated_time", dto.getOperatedTime());
        }

        return builder;
    }

    /**
     * Updates the status of a registration request.
     */
    @POST
    @Path("/operate")
    public Response updateStatus(
            @FormParam("id") String id,
            @FormParam("status") Integer status) {
        
        JsonObjectBuilder response = Json.createObjectBuilder();
        
        try {
            Long operatedTime = new RegisterUserDao().updateStatus(id, status);
            response.add("status", status)
                   .add("operated_time", operatedTime);
        } catch (Exception e) {
            return handleStatusUpdateException(e, response);
        }

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Handles status update exceptions.
     */
    private Response handleStatusUpdateException(Exception e, JsonObjectBuilder response) {
        switch (e.getMessage()) {
            case "InvalidStatus":
                response.add("error", "invalid status");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(response.build())
                        .build();
            case "NoSuchUser":
                response.add("error", "no such user");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(response.build())
                        .build();
            case "MultipleUser":
            case "AlreadyExistingUsername":
                response.add("error", "server error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(response.build())
                        .build();
            default:
                response.add("error", "unknown error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(response.build())
                        .build();
        }
    }

    /**
     * Builds a success response for registration.
     */
    private Response buildSuccessResponse() {
        return Response.ok()
                .entity(Json.createObjectBuilder()
                        .add("status", "ok")
                        .build())
                .build();
    }
}
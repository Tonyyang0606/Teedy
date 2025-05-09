package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.RegisterUserDao;
import com.sismics.docs.core.dao.dto.RegisterUserDto;
import com.sismics.docs.core.model.jpa.RegisterUser;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.transaction.SystemException;
import jakarta.ws.rs.*;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.model.jpa.User;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

@Path("/registerUser")
public class RegisterUserResource extends BaseResource{
    @PUT
    @Path("/register")
    public Response registerUser(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email,
            @FormParam("storage") String storage
    ){
        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        Long storageNum = ValidationUtil.validateLong(storage, "storage");
        ValidationUtil.validateEmail(email, "email");

        //Instantiate Register User
        RegisterUser registerUser = new RegisterUser();
        registerUser.setUsername(username);
        registerUser.setPassword(password);
        registerUser.setEmail(email);
        registerUser.setStorage(storageNum);
        registerUser.setStatus(0);

        // Put
        RegisterUserDao dao = new RegisterUserDao();
        try{
            dao.create(registerUser);
        } catch(Exception e) {
            if("AlreadyRegisteringUsername".equals(e.getMessage())){
                throw new ClientException("AlreadyRegisteringUsername","This username is under review", e);
            }
            else if("AlreadyExistingUsername".equals(e.getMessage())){
                throw new ClientException("AlreadyExistingUsername", "Login already used", e);
            }
            else{
                throw new ServerException("UnknownError", "Unknown server error", e);
            }
        }

        System.out.println("RegisterUser: " + registerUser);
        JsonObjectBuilder reponse = Json.createObjectBuilder().add("status", "ok");
        return Response.ok().entity(reponse.build()).build();
    }

    @GET
    @Path("/list")
    public Response list(){
        RegisterUserDao registerUserDao = new RegisterUserDao();
        List<RegisterUserDto> registerUserDtoList = registerUserDao.listAll();
        JsonArrayBuilder registerUsers = Json.createArrayBuilder();

        for(RegisterUserDto registerUserDto : registerUserDtoList){

            JsonObjectBuilder registerUser = Json.createObjectBuilder();
            registerUser.add("id", registerUserDto.getId())
                    .add("username", registerUserDto.getUsername())
                    .add("email", registerUserDto.getEmail())
                    .add("storage", registerUserDto.getStorage())
                    .add("submit_time", registerUserDto.getSubmitTime())
                    .add("status", registerUserDto.getStatus());
            if(registerUserDto.getOperatedTime() == null){
                registerUser.add("operated_time", "null");
            } else {
                registerUser.add("operated_time", registerUserDto.getOperatedTime());
            }
            registerUsers.add(registerUser);
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("register_users", registerUsers);
        return Response.ok().entity(response.build()).build();
    }

   @POST
    @Path("/operate")
   public Response updateStatus(
            @FormParam("id") String id,
            @FormParam("status") Integer status
    ){
        RegisterUserDao registerUserDao = new RegisterUserDao();
        JsonObjectBuilder response = Json.createObjectBuilder();
        Long operated_time;
        try{
            operated_time = registerUserDao.updateStatus(id, status);
            response.add("status", status);
            response.add("operated_time", operated_time);
        } catch(Exception e) {
            if("InvalidStatus".equals(e.getMessage())) {
                response.add("error", "invalid status");
                return Response.status(Response.Status.BAD_REQUEST).entity(response.build()).build();
            }
            else if("NoSuchUser".equals(e.getMessage())) {
                response.add("error", "no such user");
                return Response.status(Response.Status.BAD_REQUEST).entity(response.build()).build();
            }
            else if("MultipleUser".equals(e.getMessage())) {
                response.add("error", "server error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.build()).build();
            } else if("AlreadyExistingUsername".equals(e.getMessage())) {
                response.add("error", "server error");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.build()).build();
            }

        }

        return Response.ok().entity(response.build()).build();
    }

}


package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.dto.RegisterUserDto;
import com.sismics.docs.core.model.jpa.RegisterUser;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * DAO for managing user registration requests.
 */
public class RegisterUserDao {
    private static final int MIN_STATUS = 0;
    private static final int MAX_STATUS = 2;
    
    /**
     * Creates a new registration request.
     * 
     * @param registerUser The registration request to create
     * @return The ID of the created registration request
     * @throws Exception If username is already registered or in use
     */
    public String create(RegisterUser registerUser) throws Exception {
        validateUsernameUniqueness(registerUser.getUsername());
        
        // Initialize registration request
        registerUser.setId(UUID.randomUUID().toString());
        registerUser.setSubmitTime(new Date());
        
        // Persist the registration request
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(registerUser);
        
        // Create audit log
        AuditLogUtil.create(registerUser, AuditLogType.CREATE, registerUser.getUsername());
        
        return registerUser.getId();
    }
    
    /**
     * Validates that the username is not already in use or being registered.
     */
    private void validateUsernameUniqueness(String username) throws Exception {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Check if username is already being registered
        Query registeringQuery = em.createQuery(
            "select u from RegisterUser u where u.username = :username and u.status = 0");
        registeringQuery.setParameter("username", username);
        if (!registeringQuery.getResultList().isEmpty()) {
            throw new Exception("AlreadyRegisteringUsername");
        }
        
        // Check if username is already taken by an active user
        Query existingUserQuery = em.createQuery(
            "select u from User u where u.username = :username and u.deleteDate is null");
        existingUserQuery.setParameter("username", username);
        if (!existingUserQuery.getResultList().isEmpty()) {
            throw new Exception("AlreadyExistingUsername");
        }
    }
    
    /**
     * Retrieves all registration requests.
     * 
     * @return List of registration request DTOs
     */
    public List<RegisterUserDto> listAll() {
        String queryStr = "select u.REG_ID_C as c0, u.REG_USERNAME_C as c1, u.REG_EMAIL_C as c2, " +
            "u.REG_STORAGE_N as c3, u.REG_SUBMIT_TIME_D as c4, u.REG_STATUS_N as c5, " +
            "u.REG_OPERATED_TIME_D as c6 from T_REGISTER_USER u";
        
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        List<Object[]> results = em.createNativeQuery(queryStr).getResultList();
        
        List<RegisterUserDto> registerUserDtoList = new ArrayList<>();
        for (Object[] result : results) {
            registerUserDtoList.add(mapResultToDto(result));
        }
        return registerUserDtoList;
    }
    
    /**
     * Maps a database result row to a RegisterUserDto.
     */
    private RegisterUserDto mapResultToDto(Object[] result) {
        int index = 0;
        RegisterUserDto dto = new RegisterUserDto();
        dto.setId((String) result[index++]);
        dto.setUsername((String) result[index++]);
        dto.setEmail((String) result[index++]);
        dto.setStorage((Long) result[index++]);
        dto.setSubmitTime(((Timestamp) result[index++]).getTime());
        dto.setStatus((Integer) result[index++]);
        if (result[index] != null) {
            dto.setOperatedTime(((Timestamp) result[index]).getTime());
        }
        return dto;
    }
    
    /**
     * Updates the status of a registration request.
     * 
     * @param id The ID of the registration request
     * @param status The new status
     * @return The operation timestamp
     * @throws Exception If status is invalid or registration request not found
     */
    public Long updateStatus(String id, Integer status) throws Exception {
        validateStatus(status);
        RegisterUser registerUser = findPendingRegistration(id);
        
        Date operationTime = new Date();
        registerUser.setStatus(status);
        registerUser.setOperatedTime(operationTime);
        
        if (status == Constants.REGISTRATION_STATUS_APPROVED) {
            createUserFromRegistration(registerUser);
        }
        
        AuditLogUtil.create(registerUser, AuditLogType.UPDATE, registerUser.getUsername());
        return operationTime.getTime();
    }
    
    /**
     * Validates that the status is within allowed range.
     */
    private void validateStatus(Integer status) throws Exception {
        if (status < MIN_STATUS || status > MAX_STATUS) {
            throw new Exception("InvalidStatus");
        }
    }
    
    /**
     * Finds a pending registration by ID.
     */
    private RegisterUser findPendingRegistration(String id) throws Exception {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createQuery(
            "select u from RegisterUser u where u.id = :id and status = 0");
        query.setParameter("id", id);
        
        List<?> results = query.getResultList();
        if (results.isEmpty()) {
            throw new Exception("NoSuchUser");
        }
        if (results.size() > 1) {
            throw new Exception("MultipleUser");
        }
        
        return (RegisterUser) results.get(0);
    }
    
    /**
     * Creates a user account from an approved registration request.
     */
    private void createUserFromRegistration(RegisterUser registerUser) {
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(registerUser.getUsername());
        user.setPassword(registerUser.getPassword());
        user.setEmail(registerUser.getEmail());
        user.setStorageQuota(registerUser.getStorage());
        user.setOnboarding(true);
        
        new UserDao().create(user, user.getUsername());
    }
}
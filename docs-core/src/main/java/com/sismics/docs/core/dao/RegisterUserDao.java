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
 * Register User Dao
 */
public class RegisterUserDao {
    public String create(RegisterUser registerUser) throws Exception{
        // Create the user UUID
        registerUser.setId(UUID.randomUUID().toString());

        // Checks for user unicity
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q1 = em.createQuery("select u from RegisterUser u where u.username = :username and u.status = 0");
        q1.setParameter("username", registerUser.getUsername());
        List<?> l1 = q1.getResultList();
        if (l1.size() > 0) {
            throw new Exception("AlreadyRegisteringUsername");
        }
        Query q2 = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        q2.setParameter("username", registerUser.getUsername());
        List<?> l2 = q2.getResultList();
        if(l2.size() > 0){
            throw new Exception("AlreadyExistingUsername");
        }

        // Create the user
        registerUser.setSubmitTime(new Date());
        em.persist(registerUser);

        // Create audit log
        AuditLogUtil.create(registerUser, AuditLogType.CREATE, registerUser.getUsername());

        return registerUser.getId();
    }

    public List<RegisterUserDto> listAll() {
        StringBuilder sb = new StringBuilder("select u.REG_ID_C as c0, u.REG_USERNAME_C as c1, u.REG_EMAIL_C as c2, u.REG_STORAGE_N as c3, u.REG_SUBMIT_TIME_D as c4, u.REG_STATUS_N as c5, u.REG_OPERATED_TIME_D as c6");
        sb.append(" from T_REGISTER_USER u");
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q1 = em.createNativeQuery(sb.toString());
        List<Object[]> l1 = q1.getResultList();

        List<RegisterUserDto> registerUserDtoList = new ArrayList<>();
        for(Object[] o: l1){
            int i = 0;
            RegisterUserDto registerUserDto = new RegisterUserDto();
            registerUserDto.setId((String) o[i++]);
            registerUserDto.setUsername((String) o[i++]);
            registerUserDto.setEmail((String) o[i++]);
            registerUserDto.setStorage((Long) o[i++]);
            registerUserDto.setSubmitTime(((Timestamp) o[i++]).getTime());
            registerUserDto.setStatus((Integer) o[i++]);
            if(o[i] != null){
                registerUserDto.setOperatedTime(((Timestamp) o[i]).getTime());
            }
            registerUserDtoList.add(registerUserDto);
        }
        return registerUserDtoList;
    }
      public Long updateStatus(String id, Integer status) throws Exception{
        if(status < 0 || status > 2){
            throw new Exception("InvalidStatus");
        }

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q1 = em.createQuery("select u from RegisterUser u where u.id = :id and status = 0");
        q1.setParameter("id", id);
        List<?> l1 = q1.getResultList();
        if(l1.isEmpty()){
            throw new Exception("NoSuchUser");
        }
        if(l1.size() > 1){
            throw new Exception("MultipleUser");
        }
        Date date = new Date();
        RegisterUser registerUser = (RegisterUser) l1.get(0);
        registerUser.setStatus(status);
        registerUser.setOperatedTime(date);

        if(status == 1){
            User user = new User();
            user.setRoleId(Constants.DEFAULT_USER_ROLE);
            user.setUsername(registerUser.getUsername());
            user.setPassword(registerUser.getPassword());
            user.setEmail(registerUser.getEmail());
            user.setStorageQuota(registerUser.getStorage());
            user.setOnboarding(true);

            UserDao userDao = new UserDao();
            userDao.create(user, user.getUsername());
        }

        AuditLogUtil.create(registerUser, AuditLogType.UPDATE, registerUser.getUsername());
        return date.getTime();
    }
}
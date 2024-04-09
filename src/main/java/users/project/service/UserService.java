package users.project.service;

import users.project.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    /**
     * Function that return true if the user is from "France" and his age > 18 otherwise returns false
     *
     * @param user
     * @return boolean
     */
    public boolean isValidUser(User user) {
        return user != null && user.getAge() > 18 && "France".equalsIgnoreCase(user.getCountry());
    }


    /**
     * Function that reads the file and store it in a List
     *
     * @param users
     * @param usersFile
     * @param objectMapper
     * @param userType
     * @throws IOException
     */
    public void loadUsers(List users, File usersFile, ObjectMapper objectMapper, CollectionType userType) throws IOException{
        if (users == null) {
            if (!usersFile.exists()) {
                users = new ArrayList<>();
            } else {
                users = objectMapper.readValue(usersFile, userType);
            }
        }
    }
}

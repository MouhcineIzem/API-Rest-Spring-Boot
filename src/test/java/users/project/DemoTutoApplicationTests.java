package users.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import users.project.controller.UserController;
import users.project.entity.User;
import users.project.repository.UserRepository;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import users.project.service.UserService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest
class DemoTutoApplicationTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test if a user register with success in the database
     */
    @Test
    public void testRegisterUser() {
        User user = new User(425, "Mouhcine", 25, "France");

        when(userService.isValidUser(user)).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);

        ResponseEntity<String> response = userController.registerUser(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test to get details of a user using his id
     */
    @Test
    public void testGetUserDetails() {
        User user = new User(176, "Mouhcine", 56, "USA");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUserDetails(user.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

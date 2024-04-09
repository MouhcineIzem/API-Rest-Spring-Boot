package users.project.controller;

import users.project.repository.UserRepository;
import users.project.entity.User;
import users.project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();
    private static final CollectionType userType = typeFactory.constructCollectionType(List.class, User.class);
    private static final File usersFile = new File("users.json");
    private List<User> users = new ArrayList<>();




    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    /**
     * Register a new user in the Cassandra database.
     *
     * @param user object containing the details of the user to be registered.
     * @return A ResponseEntity containing the HTTP status.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (!userService.isValidUser(user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user data");
        }


        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * Get details of a user from the Cassandra database based on the provider user id.
     *
     * @param id the unique identifier of the user
     * @return ResponseEntity containing the HTTP status and the details of the user if found.
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable int id) {
        try {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(userOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user details");
        }
    }

    /**
     * Register a new user by adding their details to the users.json file.
     *
     * @param user The user object containing the details of the user to be registered.
     * @return A ResponseEntity containing the HTTP status.
     * @throws IOException
     */

    @PostMapping("registerUserInFile")
    public ResponseEntity<String> registerUserInFile(@RequestBody User user) throws IOException{
        Instant start = Instant.now();

        if (!userService.isValidUser(user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user data");
        }

        userService.loadUsers(users, usersFile, objectMapper, userType);


        users.add(user);
        objectMapper.writeValue(usersFile, users);

        Instant finish = Instant.now();
        long elapsedTime = Duration.between(start, finish).toMillis();
        System.out.println("User registered in " + elapsedTime + "ms");

        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * Get details of a user from the users.json file based on the provided user id.
     * @param id The identifier of the user whose details are to be retrieved.
     * @param country Parameter by default that contain "France" as String, in the case the user has no country implementation.
     * @return A ResponseEntity containing the HTTP status with the user details in the response body.
     */

    @GetMapping("userInFile/{id}")
    public ResponseEntity<String> getUserDetailsInFile(@PathVariable int id, @RequestParam(required = false, defaultValue = "France") String country) {
        Instant start = Instant.now();

        try {
            JSONArray jsonArray = (JSONArray) (new JSONParser()).parse(new FileReader(usersFile));


            for (Object obj : jsonArray) {
                JSONObject user = (org.json.simple.JSONObject) obj;
                if (Integer.parseInt(user.get("id").toString()) == id) {
                    Instant finish = Instant.now();
                    long elapsedTime = Duration.between(start, finish).toMillis();
                    System.out.println("User details fetched in " + elapsedTime + "ms");

                    return ResponseEntity.ok(user.toJSONString());
                }
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get user details");
        }
    }
}

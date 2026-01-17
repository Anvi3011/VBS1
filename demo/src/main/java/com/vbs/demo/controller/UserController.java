package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*") // allow data transfer btw port
public class UserController {
    @Autowired
    UserRepo userRepo;

    @Autowired
    HistoryRepo historyRepo;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user)
    {
        if(user.getRole().equalsIgnoreCase("admin")){
            return ResponseEntity
                    .badRequest()
                    .body("Admin registration is not allowed");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        History h1=new History();
        h1.setDescription("User Self Created : "+user.getUsername());
        historyRepo.save(h1);
        return ResponseEntity.ok("Signup Successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        Optional<User> optionalUser =
                Optional.ofNullable(userRepo.findByUsername(user.getUsername()));

        if(optionalUser.isEmpty()){
            return ResponseEntity.ok("Invalid username or password");
        }

        User dbUser = optionalUser.get();

        if(!dbUser.isActive()){
            return ResponseEntity.ok(
                    "Your account has been deleted by admin. Contact support."
            );
        }

        if(!passwordEncoder.matches(
                user.getPassword(),
                dbUser.getPassword())){

            return ResponseEntity.ok("Invalid username or password");
        }
        return ResponseEntity.ok(dbUser.getId() + ":" + dbUser.getRole());
    }


    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found"));
        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

        @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj){
        User user=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not Found"));
        History h1=new History();
        if(obj.getKey().equalsIgnoreCase("name")){
            if(user.getName().equalsIgnoreCase(obj.getValue()))return  "Cannot be same";
            h1.setDescription("User changed name from : "+user.getUsername()+ " to "+obj.getValue());
            user.setName(obj.getValue());
        }
        else  if(obj.getKey().equalsIgnoreCase("Password")){
            if(user.getPassword().equalsIgnoreCase(obj.getValue()))return  "Cannot be same";
            h1.setDescription("User changed Password : "+user.getUsername());
            user.setPassword(passwordEncoder.encode(obj.getValue()));

        }
        else  if(obj.getKey().equalsIgnoreCase("Email")){
            if(user.getEmail().equalsIgnoreCase(obj.getValue()))return  "Cannot be same";

            User user2=userRepo.findByEmail(obj.getValue());
            if(user2!=null) return "Email already exists";
            h1.setDescription("User changed Email from : "+user.getEmail()+ " to "+obj.getValue());
            user.setEmail(obj.getValue());
        }
        else{
            return "Invalid key";
        }
        userRepo.save(user);
        historyRepo.save(h1);
        return "update done successfully";
    }

    @PostMapping("/add/{adminId}")
    public ResponseEntity<String> add(
            @RequestBody User newUser,
            @PathVariable int adminId) {

        Optional<User> adminOpt = userRepo.findById(adminId);

        if(adminOpt.isEmpty()){
            return ResponseEntity.badRequest().body("Admin not found");
        }

        User admin = adminOpt.get();

        if(!admin.getRole().equalsIgnoreCase("admin")){
            return ResponseEntity.badRequest().body("Unauthorized");
        }

        if(newUser.getRole().equalsIgnoreCase("admin")){
            return ResponseEntity
                    .badRequest()
                    .body("Admin cannot create another admin");
        }

        newUser.setPassword(
                passwordEncoder.encode(newUser.getPassword())
        );
        newUser.setActive(true);

        userRepo.save(newUser);
        return ResponseEntity.ok("User added successfully");
    }


    @GetMapping("/users")
    public List<User> getAllUsers(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return userRepo.findByIsActiveTrueAndRoleNot("admin", sort);

    }


    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword){
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }

    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public ResponseEntity<String> deleteUser(
            @PathVariable int userId,
            @PathVariable int adminId) {

        Optional<User> adminOpt = userRepo.findById(adminId);
        Optional<User> userOpt = userRepo.findById(userId);

        if(adminOpt.isEmpty() || userOpt.isEmpty()){
            return ResponseEntity.badRequest().body("User not found");
        }

        User admin = adminOpt.get();
        User user = userOpt.get();

        if(!admin.getRole().equalsIgnoreCase("admin")){
            return ResponseEntity.badRequest().body("Unauthorized");
        }

        if(admin.getId() == user.getId()){
            return ResponseEntity
                    .badRequest()
                    .body("Admin cannot delete self");
        }

        user.setActive(false); // SOFT DELETE
        userRepo.save(user);

        return ResponseEntity.ok("User deleted successfully");
    }

}


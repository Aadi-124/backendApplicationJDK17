package com.example.BackendApplication.Controller;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendApplication.Database.SequenceGenerator;
import com.example.BackendApplication.Entities.AppUser;
import com.example.BackendApplication.Entities.Credentials;
import com.example.BackendApplication.Entities.Email;
import com.example.BackendApplication.Entities.Todos;
import com.example.BackendApplication.Entities.UserAuthorization;
import com.example.BackendApplication.Repository.DBSequenceRepository;
import com.example.BackendApplication.Repository.AppUserRepository;
import com.example.BackendApplication.Repository.TodosRepository;
import com.example.BackendApplication.Service.Base64EncodingService;
import com.example.BackendApplication.Service.EmailService;

import jakarta.mail.MessagingException;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TodoController {

    public Integer autoIncId = 0;
    private static Integer OTP = 11111;

    @Autowired
    public TodosRepository todoRepository;

    @Autowired
    public DBSequenceRepository sequenceRepository;

    @Autowired
    public SequenceGenerator sequencegenerator;

    @Autowired
    public AppUserRepository appUserRepository;

    // @Autowired
    // public PasswordEncoder passwordEncoder;

    @GetMapping("/public/test/test1")
    public String test() {
        return "ACCESSED!";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/users")
    public String user() {
        return "USER ACCESS Granteed!";
    }

    @GetMapping("/admin")
    public String admin(){
        return "ADMIN ACCESS Granteed!";
    }

    @GetMapping("/public/check-email/{email}")
    public boolean checkEmail(@PathVariable String email){
        if(appUserRepository.findByEmail(email).isPresent()){
            return true;
        } else {
            return false;
        }
    }



    @PostMapping("/users/addusertodo")
    public boolean addUserTodo(@RequestBody Todos todo){
        todo.setId(sequencegenerator.generateSequence(Todos.SEQUENCE_NAME));
        AppUser user = appUserRepository.findById(todo.getUserid()).get();
        Integer id = user.getNooftodos()+1;
        user.setNooftodos(id);
        appUserRepository.save(user);
        todo.setUserspecifictodoid(id);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        todo.setCreatedDate(formatter.format(date));
        todoRepository.save(todo);
        return true;
    }

    @PostMapping("/users/getusertodos")
    public List<Todos> getUserTodos(@RequestBody Credentials credentials){
        return todoRepository.findByUserid(credentials.getUserid());
    }
    
    @GetMapping("/admin/getalltodos")
    public List<Todos> getAllTodos(){
        return todoRepository.findAll();
    }




    // @Autowired
    // public PasswordEncoder passwordEncoder;

    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody AppUser user) {


        if (OTP.toString().compareTo(user.getOtp()) == 0) {
            user.setId(sequencegenerator.generateSequence(AppUser.SEQUENCE_NAME));
            // user.setPassword(user.getPassword());
            user.setPassword(user.getPassword());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            user.setAccountcreationdate(formatter.format(date));
            user.setNooftodos(0);
            appUserRepository.save(user);
            URI location = URI.create("/public/login");
            return ResponseEntity.created(location).body("Created!");
        } else {
            return ResponseEntity.ok().body("Invalid OTP");
        }

    }
    // @PostMapping("/public/register")
    // public ResponseEntity<?> registerUser(@RequestBody AppUser user) {
       
    //         user.setId(sequencegenerator.generateSequence(AppUser.SEQUENCE_NAME));
    //         user.setPassword(user.getPassword());
    //         SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    //         Date date = new Date();
    //         user.setAccountcreationdate(formatter.format(date));
    //         user.setNooftodos(0);
    //         appUserRepository.save(user);
    //         URI location = URI.create("/public/login");
    //         return ResponseEntity.created(location).body("Created!");

    // }

    @PostMapping("/public/login")
    public UserAuthorization login(@RequestBody Credentials credentials) {
        Optional<AppUser> appuser = appUserRepository.findByName(credentials.getUsername());
        AppUser user = appuser.get();
        if (appuser.isPresent()) {
            if (appuser.get().getPassword().compareTo(credentials.getPassword()) == 0) {
                String encodedValue = Base64EncodingService
                        .encode(credentials.getUsername() + ":" + credentials.getPassword());
                String Authorization = "Basic " + encodedValue;
                return new UserAuthorization(Authorization,user.getRole(),true,user.getId().toString(),user.getName());
            }
        }
        return new UserAuthorization(null,null,false,null,null);
    }

    public boolean sendOTP(String email, String msg) {
        try {

            Random random = new Random();
            OTP = 10000 + random.nextInt(90000);
            EmailService mailService = new EmailService();
            String to = email;
            String from = "aadityathakare124@gmail.com";
            String subject = "OTP - Todo Management Application Registration";
            String message = "<h1>" + msg + ":- <h1 style='color:blue'>" + OTP + "</h1> </h1>";
            if (mailService.sendEmail(to, from, message, subject)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception Ex) {
            System.out.println(Ex);
            return false;
        }
    }

    @PostMapping("/public/otp-registration")
    public boolean sendOTPRegistration(@RequestBody Email email) throws MessagingException {
        return sendOTP(email.getEmail(), "Your OTP for Todo Management Application Registration:- ");
    }

    @PostMapping("/public/otp-forgot")
    public ResponseEntity<?> sendOTPForgot(@RequestBody Email email) {
        try{
            if(appUserRepository.findByEmail(email.getEmail()).isPresent()){
                if(sendOTP(email.getEmail(),"OTP for Reseting Password:- ")){
                    // return appUserRepository.findByEmail(email.getEmail()).get().getName();
                    ResponseEntity.ok().body(appUserRepository.findByEmail(email.getEmail()).get().getName());
                } else {
                    return ResponseEntity.status(500).body("Server_Not_Connected");
                }
            } else {
                return ResponseEntity.badRequest().body("Server_Not_Connected");
            }
        } catch (Exception Ex){
            return ResponseEntity.status(500).body("Server_Not_Connected");
        }
        return null;        
    }

    @PostMapping("/public/reset-password")
    public boolean resetPassword(@RequestBody Credentials credentails) {
        try {
            Optional<AppUser> appuser = appUserRepository.findByEmail(credentails.getEmail());
            AppUser user = appuser.get();
            user.setPassword(credentails.getPassword());
            appUserRepository.save(user);
            return true;
        } catch (Exception Ex) {
            System.out.println(Ex);
            return false;
        }

    }

    @GetMapping("/public/verify-otp/{otp}")
    public boolean verifyOTP(@PathVariable String otp) {
        System.out.println("otp = " + otp);
        System.out.println("OTP = " + OTP);
        if (OTP.toString().compareTo(otp) == 0) {
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("/users/deletetodo")
    public Integer deleteTodos(@RequestBody Todos todo){
        todoRepository.deleteById(todo.getId());
        return todo.getId();
    }

    @PutMapping("/users/updatetodo")
    public Todos updateTodo(@RequestBody Todos todo) {
        Todos usertodo = todoRepository.findById(todo.getId()).get();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        usertodo.setUpdatedDate(formatter.format(date));
        usertodo.setDescription(todo.getDescription());
        usertodo.setTargetedDate(todo.getTargetedDate());
        usertodo.setIsDone(false);
        todoRepository.save(usertodo);
        return todo;
    }

    @PutMapping("/users/setisdone")
    public boolean setIsDone(@RequestBody Todos todo){
        Todos usertodo = todoRepository.findById(todo.getId()).get();
        usertodo.setIsDone(true);
        todoRepository.save(usertodo);
        return true;
    }

    @PostMapping("/public/send-adminotp")
    public boolean sendAdminOtp(@RequestBody Credentials credentials){

        if(credentials.getCode().compareTo("827580") == 0){
            return sendOTP("aadityathakare111@gmail.com","OTP for ADMIN Registration(TodosManagementApp):- ");
        } else {
            return false;
        }

    }
}


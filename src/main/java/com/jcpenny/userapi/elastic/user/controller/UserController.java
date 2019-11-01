package com.jcpenny.userapi.elastic.user.controller;

import com.jcpenny.userapi.elastic.user.dao.UserDao;
import com.jcpenny.userapi.elastic.user.model.User;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private UserDao userDao;

    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("")
    public ResponseEntity<String> getUsers() {
        return ResponseEntity.ok(userDao.matchAllQuery());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable String id){
        return userDao.getUserById(id);
    }


    @PostMapping
    public ResponseEntity<String> insertUser(@RequestBody User user) throws Exception {
        return ResponseEntity.ok(userDao.insertUser(user));
    }


    @PutMapping("/{id}")
    public Map<String, Object> updateUserById(@RequestBody User user, @PathVariable String id) {
        return userDao.updateUserById(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable String id) {
        return ResponseEntity.ok(userDao.deleteUserById(id));
    }
}

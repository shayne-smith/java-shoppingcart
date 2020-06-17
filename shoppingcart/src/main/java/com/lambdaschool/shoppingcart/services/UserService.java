package com.lambdaschool.shoppingcart.services;

import com.lambdaschool.shoppingcart.models.User;

import java.util.List;

public interface UserService
{

    List<User> findAll();

    User findByName(String username);

    User findUserById(long id);

    void delete(long id);

    User save(User user);

    User update(
        User user,
        long id);

    void deleteUserRole(
        long userid,
        long roleid);

    void addUserRole(
        long userid,
        long roleid);
}

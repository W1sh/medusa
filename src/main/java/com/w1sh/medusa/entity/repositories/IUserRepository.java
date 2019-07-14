package com.w1sh.medusa.entity.repositories;

import com.w1sh.medusa.entity.entities.User;

public interface IUserRepository extends IRepository<User, Long> {

    boolean isPresent(User user);
}

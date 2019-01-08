package com.company.authentication;

import org.springframework.data.repository.CrudRepository;

public interface GoogleAuthenticationRepository extends CrudRepository<GoogleUser, Long>{

}

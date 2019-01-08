package com.company.authentication;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface FacebookAuthenticationRepository extends CrudRepository<FacebookUser, Long>{

}

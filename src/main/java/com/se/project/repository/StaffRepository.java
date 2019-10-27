package com.se.project.repository;


import com.se.project.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {

	@Query(value = "select * from user u inner join staff s on u.userId = s.userId where u.email=?1 and s.password = ?2", nativeQuery = true)
	List<Staff> getUser(String email, String password);

}

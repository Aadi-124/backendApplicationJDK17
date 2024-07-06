package com.example.BackendApplication.Repository;



import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.BackendApplication.Entities.Todos;

public interface TodosRepository extends MongoRepository<Todos,Integer> {
    
    List<Todos> findByUserid(Integer userid);

}



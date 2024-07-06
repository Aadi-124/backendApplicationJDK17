package com.example.BackendApplication.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.BackendApplication.Database.DatabaseSequence;

public interface DBSequenceRepository extends MongoRepository<DatabaseSequence,String>{
    
}

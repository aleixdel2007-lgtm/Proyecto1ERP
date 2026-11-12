package dao;

import model.traduccion.Traduccion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TraduccionDao extends MongoRepository<Traduccion, String> {
}

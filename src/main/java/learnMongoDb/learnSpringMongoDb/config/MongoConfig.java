package learnMongoDb.learnSpringMongoDb.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "ecommerce_db";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://admin:password@localhost:27017/ecommerce_db?authSource=admin");
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
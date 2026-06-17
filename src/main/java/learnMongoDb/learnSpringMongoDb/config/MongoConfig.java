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
        // This forces Spring to use these exact credentials, bypassing all properties files
        return MongoClients.create("mongodb://admin:password@localhost:27017/ecommerce_db?authSource=admin");
    }

    @Override
    protected boolean autoIndexCreation() {
        // This replaces the spring.data.mongodb.auto-index-creation=true property
        return true;
    }
}
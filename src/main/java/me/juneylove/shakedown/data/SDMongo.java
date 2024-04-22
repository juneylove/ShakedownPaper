package me.juneylove.shakedown.data;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.Bukkit;

public class SDMongo {

    static MongoDatabase database;

    public static void connect() {

        String connectionString = "mongodb+srv://junezhawthorne:X2NP8TptF9kEZAy6@cluster0.fqiiwfw.mongodb.net/?retryWrites=true&w=majority";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {

                // Send a ping to confirm a successful connection
                database = mongoClient.getDatabase("players");
                MongoCollection<Document> coll = database.getCollection("players");
                Document player = coll.find(Filters.eq("username", "juneylove")).first();
//                if (player == null) {
//                    Bukkit.getPlayer("juneylove").sendMessage("Player not found in database");
//                } else {
//                    Bukkit.getPlayer("juneylove").sendMessage("doc = " + player.toJson());
//                }

            } catch (MongoException e) {
                e.printStackTrace();
            }
        }

    }

}

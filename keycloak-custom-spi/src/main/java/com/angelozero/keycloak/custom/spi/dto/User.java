package com.angelozero.keycloak.custom.spi.dto;

import org.bson.Document;

public record User(String id, String name, String email, String passwordHash) {

    public static User fromDocument(Document doc) {
        return new User(
                doc.getObjectId("_id").toHexString(),
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("passwordHash")
        );
    }
}

package com.blocky.dev.filemanager;

public enum FileID {
    MESSAGES("messages"),
    AUCTION_DATA("auction_data"),
    AUCTION_HOUSE_GUI("auction_house_gui");

    private final String id;

    FileID(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}

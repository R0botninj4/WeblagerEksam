package com.eksam.weblagereksam.BE;

public class UserActivity {

    private final User user;
    private final int boxCount;
    private final int documentCount;
    private final int pageCount;

    public UserActivity(User user, int boxCount, int documentCount, int pageCount) {
        this.user = user;
        this.boxCount = boxCount;
        this.documentCount = documentCount;
        this.pageCount = pageCount;
    }

    public User getUser() { return user; }

    public int getBoxCount() { return boxCount; }

    public int getDocumentCount() { return documentCount; }

    public int getPageCount() { return pageCount; }
}

package com.example.cafeteria_android.common;
import java.util.List;
public class DeleteUsersRequest {
    public List<String> ids;
    public DeleteUsersRequest(List<String> ids) { this.ids = ids; }
}
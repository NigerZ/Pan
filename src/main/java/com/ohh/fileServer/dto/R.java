package com.ohh.fileServer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class R<T> {

    private boolean success;
    private T data;
    public R(){
        super();
    }

    public static <T> R<T> success(){
        R<T> result = new R<>();
        result.success = true;
        return result;
    }
    public static <T> R<T> success(T data){
        R<T> result = new R<>();
        result.success = true;
        result.data = data;
        return result;
    }

    public static <T> R<T> error(T data){
        R<T> result = new R<>();
        result.success = false;
        result.data = data;
        return result;
    }


}

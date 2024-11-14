package com.bkm009.video_editor.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericApiResponse<T> {

    private T data;
    private String error;
    private int statusCode;
    private boolean success;
}

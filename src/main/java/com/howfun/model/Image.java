package com.howfun.model;

import lombok.Data;

@Data
public class Image {
    private int id;
    private String name;
    private byte[] image;
}

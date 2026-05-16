package com.ptithcm.waveapp.model;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * image la TEXT (path file), vi du: "images/albums/al001.jpg"
 * Hien thi: ImageFileHelper.loadIntoImageView(context, getImage(), imageView, placeholder)
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Album implements Serializable {
    private String id;
    private String name;
    private Artist artist;
    private String image;           // path file anh, KHONG phai byte[]
    private LocalDate releaseDate;
    @Builder.Default private boolean active = true;
    private LocalDateTime createdAt;
    // XOA getImagePath() tra byte[] - sai kieu va than ham rong gay loi bien dich
}

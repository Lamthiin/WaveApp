package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.util.List;
@Data @Builder
public class PageResponse<T> {
    private List<T> content;
    private int page, size, totalPages;
    private long totalElements;
    private boolean last;
}

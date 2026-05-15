package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.Song;
import java.util.List;
import java.util.Optional;

/**
 * Interface này đã được lược bỏ Spring Data JPA để tương thích với Android.
 * Sau này bạn có thể triển khai (implement) nó để gọi API qua Retrofit.
 */
public interface SongRepository {
    List<Song> findAll();
    Optional<Song> findById(String id);
    void save(Song song);
    void deleteById(String id);
    
    // Các phương thức tìm kiếm khác từ backend được giữ lại dưới dạng khung (stub)
    List<Song> findByActiveTrue();
    List<Song> findByAlbumIdAndActiveTrue(String albumId);
    List<Song> searchByName(String kw);
    void incrementPlayCount(String id);
}

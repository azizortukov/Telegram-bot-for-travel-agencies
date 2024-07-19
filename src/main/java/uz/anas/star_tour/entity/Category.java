package uz.anas.star_tour.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Category{

    private String id;
    private String name;
    @CreatedDate
    private LocalDateTime createdAt;

    public Category(String name) {
        this.name = name;
    }
}

package uz.anas.star_tour.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Tour {

    private String id;
    private String categoryId;
    private String name;

    public Tour(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }
}

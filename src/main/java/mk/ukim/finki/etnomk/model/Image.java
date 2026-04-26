package mk.ukim.finki.etnomk.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "record_id")
    @JsonBackReference
    private Record record;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(2048)")
    private float[] embedding;


}
package mk.ukim.finki.etnomk.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "image_pattern_patches")
public class ImagePatternPatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    @JsonBackReference
    private Image image;

    private int x;
    private int y;
    private int width;
    private int height;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] embedding;
}

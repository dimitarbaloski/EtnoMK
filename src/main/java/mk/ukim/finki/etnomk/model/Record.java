package mk.ukim.finki.etnomk.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "records")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    private String title;

    @Column(length = 2000)
    private String description;

    private LocalDate dateCreated;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne
    @JoinColumn(name = "technique_id")
    private Technique technique;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL)
    private List<Image> images;

}

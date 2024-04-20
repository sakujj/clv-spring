package ru.clevertec.house.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import ru.clevertec.house.entity.listener.HouseEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Entity
@EntityListeners({HouseEntityListener.class})
@Table(name = "House")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NaturalId
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "area", nullable = false)
    private Double area;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "number", nullable = false)
    private Integer number;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(mappedBy = Person.Fields.ownedHouses)
    private Set<Person> owners;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = Person.Fields.houseOfResidence)
    private List<Person> residents;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    public static final class Fields {
        public static final String id = "id";
        public static final String uuid = "uuid";
        public static final String area = "area";
        public static final String country = "country";
        public static final String city = "city";
        public static final String street = "street";
        public static final String number = "number";
        public static final String owners = "owners";
        public static final String residents = "residents";
        public static final String createDate = "createDate";
    }
}

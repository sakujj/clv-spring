package ru.clevertec.house.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
public class House implements IdentifiableByUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "area")
    private Double area;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "number")
    private Integer number;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(mappedBy = Person.Fields.ownedHouses)
    private Set<Person> owners;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = Person.Fields.houseOfResidence)
    private List<Person> residents;

    @Column(name = "create_date")
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

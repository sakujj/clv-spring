package ru.clevertec.house.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ConverterRegistration;
import ru.clevertec.house.entity.converter.SexConverter;
import ru.clevertec.house.entity.listener.PersonEntityListener;
import ru.clevertec.house.enumeration.Sex;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners({PersonEntityListener.class})
@Table(name = "Person")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "sex")
    @Convert(converter = SexConverter.class)
    private Sex sex;

    @Column(name = "passport_series")
    private String passportSeries;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_of_residence_id", nullable = false)
    private House houseOfResidence;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "owner_to_owned_house",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "house_id")}
    )
    private List<House> ownedHouses;

    public static final class Fields {
        public static final String id = "id";
        public static final String uuid = "uuid";
        public static final String name = "name";
        public static final String surname = "surname";
        public static final String sex = "sex";
        public static final String passportSeries = "passportSeries";
        public static final String passportNumber = "passportNumber";
        public static final String createDate = "createDate";
        public static final String updateDate = "updateDate";
        public static final String houseOfResidence = "houseOfResidence";
        public static final String ownedHouses = "ownedHouses";
    }
}

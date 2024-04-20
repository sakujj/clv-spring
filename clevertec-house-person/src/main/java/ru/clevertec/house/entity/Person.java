package ru.clevertec.house.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import ru.clevertec.house.entity.converter.SexConverter;
import ru.clevertec.house.entity.listener.PersonEntityListener;
import ru.clevertec.house.enumeration.Sex;

import java.time.LocalDateTime;
import java.util.Set;
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
    @Column(name = "id", nullable = false)
    private Long id;

    @NaturalId
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "sex", nullable = false)
    @Convert(converter = SexConverter.class)
    private Sex sex;

    @Column(name = "passport_series", nullable = false)
    private String passportSeries;

    @Column(name = "passport_number", nullable = false)
    private String passportNumber;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_of_residence_id", nullable = false)
    private House houseOfResidence;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "owner_to_owned_house",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "house_id")}
    )
    private Set<House> ownedHouses;

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

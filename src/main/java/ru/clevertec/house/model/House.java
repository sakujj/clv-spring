package ru.clevertec.house.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "House")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Data
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uuid;
    private Integer area;
    private String country;
    private String city;
    private String street;
    private Integer number;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToMany(mappedBy = Person.Fields.ownedHouses)
    private List<Person> owners;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = Person.Fields.houseOfResidence)
    private List<Person> residents;

    @Column(name = "create_date")
    private LocalDateTime createDate;
}

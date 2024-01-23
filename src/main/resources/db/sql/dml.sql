INSERT INTO House("uuid", area, country, city, street, number, create_date) VALUES
    ('acb8316d-3d13-4096-b1d6-f997b7307f0e', 64, 'Belarus','Grodno', 'Lenina st.', 101, '2022-10-29T06:12:12.123'),
    ('ae091b2b-de3f-4756-89bb-3a063b183da3', 36, 'Belarus','Minsk', 'Pushkina st.', 10, '2023-09-29T06:13:13.003'),
    ('e89895ef-ca4c-433b-87e8-3ead2646fed1', 24, 'Belarus','Gomel', 'Lobanka st.', 9, '2021-04-29T13:47:12.999'),
    ('01e311bf-ec36-47ca-91e6-e67c959c57cc', 103, 'Belarus','Vitebsk', 'Frunze st.', 4, '2024-01-01T14:12:12.332'),
    ('4aa71c48-9ded-42f1-b783-2a9e937a4f6d', 500, 'Belarus','Mogilev', 'Lenina st.', 7, '2024-01-01T17:16:10.001');

INSERT INTO Person("uuid",
                    name,
                    surname,
                    sex,
                    passport_series,
                    passport_number,
                    create_date,
                    update_date,
                    house_of_residence_id)
                    VALUES
                    ('95f3178e-f6a5-4ca6-b4f2-f0780a3f74b0', 'Pavel', 'Ivanov', 'M', 'MP', '1234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 1),
                    ('26df4783-5eae-4dd7-ae62-5249ea9c3c18', 'Ivan', 'Ivanov', 'M', 'XP', '2234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 1),
                    ('e95a9fd0-b305-4b0f-acec-993015fa8035', 'Yulia', 'Kozlova', 'W', 'ZP', '3234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 1),
                    ('236d7005-b86b-4697-b783-5eec2bc04dfa', 'Artem', 'Petuh', 'M', 'CP', '4234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 2),
                    ('13985e64-a4f1-42eb-b23e-8d8c12a3c14b', 'Matvey', 'Takun', 'M', 'VP', '5234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 2),
                    ('d06dbab0-a05d-4036-b66a-3161610eea0f', 'Andrey', 'Petrovich', 'M', 'BP', '6234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 4),
                    ('2aa5e50b-a76f-4a6e-a611-1bfda48fe7de', 'Petr', 'Petrovich', 'M', 'NP', '7234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 4),
                    ('1dd72b7d-9296-457d-b3e6-7a33ffe3abb2', 'Zhanna', 'Ivanova', 'W', 'MP', '8234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 4),
                    ('31a64056-a2f4-49fb-a9e8-952f5f7d264f', 'Valeriya', 'Sidorova', 'W', 'KP', '9234567890123', '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 4),
                    ('457ee2c4-7032-4adc-a294-c75adf1dc2bf','Kseniya', 'Chigireva', 'W', 'OP', '0234567890123',  '2020-09-09T10:00:00.000', '2020-09-09T10:00:00.000', 5);

INSERT INTO Owner_OwnedHouse(person_id, house_id) VALUES
    (1, 1),
    (2, 1),
    (2, 2),
    (2, 3),
    (3, 4),
    (4, 4),
    (4, 5);
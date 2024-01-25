<h1>Task Spring Boot</h1>
<h3>Перед запуском ru.clevertec.house.Application#main : </h3>
<p>(!) Поднять базу PostgreSQL через <code>docker compose up</code> в корневой папке</p>
<br>
<h2>Endpoints:</h2>
<p>
    <h3>[People]</h3>
    <h4>GET</h4>
    <p>(<i>параметр страницы -</i> page, <i>параметр числа ответов на странице - </i>size)</p>
    <ul>
        <li>
            <code>http://localhost:8080/people</code>
        </li>
        <li>
            <code>http://localhost:8080/people/{uuid}</code>
        </li>
        <li>
            <code>http://localhost:8080/people/{uuid}/ever-owned-houses</code>
        </li>
        <li>
            <code>http://localhost:8080/people/{uuid}/ever-lived-in-houses</code>
        </li>
        <li>
            <code>http://localhost:8080/people/{uuid}/owned-houses</code>
        </li>
    </ul>
    <h4>POST</h4>
        <ul>
        <li>
        <code>http://localhost:8080/people</code>
        </li>
        <br>
        Example request:
        <pre>
{
    "name": "my_name_1",
    "surname": "surname_1",
    "sex": "MALE",
    "passportNumber": "1234567893332",
    "passportSeries":"LL",
    "houseOfResidenceUUID": "e89895ef-ca4c-433b-87e8-3ead2646fed1"
}
        </pre>
        </ul>
    <h4>PUT</h4>
        <ul>
        <li>
        <code>http://localhost:8080/people/{uuid}</code>
        </li>
        <br>
        Example request:
        <pre>
{
    "name": "my_name_1",
    "surname": "surname_1",
    "sex": "MALE",
    "passportNumber": "1234567893332",
    "passportSeries":"LL",
    "houseOfResidenceUUID": "e89895ef-ca4c-433b-87e8-3ead2646fed1"
}
        </pre>
        </ul>
    <h4>DELETE</h4>
        <ul>
        <li>
        <code>http://localhost:8080/people/{uuid}</code>
        </li>
        </ul>
<p>
<h3>[Houses]</h3>
    <h4>GET</h4>
    <p>(<i>параметр страницы -</i> page, <i>параметр числа ответов на странице - </i>size)</p>
    <ul>
        <li>
            <code>http://localhost:8080/houses</code>
        </li>
        <li>
            <code>http://localhost:8080/houses/{uuid}</code>
        </li>
        <li>
            <code>http://localhost:8080/houses/{uuid}/ever-owners</code>
        </li>
        <li>
            <code>http://localhost:8080/houses/{uuid}/ever-residents</code>
        </li>
        <li>
            <code>http://localhost:8080/houses/{uuid}/residents</code>
        </li>
    </ul>
    <h4>POST</h4>
    <p>
        <ul>
        <li>
        <code>http://localhost:8080/houses</code>
        </li>
        <br>
        Example request:
        <pre>
{
    "area": 125.5,
    "country": "Germany",
    "city": "Hamburg",
    "street": "Bäckerbreitergang",
    "number": 29
}
        </pre>
        </ul>
    <h4>PUT</h4>
        <ul>
        <li>
        <p><code>http://localhost:8080/houses/{uuid}</code></p>
        <br>
        Example request:
        <pre>
{
    "area": 125.5,
    "country": "Germany",
    "city": "Hamburg",
    "street": "Bäckerbreitergang",
    "number": 29
}
        </pre>
        </li>
        <br>
        <li>
        <p><code>http://localhost:8080/houses/{uuid}/add-owner</code></p>
        <br>
        Example request:
        <pre>
"31a64056-a2f4-49fb-a9e8-952f5f7d264f"
        </pre>
        </li>
        </ul>
    <h4>DELETE</h4>
        <ul>
        <li>
        <code>http://localhost:8080/houses/{uuid}</code>
        </li>
        </ul>

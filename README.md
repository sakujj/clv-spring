<h2>Task Hibernate</h2>
<h3>Как запустить?</h3>
<p>В корневой папке прописать <code>docker compose up</code> и затем .war архив поместить в Apache Tomcat 10.
Адрес контроллеров следующий: <code>http://localhost:???/clv-house/[people|houses]</code>, где вместо <code>???</code>
порт запуска tomcat. Параметры для пагинации в гет запросах: <code>page(default = 1)</code>, <code>size(default = 15)</code>.</p>
<br>
<p>Не успел добавить валидацию. Написаны интеграционные тесты для репозиториев и юнит тесты для PersonMapper. 
JdbcTemplate используется при удалении в HouseRepository.</p>

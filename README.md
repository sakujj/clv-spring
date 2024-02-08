<h1>Task Spring Boot Aop Starter</h1>
<p> ( ! ) Перед запуском:
<ol>
<li>Поднять базу PostgreSQL через <code>docker compose up</code> в корневой папке.</li>
<li>Запустить проект через кастомную gradle-таску launch <code>./gradlew launch</code>, тем самым опубликовав стартер в mavenLocal,
а затем запустив приложение.</li>
</ol>
</p>
<br>
<p> Aop уже был реализован для кеша. Теперь кеш был перенесен в стартер.</p>
<p> Конфигурация кеша:
<ul>
   <li><code>sakujj.cache.isEnabled</code> - включен ли кеш, по умолчанию true;</li> 
   <li><code>sakujj.cache.type</code> - тип кеша, варианты: <i>"LFU"</i>, <i>"LRU"</i>. По умолчанию LRU;</li> 
   <li><code>sakujj.cache.capacity</code> - вместимость кеша, по умолчанию 100.</li> 
</ul>
</p>
<br>
<p>Проект переделан в многомодульный: <ol>
<li>
    Модуль <b>aop-cache-spring-boot-starter</b> - реализован стартер.
</li>
<li>
    Модуль <b>clevertec-house-person</b> - находится основной проект, для которого 1-ый модуль является
implementation зависимостью, берущейся из mavenLocal.
</li>
<li>
    Модуль <b>buildSrc</b> - для создания плагина <i>build-conventions</i> применяющего и хранящего общую для всех других 
модулей конфигурацию.
</li>
<li>Корневой родительский модуль <b>multi-project</b> - в нем имеется task launch, которая сначала публикует стартер в mavenLocal,
а затем запускает основной проект.</li>
</ol>
</p>
<br>
<h4>Документация основного приложения по URI: <a href="http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a></h4>
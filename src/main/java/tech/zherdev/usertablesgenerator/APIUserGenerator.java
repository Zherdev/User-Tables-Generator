/*
 * APIUserGenerator
 *
 * Ivan Zherdev, 2019
 */
package tech.zherdev.usertablesgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Класс APIUserGenerator предназначен для получения пользователей из API.
 * Производный от абстрактного класса AUserGenerator.
 *
 * @author Ivan Zherdev
 */
public class APIUserGenerator extends AUserGenerator {
    /* Используются библиотеки Gson и Apache HttpClient */

    /* API */
    private static final String URL = "http://randomuser.ru/api.json";

    private static HttpClient client = HttpClientBuilder.create().build();

    private static TextReader textReader  = new TextReader();

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(User.class, new CustomDeserializer())
            .create();

    /* Страну генерируем локально */
    private ArrayList<String> countries;

    private UserDB db;

    /**
     * @throws IOException в случае ошибки при чтении файла
     */
    APIUserGenerator() throws IOException {
        try {
            db = new UserDB();
        } catch (Exception e) {
            /* Не используем БД в случае любой ошибки при подключении */
            UserTablesGenerator.logger.error(e);
            db = null;
        }

        /* Загрузка списка стран из ресурсных файлов */
        try {
            String fileName = RESOURCE_FOLDER + "Страна.txt";
            countries = textReader.readFromFile(fileName);
        } catch (IOException e) {
            String message = e.getMessage() + " - Ошибка при чтении файла "
                    + "Страна.txt";
            throw new IOException(message);
        }
    }

    /**
     * Метод getResponseFromAPI(String url) возвращает ответ API на get запрос
     *
     * @param url адрес для get запроса
     * @return ответ API
     * @throws IOException в случае ошибки при обращении к API
     */
    private HttpResponse getResponseFromAPI(String url) throws IOException {
        HttpResponse response;
        HttpGet request = new HttpGet(url);

        // Выполняем запрос
        try {
           response = client.execute(request);
            // Получение кода состояния
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode < 200 || responseCode > 299) {
                throw new IOException("Неудача. Код состояния " + responseCode);
            }
        } catch (IOException e) {
            String message = e.getMessage() + " - Ошибка при обращении к API " +
                    url + ", проверьте соединение с Интернетом.";
            throw new IOException(message);
        }

        return response;
    }

    /**
     * Метод responseBodyToString(...) извлекает тело ответа API
     *
     * @param response ответ API
     * @return строка с телом ответа
     * @throws IOException в случае ошибки при обработке ответа API
     */
    private String responseBodyToString(HttpResponse response)
            throws  IOException {
        try {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            String message = e.getMessage() +
                    " - Ошибка при обработке ответа API " + URL;
            throw new IOException(message);
        }
    }

    /**
     * Метод generateUser() генерирует пользователя из данных, полученных от API
     *
     * @return пользователь
     * @throws UserGeneratorException в случае ошибки при получении
     * пользователя от API
     */
    public User generateUser() throws UserGeneratorException {
        String response = null;

        try {
            response = responseBodyToString(getResponseFromAPI(URL));
        } catch (IOException e) {
            String message = e.getMessage() + " - Ошибка при получении " +
                    "генератором пользователя от API";
            throw new UserGeneratorException(message);
        }

        /* substring избавляет от внешних скобок [ ] массива */
        JsonElement jUser = new JsonParser()
                .parse(response.substring(1, response.length() - 1));

        User user = gson.fromJson(jUser, User.class);

        user.setZip(random.nextInt(MAX_MAIL_INDEX - MIN_MAIL_INDEX)
                    + MIN_MAIL_INDEX);
        user.setAppart(random.nextInt(MAX_APPART_NUM) + 1);
        user.setCountry(countries.get(random.nextInt(countries.size())));
        user.setInn(innGenerator.generateINN());

        /* Запись в БД */
        if (db != null) {
            try {
                db.putUser(user);
            } catch (SQLException e) {
                String message = e.getMessage() + " - Ошибка при записи " +
                        "пользователя из API в БД";
                throw new UserGeneratorException(message);
            }
        }

        return user;
    }

}

package com.ibm.openpages.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <p>
 * This class is retrieving data from Bank of canada API - Currency rates
 * </p>
 *
 * @author dannymunoz on 2023-06-01
 * @project currency_op
 */
public class ApiClient {

    /**
     * <p>
     * This method will connect with the API
     * and will return a JSON representation of this data
     * </p>
     *
     * @param apiUrl String URL
     * @return
     */
    public static JsonNode getApiData(String apiUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            jsonNode = objectMapper.readTree(response.toString());

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonNode;
    }
}

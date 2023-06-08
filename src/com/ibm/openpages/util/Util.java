package com.ibm.openpages.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.openpages.api.configuration.IExchangeRate;
import com.ibm.openpages.api.service.IConfigurationService;
import com.ibm.openpages.beans.FXUSDCAD;
import com.ibm.openpages.beans.Observation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.ibm.openpages.constants.ConstantString.SLASH_CAD;

/**
 * Utility class for various currency-related operations.
 * <p>
 * This class provides methods for retrieving data from an API, processing JSON data,
 * and performing calculations and updates related to currency exchange rates.
 * </p>
 *
 * @author dannymunoz on 2023-06-05
 * @project currency_op
 */
public class Util {


    /**
     * Retrieves JSON data from the specified API URL.
     * <p>
     * This method connects to the API using the provided URL and retrieves the JSON data
     * representation of the API response. The JSON data is then parsed into a JsonNode object
     * and returned.
     *
     * @param apiUrl The URL of the API.
     * @return The JSON data retrieved from the API as a JsonNode object.
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

    /**
     * <p>
     * Retrieves observations for a specific year and month from the provided JSON data.
     * <p>
     * This method filters the observations based on the specified year and month from the JSON data.
     * It expects the JSON data to have a structure containing an "observations" node, where each observation
     * includes a date (d) and the exchange rate (FXUSDCAD). The method extracts the relevant observations
     * for the specified year and month, creates Observation objects, populates them with the date and exchange rate,
     * and adds them to a list. The list of observations for the specified year and month is then returned.
     * </p>
     *
     * @param jsonNode The JSON data containing the observations.
     * @param year     The year to filter the observations.
     * @param month    The month to filter the observations.
     * @return A list of observations for the specified year and month.
     */
    public static List<Observation> getObservationsForDate(JsonNode jsonNode, String year, String month) {
        List<Observation> observations = new ArrayList<>();

        if (jsonNode != null && jsonNode.has("observations")) {
            JsonNode observationsNode = jsonNode.get("observations");

            for (JsonNode observationNode : observationsNode) {
                String observationDate = observationNode.get("d").asText();
                String[] dateParts = observationDate.split("-");
                String observationYear = dateParts[0];
                String observationMonth = dateParts[1];

                if (observationYear.equals(year) && observationMonth.equals(month)) {
                    JsonNode fxusdcadNode = observationNode.get("FXUSDCAD");
                    String exchangeRate = fxusdcadNode.get("v").asText();

                    Observation observation = new Observation();
                    observation.setD(observationDate);

                    FXUSDCAD fxusdcad = new FXUSDCAD();
                    fxusdcad.setV(exchangeRate);

                    observation.setFxusdcad(fxusdcad);

                    observations.add(observation);
                }
            }
        }

        return observations;
    }

    /**
     * Retrieves observation labels from the provided JSON data.
     * <p>
     * This method extracts the observation labels from the JSON data. It expects the JSON data
     * to have a structure containing a "groupDetails" node, which further contains a "groupSeries"
     * node. The method iterates over the field names in the "groupSeries" node and retrieves the
     * "label" value for each field name. These labels are then added to a list, which is returned
     * as the result.
     *
     * @param jsonNode The JSON data containing the observation labels.
     * @return A list of observation labels.
     */
    public static List<String> getObservationLabels(JsonNode jsonNode) {
        List<String> labels = new ArrayList<>();

        if (jsonNode != null && jsonNode.has("groupDetails")) {
            JsonNode groupDetailsNode = jsonNode.get("groupDetails");

            if (groupDetailsNode.has("groupSeries")) {
                JsonNode groupSeriesNode = groupDetailsNode.get("groupSeries");

                Iterator<String> fieldNames = groupSeriesNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String label = fieldNames.next();
                    labels.add(groupSeriesNode.get(label).get("label").asText());
                }
            }
        }

        return labels;
    }


    /**
     * Calculates the average rate for a specific month and year based on the provided JSON data.
     * <p>
     * This method calculates the average rate for a specific month and year by iterating over the
     * observations in the JSON data. It expects the JSON data to have a structure containing an
     * "observations" node. It compares the year and month of each observation with the provided
     * year and month parameters. If a match is found, it retrieves the exchange rate for the
     * specified ISO code and calculates the average rate. The method returns the average rate as
     * a double value. If an error occurs or the rate is not found, it returns a default value of 0.0.
     * </p>
     *
     * @param isoCode    The ISO code for the currency.
     * @param year       The year for which to calculate the average rate.
     * @param month      The month for which to calculate the average rate.
     * @param jsonString The JSON data containing the observations.
     * @return The average rate for the specified month and year.
     */
    public static double getAverageMonthlyRates(String isoCode, String year, String month, JsonNode jsonString) {
        try {
            JsonNode observationsNode = jsonString.get("observations");
            for (JsonNode observationNode : observationsNode) {
                String dateValue = observationNode.get("d").asText();
                String observationYear = dateValue.substring(0, 4);
                String observationMonth = dateValue.substring(5, 7);

                if (observationYear.equals(year) && observationMonth.equals(month)) {
                    JsonNode fxNode = observationNode.get("FXM" + isoCode + "CAD");
                    if (fxNode != null) {
                        JsonNode valueNode = fxNode.get("v");
                        if (valueNode != null) {
                            String valueString = valueNode.asText();
                            System.out.println("Value for year " + year + " and month " + month + ": " + valueString);
                            return Double.parseDouble(valueString);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0; // Return a default value if an error occurs or the value is not found
    }

    /**
     * Updates the exchange rate for a specific ISO code with a new rate.
     * <p>
     * This method updates the exchange rate for a specific ISO code with a new rate. It takes the
     * ISO code, new rate, and a date as parameters. It uses the provided `configurationService` to
     * create an `IExchangeRate` object with the specified ISO code, new rate, and date. Then, it
     * sets the exchange rate using the `configurationService` by calling the `setExchangeRate`
     * method. If an exception occurs during the update process, it prints an error message and
     * stack trace to the console.
     * </p>
     *
     * @param configurationService The configuration service used to update the exchange rate.
     * @param isoCode              The ISO code for the currency.
     * @param newRate              The new exchange rate to be set.
     * @param date                 The date associated with the new exchange rate.
     */
    public static void updateOneRate(IConfigurationService configurationService, String isoCode, double newRate, Date date) {
        try {
            IExchangeRate exchangeRate = configurationService
                    .getConfigurationFactory()
                    .createExchangeRate(configurationService, isoCode, newRate, null);

            configurationService.setExchangeRate(exchangeRate);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            System.out.println(isoCode + " EXCEPTION");
            System.out.println(newRate + " EXCEPTION");
            e.printStackTrace();
        }

    }

    /**
     * Modifies the list of currency label names.
     * <p>
     * This method modifies the list of currency label names by removing occurrences of the
     * "/CAD" substring. It takes a `jsonObservationNames` parameter of type `JsonNode`, which
     * represents the observation names obtained from the API. The method uses the `getObservationLabels`
     * method from the `Util` class to retrieve the original currency label names. It then creates a new
     * list by applying a modification to each label name. The modification replaces the "/CAD" substring
     * with an empty string using the `replaceAll` method. Finally, the modified list of currency label
     * names is returned.
     * </p>
     *
     * @param jsonObservationNames The JSON node representing the observation names obtained from the API.
     * @return The modified list of currency label names.
     */
    public static List<String> listModificator(JsonNode jsonObservationNames) {

        List<String> currencyLabelNames = Util.getObservationLabels(jsonObservationNames);
        List<String> modifiedCurrencyLabelNames = currencyLabelNames.stream()
                .map(currencyLabel -> currencyLabel.replaceAll(SLASH_CAD, ""))
                .collect(Collectors.toList());

        return modifiedCurrencyLabelNames;
    }

}

package com.ibm.openpages.handler;


import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.openpages.api.Context;
import com.ibm.openpages.api.configuration.ICurrency;
import com.ibm.openpages.api.service.IConfigurationService;
import com.ibm.openpages.api.service.IServiceFactory;
import com.ibm.openpages.api.service.ServiceFactory;
import com.ibm.openpages.api.trigger.events.CreateResourceEvent;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;
import com.ibm.openpages.util.Util;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static com.ibm.openpages.constants.ConstantString.*;
import static com.ibm.openpages.util.ApiClient.getApiData;

/**
 * <p>
 * This class handles the currency trigger event in OpenPages.
 * It retrieves currency data from an API and updates the exchange rates configuration in OpenPages accordingly.
 * The trigger is executed when a new resource is created.
 * </p>
 * Author: dannymunoz
 * Date: 2023-06-01
 * Project: currency_op
 */
public class CurrencyHandler extends DefaultEventHandler {

    @Override
    public boolean handleEvent(CreateResourceEvent event) {

        try {

            // Get the context from the event
            Context context = event.getContext();

            // Get the service factory based on the context
            IServiceFactory serviceFactory = ServiceFactory.getServiceFactory(context);

            // Create the configuration service using the service factory
            IConfigurationService configurationService = serviceFactory.createConfigurationService();

            // Get the list of currencies from the configuration service
            // The boolean parameter 'true' indicates to include only active currencies
            List<ICurrency> currencies = serviceFactory.createConfigurationService().getCurrencies(true);

            System.out.println("----------------- currency trigger started -----------------");

            JsonNode jsonObservationNames = getApiData(API_URL);

            /**
             * <p>
             *     Calculating previous month
             * </p>
             */
            LocalDate date = LocalDate.now();
            String monthValue = "0" + (date.getMonthValue() - 1);
            String yearObs = String.valueOf(date.getYear());


            int i = 0;
            for (ICurrency iCurrency : currencies) {

                /**
                 * Getting ISO-CODE Monthly average rate in case that is equals to CAD
                 * the APIUrl will use USD instead of CAD because "FXMCADCAD" return null
                 * in JsonNodeMonthlyRates
                 */
                if (iCurrency.getCurrencyCode().toString().equals("CAD")) {

                    JsonNode jsonNodeMonthlyRates =
                            getApiData(
                                    FIRST_URL_PART
                                            + USD +
                                            SECOND_URL_PART);

                    // Getting double averageMonthlyRates - previous month
                    double averageMonthlyRates =
                            Util.getAverageMonthlyRates(USD,
                                    yearObs,
                                    monthValue, jsonNodeMonthlyRates);

                    // 1USD/CAD
                    // Converting USD to CAD into CAD to USD
                    double result = 1 / averageMonthlyRates;

                    // Rounding to 4 decimal
                    BigDecimal decimalResult = new BigDecimal(result);
                    decimalResult = decimalResult.setScale(4, RoundingMode.HALF_UP);

                    double formattedResult = decimalResult.doubleValue();

                    // Calling the method to update rates in OP
                    Util.updateOneRate(configurationService, iCurrency.getCurrencyCode()
                            .toString(), formattedResult, new Date());
                }

                if (!iCurrency.getCurrencyCode().toString().equals(SECOND_URL_PART)) {
                    JsonNode jsonNodeMonthlyRates =
                            getApiData(
                                    FIRST_URL_PART
                                            + iCurrency.getCurrencyCode().toString() +
                                            SECOND_URL_PART);

                    // Getting rate as a double
                    double averageMonthlyRates =
                            Util.getAverageMonthlyRates(iCurrency.getCurrencyCode()
                                            .toString(),
                                    yearObs,
                                    monthValue, jsonNodeMonthlyRates);

                    System.out.println("NO CAD ------ " + iCurrency.getCurrencyCode().toString());


                    double result = 1 / averageMonthlyRates;

                    BigDecimal decimalResult = new BigDecimal(result);
                    decimalResult = decimalResult.setScale(4, RoundingMode.HALF_UP);

                    double formattedResult = decimalResult.doubleValue();

                    System.out.println("RATE TO ENTER: " + formattedResult);

                    // Calling the method to update rates in OP
                    if (!iCurrency.getCurrencyCode().toString().equals("USD")) {
                        Util.updateOneRate(configurationService, iCurrency.getCurrencyCode()
                                .toString(), formattedResult, new Date());
                    }
                }
            }
            return true;

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

}

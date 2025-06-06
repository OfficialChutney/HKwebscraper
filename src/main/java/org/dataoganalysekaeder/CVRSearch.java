package org.dataoganalysekaeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dataoganalysekaeder.storeInfo.Address;
import org.dataoganalysekaeder.storeInfo.StoreInfo;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public class CVRSearch {

    public static String time = LocalDateTime.now().getDayOfMonth() + "_" + LocalDateTime.now().getMonthValue() + "_" + LocalDateTime.now().getYear() + "_" + LocalDateTime.now().getHour();
    public static boolean logChecked = false;
    public static File logFile = new File("./log/log_" + time + ".txt");

    public static void parse(StoreInfo storeinfo) {
        parse(storeinfo, true);
    }

    public static void parse(StoreInfo storeinfo, boolean checkAllVariables) {
        // Login credentials
        Address address = storeinfo.getAddress();
        String username = "HK_Danmark_2_CVR_I_SKYEN";
        String password = "f15545bc-5790-4ccf-8dac-469f556100dc";
        String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

        StringBuilder extraQuery = new StringBuilder();
        if (address.hasHouseChar() && checkAllVariables) {
            extraQuery.append(String.format("""
                    { "match": { "VrproduktionsEnhed.beliggenhedsadresse.bogstavFra": "%s" }},
                    """, address.getHouseChar()));
        }

        if (address.hasDoorValue() && checkAllVariables) {
            extraQuery.append(String.format("""
                    { "match": { "VrproduktionsEnhed.beliggenhedsadresse.sidedoer": "%s" }},
                    """, address.getDoor()));
        }

        if (address.hasFloorValue() && checkAllVariables) {
            extraQuery.append(String.format("""
                    { "match": { "VrproduktionsEnhed.beliggenhedsadresse.etage": "%s" }},
                    """, address.getFloor()));
        }


        String jsonQuery = String.format(
                """
                        {
                          "_source": [
                            "VrproduktionsEnhed.pNummer",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteCvrNummerRelation",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteNavn.navn",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteBeliggenhedsadresse.postnummer",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteBeliggenhedsadresse.husnummerFra",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteBeliggenhedsadresse.vejnavn",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteBeliggenhedsadresse.etage",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteBeliggenhedsadresse.sidedoer",
                            "VrproduktionsEnhed.produktionsEnhedMetadata.nyesteBeliggenhedsadresse.bogstavFra"
                          ],
                          "query": {
                            "bool": {
                              "must": [
                                %s
                                { "match": { "VrproduktionsEnhed.beliggenhedsadresse.vejnavn": "%s" }},
                                { "match": { "VrproduktionsEnhed.beliggenhedsadresse.husnummerFra": "%s" }},
                                { "match": { "VrproduktionsEnhed.beliggenhedsadresse.postnummer": "%s" }},
                                { "match": { "VrproduktionsEnhed.hovedbranche.branchekode": "471130" }}
                              ],
                              "must_not": [
                                { "exists": { "field": "VrproduktionsEnhed.livsforloeb.periode.gyldigTil" }}
                              ]
                            }
                          }
                        }
                        """, extraQuery, address.getStreet(), address.getHouseNumber(), address.getPostalCode());

        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://distribution.virk.dk/cvr-permanent/produktionsenhed/_search"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + auth)
                .POST(HttpRequest.BodyPublishers.ofString(jsonQuery))
                .build();

        JsonNode hits;


        HttpResponse<String> response;
        try {
            // Send request
            HttpClient client = HttpClient.newHttpClient();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            hits = root.at("/hits/hits");
        } catch (Exception e) {
            e.printStackTrace();
            log("ERROR FOR " + storeinfo.getName() + ", " + storeinfo.getAddress() + ": JSON response not succeeded", "", "");
            return;
        }

        for (JsonNode hit : hits) {
            String pNummer = hit.at("/_source/VrproduktionsEnhed/pNummer").asText();
            String cvr = hit.at("/_source/VrproduktionsEnhed/produktionsEnhedMetadata/nyesteCvrNummerRelation").asText();
            String cvrName = hit.at("/_source/VrproduktionsEnhed/produktionsEnhedMetadata/nyesteNavn/navn").asText();


            JsonNode adr = hit.at("/_source/VrproduktionsEnhed/produktionsEnhedMetadata/nyesteBeliggenhedsadresse");

            String parsedStreet = adr.path("vejnavn").asText();
            String parsedPostal = adr.path("postnummer").asText();
            String parsedHouseNumber = adr.path("husnummerFra").asText();

            if (address.getStreet().equalsIgnoreCase(parsedStreet) &&
                    (Integer.parseInt(parsedPostal) == address.getPostalCode()) &&
                    (Integer.parseInt(parsedHouseNumber) == address.getHouseNumber())) {

                String houseCharString = adr.path("bogstavFra").asText("null").trim();
                String sidedoor = adr.path("sidedoer").asText("null").trim();
                String floor = adr.path("etage").asText("null").trim().toLowerCase();


                if((!address.hasHouseChar() && !houseCharString.equalsIgnoreCase("null")) ||
                        (!address.hasDoorValue() && !sidedoor.equalsIgnoreCase("null")) ||
                        (!address.hasFloorValue() && !floor.equalsIgnoreCase("null"))) {

                    if(!checkAllVariables) {
                        continue;
                    }

                }


                if (address.hasHouseChar() && checkAllVariables) {
                    char houseChar = Character.toLowerCase(houseCharString.charAt(0));

                    if (houseChar != address.getHouseChar()) {
                        continue;
                    }
                }

                if (address.hasDoorValue() && checkAllVariables) {

                    if (sidedoor.equalsIgnoreCase(address.getDoor())) {
                        continue;
                    }
                }

                if (address.hasFloorValue() && checkAllVariables) {

                    if (!floor.equalsIgnoreCase(address.getFloor())) {
                        continue;
                    }
                }


                storeinfo.setPNummer(Integer.parseInt(pNummer));
                storeinfo.setCVRNum(Integer.parseInt(cvr));
                storeinfo.setCVRName(cvrName);
                return;
            }
        }

        if (checkAllVariables) {
            log("Ignoring All house variables for: " + storeinfo.getName() + ", " + storeinfo.getAddress(), "", "");
            parse(storeinfo, false);
            return;
        }

        log("ERROR FOR " + storeinfo.getName() + ", " + storeinfo.getAddress() + ": No hit found", response.body(), jsonQuery);

    }

    public static void log(String logString, String responseBody, String jsonQuery) {
        try {
            if (!logChecked && logFile.exists()) {
                int i = 1;
                while (logFile.exists()) {
                    logFile = new File("./log/log_" + time + "_" + i + ".txt");
                    i++;
                }
            }
            logChecked = true;


            try (FileOutputStream fos = new FileOutputStream(logFile, true);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 PrintWriter pw = new PrintWriter(osw, true)  // true = autoFlush
            ) {
                pw.println("****************************");
                pw.println(logString);
                pw.println(responseBody);
                pw.println(jsonQuery);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package org.dataoganalysekaeder.storeInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Address {
    String originalAddress;
    String street;
    int houseNumber;
    char houseChar;
    int postalCode;
    String city;
    String floor;
    String door;

    public Address(String originalAddress) {
        this.originalAddress = originalAddress.trim();

        boolean timeout;

        do {
            try {
                washAddress(this.originalAddress);
                timeout = false;
            } catch (Exception e) {
                timeout = true;
                e.printStackTrace();
            }
        } while (timeout);

    }

    public void washAddress(String address) throws IOException, InterruptedException {
        String originalAddress = address;
        if(!washAddress(originalAddress, true)) {
            washAddress(originalAddress, false);
        }
    }


    public boolean washAddress(String address, boolean useDatawash) throws IOException, InterruptedException {
        street = null;
        houseNumber = -1;
        houseChar = '\u0000';
        postalCode = -1;
        city = null;
        floor = null;
        door = null;



        // URL-encode (and force spaces to %20):
        String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);

        String url;

        if (useDatawash) {
            url = "https://api.dataforsyningen.dk/datavask/adresser?betegnelse=" + encoded;
        } else {
            url = "https://api.dataforsyningen.dk/autocomplete?q=" + encoded + "&fuzzy=";
        }


        // Send the request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            throw new RuntimeException("Non-OK response; aborting.");
        }


        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resp.body());
        JsonNode results = root;

        if (useDatawash) {
            String category = root.path("kategori").asText("");
            if(category.equals("C")) {
                return false;
            }

            results = root.path("resultater");
        }

        if (!results.isArray() || results.isEmpty()) {
            System.err.println("Ingen adresse fundet i 'resultater'.");
            return false;
        }

        try {
            for (JsonNode hit : results) {

                JsonNode clean;

                if (useDatawash) {
                    clean = hit.path("aktueladresse");
                    if (clean.isMissingNode() || clean.isNull()) {
                        clean = hit.path("adresse");
                    }

                } else {
                    clean = hit.path("data");
                }

                if (clean.isMissingNode() || clean.isNull()) {
                    continue;
                }


                street = clean.path("vejnavn").asText("").trim();
                floor = clean.path("etage").asText("").trim();
                door = clean.path("dør").asText("").trim();
                postalCode = Integer.parseInt(clean.path("postnr").asText("").trim());
                city = clean.path("postnrnavn").asText("").trim();

                String houseNumber = clean.path("husnr").asText("").trim();

                this.houseNumber = Integer.parseInt(houseNumber.replaceAll("\\D+", "").trim());

                String houseChar = houseNumber.replaceAll("\\d+", "").trim();

                if (!houseChar.isEmpty()) {
                    this.houseChar = houseChar.charAt(0);
                }


                return true;

            }
        } catch (Exception e) {
            System.out.println("NON RESPONSE FOR WASHING: " + originalAddress);
            System.out.println(resp.body());
        }

        return false;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(street)
                .append(" ")
                .append(houseNumber);

        if (hasHouseChar()) {
            sb.append(houseChar);
        }
        if (hasFloorValue()) {
            sb.append(" ").append(floor);
        }
        if (hasDoorValue()) {
            sb.append(" ").append(door);
        }

        sb.append(", ")
                .append(postalCode)
                .append(" ")
                .append(city);

        return sb.toString();
    }


    public String getStreet() {
        return street.toLowerCase();
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city.toLowerCase();
    }

    public String getFloor() {
        return floor.toLowerCase();
    }

    public String getDoor() {
        return door.toLowerCase();
    }

    public char getHouseChar() {
        return Character.toLowerCase(houseChar);
    }

    public boolean hasHouseChar() {
        return houseChar != '\u0000';
    }

    public boolean hasDoorValue() {
        return !door.isEmpty();
    }

    public boolean hasFloorValue() {
        return !floor.isEmpty();
    }
}

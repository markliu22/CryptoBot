package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.util.*;

public class App {
    // keep variables private, interest with them through public getters and setters
    // constants
    private static final String CB_ACCESS_SIGN = "CB-ACCESS-SIGN";
    private static final String CB_ACCESS_TIMESTAMP = "CB-ACCESS-TIMESTAMP";
    private static final String CB_ACCESS_KEY = "CB-ACCESS-KEY";
    private static final String CB_ACCESS_PASSPHRASE = "CB-ACCESS-PASSPHRASE";

    private static String BASE_URL;
    private static String API_KEY;
    private static String SECRET_KEY;
    private static String PASSPHRASE;

    // old decode was wrong, correct version: https://stackoverflow.com/questions/49679288/gdax-api-returning-invalid-signature-on-post-requests
    public static String generateSignedHeader(String requestPath, String method, String body, String timestamp) {
        try {
            String prehash = timestamp + method.toUpperCase() + requestPath + body;
            byte[] secretDecoded = Base64.getDecoder().decode(SECRET_KEY);
            SecretKeySpec keyspec = new SecretKeySpec(secretDecoded, Mac.getInstance("HmacSHA256").getAlgorithm());
            Mac sha256 = (Mac) Mac.getInstance("HmacSHA256").clone();
            sha256.init(keyspec);
            String response = Base64.getEncoder().encodeToString(sha256.doFinal(prehash.getBytes()));
            return response;
        } catch (CloneNotSupportedException | InvalidKeyException e) {
            System.out.println(e);
            throw new RuntimeErrorException(new Error("Cannot set up authentication headers."));
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
            throw new RuntimeErrorException(new Error("Cannot set up authentication headers."));
        }
    }

    // TODO: place order method
    public static void buyCoin(HttpClient client, String coinName) {
        // must have enough usd in their account
        // https://docs.pro.coinbase.com/#place-a-new-order
    }

    // TODO: sell method
    public static void sellCoin(HttpClient client, String coinName) {
        // https://docs.pro.coinbase.com/#create-conversion
        // convert to usd
    }

    // TODO: check out coin price method
    public static void getCoinPrice(HttpClient client, String coinName) {
        // TODO
    }

    // response contains a bunch of accounts, 1 for each coin
    public static HttpResponse<String> getAllAccounts(HttpClient client) throws IOException, InterruptedException {
        String TIMESTAMP = Instant.now().getEpochSecond() + "";
        String REQUEST_PATH = "/accounts";
        String METHOD = "GET";
        String SIGN = generateSignedHeader(REQUEST_PATH, METHOD, "", TIMESTAMP);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .setHeader(CB_ACCESS_SIGN, SIGN)
                .setHeader(CB_ACCESS_TIMESTAMP, TIMESTAMP)
                .setHeader(CB_ACCESS_KEY, API_KEY)
                .setHeader(CB_ACCESS_PASSPHRASE, PASSPHRASE)
                .setHeader("content-type", "application/json")
                .uri(URI.create(BASE_URL + "accounts"))
                .build();
        // response.body() contains a list of accounts, 1 for each coin
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    // start bot method
    public static void startProcess() throws IOException, InterruptedException {
        // auto import: https://stackoverflow.com/questions/63243193/has-intellij-idea2020-1-removed-maven-auto-import-dependencies
        // get env vars from: https://github.com/cdimascio/dotenv-java
        Dotenv dotenv = Dotenv.load();
        BASE_URL = dotenv.get("BASE_URL");
        API_KEY = dotenv.get("API_KEY");
        SECRET_KEY = dotenv.get("SECRET_KEY");
        PASSPHRASE = dotenv.get("PASSPHRASE");

        // Now can create request
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> res = getAllAccounts(client);
        System.out.println(res.body());

        // enter num coins you're watching
        Scanner sc = new Scanner(System.in);
        System.out.println("Number of coins to watch:");
        int numCoins = sc.nextInt();
        sc.nextLine();
        Coin[] coins = new Coin[numCoins];

        for(int i = 0; i < numCoins; i++) {
            System.out.println("Name of coin " + (i + 1) + ") ($):");
            String name = sc.nextLine();

            System.out.println("Target price to sell coin " + (i + 1) + ") ($):");
            double sellPrice = sc.nextDouble();

            System.out.println("Target price to buy coin " + (i + 1) + ") ($):");
            double buyPrice = sc.nextDouble();

            System.out.println("Maximum amount of coin " + (i + 1) + ") to sell at a time  ($):");
            double maxBuyAmount = sc.nextDouble();

            System.out.println("Maximum amount of coin " + (i + 1) + ") to buy at a time ($):");
            double maxSellAmount = sc.nextDouble();

            Coin currCoin = new Coin(name, sellPrice, buyPrice, maxBuyAmount, maxSellAmount);
            coins[i] = currCoin;
            System.out.println(currCoin.getName() + " added.");
            sc.nextLine();
        }

        // TODO: ask about risk tolerance



        // while true
            // for each Coin
                // check price of coin:
                    // if we own some of this coin && price > sellPrice: sell
                    // if price < buyPrice: buy
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        startProcess();
    }
}

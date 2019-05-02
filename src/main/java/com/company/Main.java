package com.company;

import com.company.utils.TokenUtils;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.Key;
import java.util.Scanner;

public class Main {

    private static final String SERVER_KEY = "wo/C7CHg0cECGwDn15Wotd761XonOpRLah61cr8ST7U=";

    public static void main(String[] args) {

        Database database = new Database();
        Spark.port(7575);

        Spark.post("/login", (request, response) -> login(database, request, response));

        Spark.post("/register", ((request, response) -> register(database, request, response)));

        Spark.get("/top_sekret", Main::topSekret);

        Spark.get("/json", ((request, response) -> readFromFile("duomenys.json")));

        Spark.get("/home", (request, response) -> readFromFile("index.html"));

        Spark.get("/getUsers", (request, response) -> getUsers(database));

        Spark.post("/createUser", (request, response) -> createUser(database, request));

    }

    private static Object register(Database database, Request request, Response response) {
        String username = request.queryParams("username");
        String password = request.queryParams("passowrd");
        String email = request.queryParams("email");

        if (username == null
                || password == null
                || email == null) {
            response.status(400);
            return null;
        }

        boolean created = database.register(username, password, email);

        if (created) {
            Key key = TokenUtils.createKeyFromString(SERVER_KEY);
            return TokenUtils.createToken(username, key);
        }
        return null;
    }

    private static Object createUser(Database database, Request request) {
        String vardas = request.queryParams("vardas");
        String pavarde = request.queryParams("pavarde");
        String amzius = request.queryParams("amzius");
        int i = Integer.parseInt(amzius.trim());
        database.insertUser(vardas, pavarde, i);
        return "Vartotojas sukurtas: " + vardas + " " + pavarde + " , " + amzius + " metai.";
    }

    private static Object getUsers(Database database) throws FileNotFoundException {
        readFromFile("sarasas.html");
        String turinys = readFromFile("sarasas.html");
        String duombazesTurinys = database.selectUsers(); //pasiimti duomenis is DB
        turinys = turinys.replace("{TURINYS}", duombazesTurinys);
        return turinys;
    }

    private static Object topSekret(Request request, Response response) {
        String token = request.headers("token");
        Key key = TokenUtils.createKeyFromString(SERVER_KEY);

        String username = TokenUtils.getUsername(token, key);
        if (username != null && username.length() > 0) {
            return "Slapta informacija";
        }
        response.status(401);
        return "Unauthorized access";
    }

    private static Object login(Database database, Request request, Response response) {
        String slapyvardis = request.queryParams("username");
        String slaptazodis = request.queryParams("slaptazodis");

        boolean isValid = database.isUserValid(slapyvardis, slaptazodis);
        if (isValid) {
            Key key = TokenUtils.createKeyFromString(SERVER_KEY);
            String token = TokenUtils.createToken(slapyvardis, key);
            return token;
        }
        response.status(401);
        return "";
    }

    private static String readFromFile(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        String result = "";
        while (scanner.hasNextLine()) {
            result += scanner.nextLine();
        }
        return result;
    }
}
package com.company;

import com.company.models.Vartotojas;
import com.company.utils.TokenUtils;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.utils.IOUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.*;
import java.security.Key;
import java.util.List;
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
        Spark.get("/getUsers", (request, response) -> gautiVartotojus(database));
        Spark.post("/createUser", (request, response) -> createUser(database, request));
        Spark.post("/passwordReminder", (request, response) -> remindPassword(database, response, request));
        Spark.put("/uploadImage", (request, response) -> uploadImage(request));
        Spark.put("/gautiVartotojus", (request, response) -> gautiVartotojus(database));


    }

    private static Object uploadImage(Request req) throws IOException, ServletException {
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("files/"));
        System.out.println("Upload method called");
        Part filePart = req.raw().getPart("imageParameterName");
        try (InputStream inputStream = filePart.getInputStream()) {
            OutputStream outputStream = new FileOutputStream("files/" + filePart.getSubmittedFileName());
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        return "File uploaded and saved.";
    }

    private static Object remindPassword(Database database, Response response, Request request) {
        String email = request.queryParams("email");
        System.out.println("Email: " + email);

        if (email == null) {
            response.status(400);
            return null;
        }
        String password = database.getPasswordForEmail(email);
        if (password != null) {
            sendEmail(email, password);
        } else {
            response.status(404);
            return null;
        }
        return "Email successfully sent";
    }

    private static void sendEmail(String recipient, String password) {
        Email emailMailer = EmailBuilder.startingBlank()
                .from("Serveriukas", "tas@kentas.lt")
                .to(recipient)
                .withSubject("Serveriukas password reminder")
                .withPlainText("Login password reminder. Your pasword is: " + password)
                .buildEmail();
        MailerBuilder
                .withDebugLogging(true)
                .withTransportStrategy(TransportStrategy.SMTPS)
                .withSMTPServer("muskatas.serveriai.lt", 465, "tas@kentas.lt", "kaunascoding123")
                .buildMailer()
                .sendMail(emailMailer);
    }

    private static Object register(Database database, Request request, Response response) {
        String username = request.queryParams("username");
        String password = request.queryParams("password");
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

        response.status(500);
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

    private static Object gautiVartotojus(Database database) throws FileNotFoundException {
//        readFromFile("sarasas.html");
        String turinys = readFromFile("sarasas.html");
        List<Vartotojas> vartotojai = database.gautiVartotojus(); //pasiimti duomenis is DB
        String duombazesTurinys = "";
        for (Vartotojas vartotojas : vartotojai) {
            duombazesTurinys += vartotojas.toString() + "\n";
        }
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
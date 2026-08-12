package MyPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.getWriter()
                .append("Served at: ")
                .append(request.getContextPath());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // OpenWeather API setup
        String myApiKey = System.getenv("OPENWEATHER_API_KEY");

        // Get city name from form input
        String city = request.getParameter("city");

        // Create OpenWeather API URL
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                + city + "&appid=" + myApiKey;

        try {

            // API Integration
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read data from network
            InputStream inpStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inpStream);

            // Store response in String
            StringBuilder responseContent = new StringBuilder();

            Scanner scanner = new Scanner(reader);

            while (scanner.hasNext()) {
                responseContent.append(scanner.nextLine());
            }

            scanner.close();

            // Convert response into JSON
            Gson gson = new Gson();

            JsonObject jsonObject =
                    gson.fromJson(responseContent.toString(), JsonObject.class);

            System.out.println(jsonObject);

            // Temperature
            double tempInKelvin =
                    jsonObject.getAsJsonObject("main")
                            .get("temp")
                            .getAsDouble();

            int tempInCelsius = (int) (tempInKelvin - 273.15);

            // Humidity
            int humidity =
                    jsonObject.getAsJsonObject("main")
                            .get("humidity")
                            .getAsInt();

            // Wind speed
            double windSpeed =
                    jsonObject.getAsJsonObject("wind")
                            .get("speed")
                            .getAsDouble();

            // Visibility
            int visibilityInMeter =
                    jsonObject.get("visibility").getAsInt();

            int visibility = visibilityInMeter / 1000;

            // Weather condition
            String weatherCondition =
                    jsonObject.getAsJsonArray("weather")
                            .get(0)
                            .getAsJsonObject()
                            .get("main")
                            .getAsString();

            // Cloud condition
            int cloudCover =
                    jsonObject.getAsJsonObject("clouds")
                            .get("all")
                            .getAsInt();

            // Date
            long dateTimestamp =
                    jsonObject.get("dt").getAsLong() * 1000;

            SimpleDateFormat sdfDate =
                    new SimpleDateFormat("EEE MMM dd yyyy");

            String date =
                    sdfDate.format(new Date(dateTimestamp));

            // Current time
            SimpleDateFormat sdfTime =
                    new SimpleDateFormat("HH:mm");

            String formattedTime =
                    sdfTime.format(new Date());

            // Send data to JSP
            request.setAttribute("date", date);
            request.setAttribute("city", city);
            request.setAttribute("visibility", visibility);
            request.setAttribute("temperature", tempInCelsius);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", windSpeed);
            request.setAttribute("cloudCover", cloudCover);
            request.setAttribute("currentTime", formattedTime);
            request.setAttribute("weatherData", responseContent.toString());

            connection.disconnect();

        } catch (IOException e) {

            e.printStackTrace();
        }

        // Forward request to JSP
        request.getRequestDispatcher("index.jsp")
                .forward(request, response);
    }
}
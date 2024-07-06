package com.aluracursos.conversor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@SpringBootApplication
public class ConversorApplication {
	private static final String API_KEY = "d37b1efdb358cd4002682bdc";
	private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

	public static void main(String[] args) {
		SpringApplication.run(ConversorApplication.class, args);

		System.out.println("******************************************************");
		System.out.println("*     Bienvenido a mi conversor de divisas           *");
		System.out.println("******************************************************");

		Scanner scanner = new Scanner(System.in);
		boolean continueConversion = true;

		Set<String> availableCurrencies = fetchAvailableCurrencies();

		while (continueConversion) {
			System.out.print("-Cantidad que desea convertir: ");
			double amount = scanner.nextDouble();
			System.out.println("------------------------------------------------------");

			System.out.print("- Divisa actual: ");
			String baseCurrency = scanner.next().toUpperCase();
			System.out.println("------------------------------------------------------");

			System.out.print("- Divisa de cambio: ");
			String targetCurrency = scanner.next().toUpperCase();
			System.out.println("------------------------------------------------------");


			try {
				validateCurrency(availableCurrencies, baseCurrency);
				validateCurrency(availableCurrencies, targetCurrency);

				double convertedAmount = convertCurrency(baseCurrency, targetCurrency, amount);
				System.out.printf("%.2f %s es equivalente a %.2f %s%n", amount, baseCurrency, convertedAmount, targetCurrency);
			} catch (IllegalArgumentException ex) {
				System.out.println("Error: " + ex.getMessage());
				System.out.println("Divisas disponibles: " + availableCurrencies);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Error: " + ex.getMessage());
			}

			System.out.println("*******************************************");
			System.out.print("¿Deseas realizar otra conversión? (si/no): ");
			String response = scanner.next().toLowerCase();
			if (!response.equals("si")) {
				continueConversion = false;
			}
		}

		scanner.close();
	}

	public static Set<String> fetchAvailableCurrencies() {
		Set<String> currencies = new HashSet<>();
		try {
			String urlStr = BASE_URL + API_KEY + "/latest/USD";
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuilder content = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			conn.disconnect();

			Gson gson = new Gson();
			JsonObject json = gson.fromJson(content.toString(), JsonObject.class);
			JsonObject conversionRates = json.getAsJsonObject("conversion_rates");

			// Obtener las claves (monedas) del objeto conversionRates
			Set<Map.Entry<String, JsonElement>> entries = conversionRates.entrySet();
			for (Map.Entry<String, JsonElement> entry : entries) {
				currencies.add(entry.getKey());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return currencies;
	}

	public static double convertCurrency(String baseCurrency, String targetCurrency, double amount) throws Exception {
		String urlStr = BASE_URL + API_KEY + "/latest/" + baseCurrency;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuilder content = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}

		in.close();
		conn.disconnect();

		Gson gson = new Gson();
		JsonObject json = gson.fromJson(content.toString(), JsonObject.class);
		JsonObject conversionRates = json.getAsJsonObject("conversion_rates");
		double exchangeRate = conversionRates.get(targetCurrency).getAsDouble();

		return amount * exchangeRate;
	}

	private static void validateCurrency(Set<String> availableCurrencies, String currency) {
		if (!availableCurrencies.contains(currency)) {
			throw new IllegalArgumentException("Divisa no válida: " + currency);
		}
	}
}











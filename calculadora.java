import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConversorMoedas {

    private static final String API_KEY = "a4cb2212b950848bb19aac9b";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/USD";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Map<String, Double> rates;
    private Map<String, Map<String, Double>> historicalData;

    public ConversorMoedas() {
        rates = new HashMap<>();
        historicalData = new HashMap<>();
    }

    public void obterTaxasDeCambio() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String jsonResponse = response.toString();
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonObject ratesObject = jsonObject.getAsJsonObject("conversion_rates");

        for (String key : ratesObject.keySet()) {
            rates.put(key, ratesObject.get(key).getAsDouble());
        }

        System.out.println("Taxas de câmbio obtidas com sucesso!");
    }

    public void converterMoeda(String moedaOrigem, String moedaDestino, double valor) {
        double taxaOrigem = rates.getOrDefault(moedaOrigem, 1.0);
        double taxaDestino = rates.getOrDefault(moedaDestino, 1.0);

        double valorConvertido = valor * (taxaDestino / taxaOrigem);

        System.out.println("Valor convertido: " + valorConvertido + " " + moedaDestino);

        // Registro do histórico de conversões
        LocalDateTime agora = LocalDateTime.now();
        String dataHoraFormatada = agora.format(DATE_TIME_FORMATTER);
        Map<String, Double> conversoes = historicalData.getOrDefault(dataHoraFormatada, new HashMap<>());
        conversoes.put(moedaOrigem + " para " + moedaDestino, valorConvertido);
        historicalData.put(dataHoraFormatada, conversoes);
    }

    public void exibirHistoricoConversoes() {
        System.out.println("Histórico de conversões:");
        for (String dataHora : historicalData.keySet()) {
            Map<String, Double> conversoes = historicalData.get(dataHora);
            System.out.println("Data/Hora: " + dataHora);
            for (String conversao : conversoes.keySet()) {
                System.out.println(conversao + " : " + conversoes.get(conversao));
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws IOException {
        ConversorMoedas conversor = new ConversorMoedas();
        conversor.obterTaxasDeCambio();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite a moeda de origem (ex: USD):");
        String moedaOrigem = scanner.nextLine().toUpperCase();
        System.out.println("Digite a moeda de destino (ex: BRL):");
        String moedaDestino = scanner.nextLine().toUpperCase();
        System.out.println("Digite o valor a ser convertido:");
        double valor = scanner.nextDouble();

        conversor.converterMoeda(moedaOrigem, moedaDestino, valor);
        conversor.exibirHistoricoConversoes();
    }
}

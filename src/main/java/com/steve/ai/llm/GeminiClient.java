package com.steve.ai.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steve.ai.SteveMod;
import com.steve.ai.config.SteveConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for Google Gemini API
 * FREE tier: 15 RPM, 1500 RPD
 * Paid: ~10x cheaper than GPT-3.5
 * Using gemini-2.5-flash with high token limit for thinking mode
 */
public class GeminiClient {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    
    private final HttpClient client;
    private final String apiKey;

    public GeminiClient() {
        this.apiKey = SteveConfig.OPENAI_API_KEY.get(); // We'll use the same config for now
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    }

    public String sendRequest(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            SteveMod.LOGGER.error("Gemini API key not configured!");
            return null;
        }

        JsonObject requestBody = buildRequestBody(systemPrompt, userPrompt);

        // Pass the key as a header instead of a URL query parameter so it is
        // never embedded in the request URI and cannot appear in HTTP-level logs
        // or exception stack traces.
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GEMINI_API_URL))
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", apiKey)
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                SteveMod.LOGGER.error("Gemini API request failed: {}", response.statusCode());
                // Log only a truncated body — never include the API key
                String body = response.body();
                SteveMod.LOGGER.error("Response body: {}",
                    body != null && body.length() > 500 ? body.substring(0, 500) + "..." : body);
                return null;
            }

            String responseBody = response.body();
            if (responseBody == null || responseBody.isEmpty()) {
                SteveMod.LOGGER.error("Gemini API returned empty response");
                return null;
            }

            return parseResponse(responseBody);
            
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error communicating with Gemini API", e);
            return null;
        }
    }

    private JsonObject buildRequestBody(String systemPrompt, String userPrompt) {
        JsonObject body = new JsonObject();

        // Use the dedicated system_instruction field so the model treats the
        // system context with appropriate weight, separate from the user turn.
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", systemPrompt);
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        body.add("system_instruction", systemInstruction);

        // User message
        JsonArray contents = new JsonArray();
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userPartObj = new JsonObject();
        userPartObj.addProperty("text", userPrompt);
        userParts.add(userPartObj);
        userContent.add("parts", userParts);
        contents.add(userContent);
        body.add("contents", contents);
        
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", SteveConfig.TEMPERATURE.get());
        generationConfig.addProperty("maxOutputTokens", SteveConfig.MAX_TOKENS.get());
        body.add("generationConfig", generationConfig);
        
        return body;
    }

    private String parseResponse(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            // Gemini response format: candidates[0].content.parts[0].text
            if (json.has("candidates") && json.getAsJsonArray("candidates").size() > 0) {
                JsonObject firstCandidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
                
                if (firstCandidate.has("finishReason")) {
                    String finishReason = firstCandidate.get("finishReason").getAsString();
                    if ("MAX_TOKENS".equals(finishReason)) {
                        SteveMod.LOGGER.error("Gemini response was cut off due to MAX_TOKENS limit");
                    }
                }
                
                if (firstCandidate.has("content")) {
                    JsonObject content = firstCandidate.getAsJsonObject("content");
                    if (content.has("parts") && content.getAsJsonArray("parts").size() > 0) {
                        JsonObject firstPart = content.getAsJsonArray("parts").get(0).getAsJsonObject();
                        if (firstPart.has("text")) {
                            return firstPart.get("text").getAsString();
                        }
                    } else {
                        SteveMod.LOGGER.error("Gemini response has no 'parts' in content - response may have been cut off");
                    }
                }
            }
            
            SteveMod.LOGGER.error("Unexpected Gemini response format: {}", responseBody);
            return null;
            
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error parsing Gemini response", e);
            return null;
        }
    }
}


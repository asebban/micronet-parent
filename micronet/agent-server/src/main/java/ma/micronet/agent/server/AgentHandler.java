package ma.micronet.agent.server;

import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ma.micronet.agent.api.AgentProcessorFactory;
import ma.micronet.agent.api.IAgentProcessor;
import ma.micronet.agent.api.IProcessingUnit;
import ma.micronet.commons.Message;

public class AgentHandler implements Runnable {

    private Socket socket;
    private IAgentProcessor processor;
    private Logger logger = LoggerFactory.getLogger(AgentHandler.class);
    
    public AgentHandler(Socket socket, IAgentProcessor processor) {
        this.socket = socket;
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            InputStream is = this.socket.getInputStream();
            byte[] buffer = new byte[1024];
            is.read(buffer);
            String request = new String(buffer, "UTF-8");
            request = request.trim();
            logger.debug("Agent Handler ID " + processor.getAgent().getId() + ": Received request: " + request);
            Gson gson = new Gson();
            Message requestMessage = gson.fromJson(request, Message.class);
            Message preProcessedMessage = processor.process(requestMessage);
            preProcessedMessage.setSenderAdressable(processor.getAgent());

            List<IProcessingUnit> processingUnits = AgentProcessorFactory.findProcessingUnits();
            String rootPath = processor.getAgent().getPath();
            rootPath = formatPath(rootPath);

            Message responseMessage=null;

            for (IProcessingUnit processingUnit : processingUnits) {
                
                String messagePath = requestMessage.getPath();
                String formattedMessagePath = formatPath(messagePath);
                String templatePath = processingUnit.registerRelativePath();
                templatePath = rootPath + formatPath(templatePath);

                if (matchesTemplate(templatePath, formattedMessagePath)) {
                    Map<String, String> pathVariables = extractPathVariablesAndValues(templatePath, formattedMessagePath);
                    preProcessedMessage.getParameters().putAll(pathVariables);
                    Map<String, String> queryParameters = extractQueryParameters(messagePath);
                    preProcessedMessage.getParameters().putAll(queryParameters);
                    responseMessage = processingUnit.execute(preProcessedMessage);
                    break;
                }
            }
            
            if (responseMessage == null) {
                logger.debug("Agent Handler ID " + processor.getAgent().getId() + ": No processing unit found for the request");
                responseMessage = preProcessedMessage;
            }

            String responseString = gson.toJson(responseMessage);
            logger.debug("Agent Handler ID " + processor.getAgent().getId() + ": Sending response: " + responseString);
            this.socket.getOutputStream().write(responseString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }   
    }

    private String formatPath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // remove any String that comes after a "?" character
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }
        return path;
    }

    private Map<String, String> extractPathVariablesAndValues(String template, String s) {
        if (template == null || s == null) {
            throw new IllegalArgumentException("AgentHandler.extractPathVariablesAndValues: template and s must not be null");
        }
        Map<String, String> pathVariables = new HashMap<>();
        ArrayList<String> groupNames = new ArrayList<>();

        // Convert the template to a regex pattern and store group names
        // Pattern to find placeholders in the template
        Pattern placeholderPattern = Pattern.compile("\\{(.*?)}");
        Matcher placeholderMatcher = placeholderPattern.matcher(template);

        StringBuffer regexBuffer = new StringBuffer(template);
        while (placeholderMatcher.find()) {
            groupNames.add(placeholderMatcher.group(1));
            placeholderMatcher.appendReplacement(regexBuffer, "(.*?)");
        }
        placeholderMatcher.appendTail(regexBuffer);

        String regex = regexBuffer.toString();
        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);

        if (matcher.matches()) {
            // Extract the values and print them along with their names
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String extractedValue = matcher.group(i);
                String groupName = groupNames.get(i - 1);
                pathVariables.put(groupName, extractedValue);
            }
        }
        else {
            logger.debug("AgentHandler.extractPathVariablesAndValues: No pathVariable found");
        }

        return pathVariables;
    }

    private Map<String, String> extractQueryParameters(String s) {
        if (s == null) {
            throw new IllegalArgumentException("AgentHandler.extractQueryParameters: s must not be null");
        }

        Map<String, String> queryParameters = new HashMap<>();
        // Extract the query parameters
        if (s.contains("?")) {
            String query = s.substring(s.indexOf("?") + 1);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                queryParameters.put(keyValue[0], keyValue[1]);
            }
        }
        return queryParameters;
    }

    public boolean matchesTemplate(String template, String url) {
        // Escape special regex characters in the template
        String escapedTemplate = Pattern.quote(template);

        // Replace the placeholders with regex patterns
        String regex = escapedTemplate.replaceAll("\\\\\\{[^}]+\\\\\\}", "([^/]+)");

        // Create a pattern from the regex
        Pattern pattern = Pattern.compile(regex);
        
        // Match the URL against the pattern
        Matcher matcher = pattern.matcher(url);

        // Check if the entire URL matches the pattern
        return matcher.matches();
    }

}

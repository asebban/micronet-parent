package ma.micronet.agent.server;

import java.io.IOException;
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

import ma.micronet.agent.api.AgentProcessorFactory;
import ma.micronet.agent.api.IAgentProcessor;
import ma.micronet.agent.api.IProcessingUnit;
import ma.micronet.commons.Message;
import ma.micronet.commons.MicroNetException;

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
            Message requestMessage = Message.jsonToMessage(request);
            String messageId = requestMessage.getMessageId();
            Message preProcessedMessage = processor.process(requestMessage);
            preProcessedMessage.setMessageId(messageId); // restore the original message ID if it was modified
            preProcessedMessage.setSenderAdressable(processor.getAgent());

            List<IProcessingUnit> processingUnits = AgentProcessorFactory.findProcessingUnits();
            String rootPath = processor.registerPath();
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
                    String messageID = preProcessedMessage.getMessageId();
                    responseMessage = dispatch(processingUnit, preProcessedMessage);
                    responseMessage.setMessageId(messageID); // restore the original message ID if it was modified
                    break;
                }
            }
            
            if (responseMessage == null) {
                logger.debug("Agent Handler ID " + processor.getAgent().getId() + ": No processing unit found for the request");
                responseMessage = preProcessedMessage;
            }

            String responseString = responseMessage.toString();
            logger.debug("Agent Handler ID " + processor.getAgent().getId() + ": Sending response: " + responseString);
            this.socket.getOutputStream().write(responseString.getBytes());
        } catch (Exception e) {
            Message error = Message.errorMessage("Error processing the request: " + e.getMessage());
            try {
                this.socket.getOutputStream().write(error.toString().getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
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

    private Map<String, String> extractPathVariablesAndValues(String template, String path) {
        if (template == null || path == null) {
            throw new IllegalArgumentException("AgentHandler.extractPathVariablesAndValues: template and s must not be null");
        }
        Map<String, String> pathVariables = new HashMap<>();
        ArrayList<String> groupNames = new ArrayList<>();

        // Convert the template to a regex pattern and store group names
        // Pattern to find placeholders in the template
        Pattern placeholderPattern = Pattern.compile("\\{(.*?)}");
        Matcher placeholderMatcher = placeholderPattern.matcher(template);

        StringBuffer regexBuffer = new StringBuffer("");

        while (placeholderMatcher.find()) {
            groupNames.add(placeholderMatcher.group(1));
            placeholderMatcher.appendReplacement(regexBuffer, "(.*?)");
        }
        placeholderMatcher.appendTail(regexBuffer);

        String regex = regexBuffer.toString();
        // Compile the pattern
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);

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

    private Map<String, String> extractQueryParameters(String chain) {
        if (chain == null) {
            throw new IllegalArgumentException("AgentHandler.extractQueryParameters: s must not be null");
        }

        Map<String, String> queryParameters = new HashMap<>();
        // Extract the query parameters
        if (chain.contains("?")) {
            String query = chain.substring(chain.indexOf("?") + 1);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                queryParameters.put(keyValue[0], keyValue[1]);
            }
        }
        return queryParameters;
    }

    public boolean matchesTemplate(String template, String url) {
        // Replace the placeholders with regex patterns
        String regex = template.replaceAll("\\{[^}]+\\}", "([^/]+)");

        // Create a pattern from the regex
        Pattern pattern = Pattern.compile(regex);
        
        // Match the URL against the pattern
        Matcher matcher = pattern.matcher(url);

        // Check if the entire URL matches the pattern
        return matcher.matches();
    }

    private Message dispatch(IProcessingUnit processingUnit, Message message) throws MicroNetException {
        
        Message response = Message.copy(message);
        response.setDirection(Message.RESPONSE);

        switch(message.getCommand()) {
            case Message.GET:
                response = processingUnit.get(message);
                break;
            case Message.ADD:
                response = processingUnit.add(message);
                break;
            case Message.UPDATE:
                response = processingUnit.update(message);
                break;
            case Message.DELETE:
                response = processingUnit.delete(message);
                break;
            default:
                logger.debug("AgentHandler.dispatch: Unknown command: " + message.getCommand());
                response = processingUnit.execute(message);
                return response;
        }

        return response;
    }

}

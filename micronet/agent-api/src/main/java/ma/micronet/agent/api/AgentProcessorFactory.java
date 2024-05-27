package ma.micronet.agent.api;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ma.micronet.commons.MicroNetException;

public class AgentProcessorFactory {

    private static Logger logger = LoggerFactory.getLogger(AgentProcessor.class);

    @SuppressWarnings("deprecation")
    public static AgentProcessor findProcessorInstance() throws MicroNetException {
        
        Reflections reflections = new Reflections("ma.micronet");
        for (Class<? extends AgentProcessor> clazz : reflections.getSubTypesOf(AgentProcessor.class)) {
            try {
                logger.debug("Found processor: " + clazz.getName());
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Agent Processor Instantiation problem", e);
            }
        }
        throw new MicroNetException("No processor found!");
    }

}

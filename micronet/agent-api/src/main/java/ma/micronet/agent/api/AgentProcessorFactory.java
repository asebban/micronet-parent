package ma.micronet.agent.api;

import java.util.ArrayList;
import java.util.List;

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

    @SuppressWarnings("deprecation")
    public static List<IProcessingUnit> findProcessingUnits() throws MicroNetException {
        
        Reflections reflections = new Reflections("ma.micronet");
        ArrayList<IProcessingUnit> processingUnits = new ArrayList<>();
        for (Class<? extends IProcessingUnit> clazz : reflections.getSubTypesOf(IProcessingUnit.class)) {
            try {
                logger.debug("Found processing unit: " + clazz.getName());
                processingUnits.add(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Agent Processor Instantiation problem", e);
            }
        }

        if (!processingUnits.isEmpty())
            return processingUnits;
        else {
            logger.debug("No processing units found!");
            return new ArrayList<>();
        }
    }

}

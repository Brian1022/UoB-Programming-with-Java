package edu.uob;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameActionParser {

    private final File actionsFile;

    public GameActionParser(File actionsFile) {
        this.actionsFile = actionsFile;
    }

    public List<GameAction> parse() {
        List<GameAction> actions = new java.util.LinkedList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(actionsFile);
            doc.getDocumentElement().normalize();

            NodeList actionNodes = doc.getElementsByTagName("action");

            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element actionElement = (Element) actionNodes.item(i);

                Set<String> triggers = this.extractValues(actionElement, "triggers", "keyphrase");
                Set<String> subjects = this.extractValues(actionElement, "subjects", "entity");
                Set<String> consumed = this.extractValues(actionElement, "consumed", "entity");
                Set<String> produced = this.extractValues(actionElement, "produced", "entity");

                String narration = "";
                NodeList narrationNodes = actionElement.getElementsByTagName("narration");
                if (narrationNodes.getLength() > 0) {
                    narration = narrationNodes.item(0).getTextContent();
                }

                GameAction action = new GameAction(triggers, subjects, consumed, produced, narration);
                actions.add(action);
            }

        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to parse actions file: ");
            sb.append(e.getMessage());
            System.err.println(sb.toString());
        }

        return actions;
    }

    private Set<String> extractValues(Element parent, String sectionName, String tagName) {
        Set<String> values = new HashSet<>();
        NodeList sectionList = parent.getElementsByTagName(sectionName);
        if (sectionList.getLength() > 0) {
            Element section = (Element) sectionList.item(0);
            NodeList valueNodes = section.getElementsByTagName(tagName);
            for (int i = 0; i < valueNodes.getLength(); i++) {
                values.add(valueNodes.item(i).getTextContent().toLowerCase());
            }
        }
        return values;
    }
}
